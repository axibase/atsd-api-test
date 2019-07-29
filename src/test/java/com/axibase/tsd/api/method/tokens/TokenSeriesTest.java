package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.NotPassedCheck;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Util.prettyPrint;
import static org.testng.AssertJUnit.assertTrue;

public class TokenSeriesTest extends TokenWorkTest {
    private final String entity = Mocks.entity();
    private final String metric = Mocks.metric();
    private static final int VALUE = Mocks.value();
    private static final String SAMPLE_TIME = Mocks.ISO_TIME;

    private Series series;
    private final String username;


    @Factory(
            dataProvider = "users"
    )
    public TokenSeriesTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        series = new Series(entity, metric);
        series.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        SeriesMethod.insertSeriesCheck(series);
    }

    @Test
    @Issue("6052")
    public void getMethodTest() throws Exception{
        String getURL = "/series/json/" + entity + "/" + metric;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, String.format(getURL + "?startDate=%s&endDate=%s", SAMPLE_TIME, SAMPLE_TIME));
        Response response = executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + getURL)
                .queryParam("startDate", "previous_hour")
                .queryParam("endDate", "next_day")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getToken)
                .method(HttpMethod.GET));
        response.bufferEntity();
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
        Response response = query(queryURL, query, queryToken);
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
        insert(username, insertURL, seriesList, insertToken);
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
        SeriesMethod.insertSeriesCheck(deletionSeries);

        String deleteURL = "/series/delete";
        SeriesQuery delete = new SeriesQuery(deletionEntity, deletionMetric);
        delete.setExactMatch(false);
        List<SeriesQuery> deleteQuery = new ArrayList<>();
        deleteQuery.add(delete);
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        executeTokenRootRequest(webTarget -> webTarget.path(API_PATH + deleteURL)
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + deleteToken)
                .method(HttpMethod.POST, Entity.json(deleteQuery)))
                .bufferEntity();
        //checking that series was successfully deleted
        Checker.check(new NotPassedCheck(new SeriesCheck(Collections.singletonList(deletionSeries))));
    }
}
