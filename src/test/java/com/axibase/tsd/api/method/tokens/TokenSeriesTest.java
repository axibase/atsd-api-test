package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.util.*;

import static com.axibase.tsd.api.util.Util.prettyPrint;
import static org.testng.AssertJUnit.assertTrue;

public class TokenSeriesTest extends SeriesMethod {
    private final String entity = Mocks.entity();
    private final String metric = Mocks.metric();
    private static final int VALUE = Mocks.INT_VALUE;
    private static final String SAMPLE_TIME = Mocks.ISO_TIME;

    private Series series;
    private final String username;


    @Factory(
            dataProvider = "users", dataProviderClass = TokenWorkTest.class
    )
    public TokenSeriesTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        series = new Series(entity, metric);
        series.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        insertSeriesCheck(series);
    }

    @Test
    @Issue("6052")
    public void getMethodTest() throws Exception{
        String getURL = "/series/json/" + entity + "/" + metric;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, String.format(getURL + "?startDate=%s&endDate=%s", SAMPLE_TIME, SAMPLE_TIME));
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("startDate", SAMPLE_TIME);
        parameters.put("endDate", SAMPLE_TIME);
        Response response = urlQuerySeries(entity, metric, parameters, getToken);
        String responseEntity = response.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseEntity, !(responseEntity.contains("warning")));
        compareJsonString(prettyPrint(Collections.singletonList(series)), responseEntity, false);
    }

    @Test
    @Issue("6052")
    public void queryMethodTest() throws Exception {
        String queryURL = "/series/query";
        SeriesQuery q = new SeriesQuery(entity, metric, SAMPLE_TIME, SAMPLE_TIME);
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        List<SeriesQuery> query = new ArrayList<>();
        query.add(q);
        Response response = querySeries(query, queryToken);
        String responseEntity = response.readEntity(String.class);
        assertTrue("User: " + username + " Response contains warning: " + responseEntity, !(responseEntity.contains("warning")));
        compareJsonString(prettyPrint(query), responseEntity, false);
    }

    @Test
    @Issue("6052")
    public void insertMethodTest() throws Exception {
        String insertURL = "/series/insert";
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        List<Series> seriesList = new ArrayList<>();
        Series series = new Series(Mocks.entity(), Mocks.metric());
        series.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        seriesList.add(series);
        insertSeries(seriesList, insertToken);
        Checker.check(new SeriesCheck(seriesList));
    }

    @Test
    @Issue("6052")
    public void deleteMethodTest() throws Exception {
        //creating data for series that will be deleted
        String deletionEntity = Mocks.entity();
        String deletionMetric = Mocks.metric();
        Series deletionSeries = new Series(deletionEntity, deletionMetric);
        deletionSeries.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        insertSeriesCheck(deletionSeries);

        String deleteURL = "/series/delete";
        SeriesQuery delete = new SeriesQuery(deletionEntity, deletionMetric);
        delete.setExactMatch(false);
        List<SeriesQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        deleteSeries(deleteQuery, deleteToken);
        //checking that series was successfully deleted
        Checker.check(new DeletionCheck(new SeriesCheck(Collections.singletonList(deletionSeries))));
    }
}
