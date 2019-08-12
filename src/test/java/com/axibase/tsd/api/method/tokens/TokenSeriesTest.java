package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.*;

import static org.testng.AssertJUnit.assertTrue;

public class TokenSeriesTest extends SeriesTest {
    private static final int VALUE = Mocks.INT_VALUE;
    private static final String SAMPLE_TIME = Mocks.ISO_TIME;

    private final String entity = Mocks.entity();
    private final String metric = Mocks.metric();
    private final String username;
    private Series series;


    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenSeriesTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        series = new Series(entity, metric)
                .addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE))
                .setType(SeriesType.HISTORY);
        insertSeriesCheck(series);
    }

    @Test(
            description = "Tests series get endpoint with tokens."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String getURL = "/series/json/" + entity + "/" + metric;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, String.format(getURL + "?startDate=%s&interval=%s&timeFormat=%s", SAMPLE_TIME, "1-DAY", "iso"));
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("startDate", SAMPLE_TIME);
        parameters.put("interval", "1-DAY");
        parameters.put("timeFormat", "iso");
        Response response = urlQuerySeries(entity, metric, parameters, getToken);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(series)), response.readEntity(String.class), false));
    }

    @Test(
            description = "Tests series query endpoint with tokens."
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String queryURL = "/series/query";
        SeriesQuery query = new SeriesQuery(entity, metric)
                .setStartDate(SAMPLE_TIME)
                .setInterval(new Interval(1, TimeUnit.DAY));
        String queryToken = TokenRepository.getToken(username, HttpMethod.POST, queryURL);
        Response response = querySeries(Collections.singletonList(query), queryToken);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(series)), response.readEntity(String.class), false));
    }

    @Test(
            description = "Tests series insert endpoint with tokens."
    )
    @Issue("6052")
    public void testInsertMethod() throws Exception {
        String insertURL = "/series/insert";
        String insertToken = TokenRepository.getToken(username, HttpMethod.POST, insertURL);
        List<Series> seriesList = new ArrayList<>();
        Series series = new Series(Mocks.entity(), Mocks.metric());
        series.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        seriesList.add(series);
        insertSeries(seriesList, insertToken);
        Checker.check(new SeriesCheck(seriesList));
    }

    @Test(
            description = "Tests series delete endpoint with tokens."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        //creating data for series that will be deleted
        String deletionEntity = Mocks.entity();
        String deletionMetric = Mocks.metric();
        Series deletionSeries = new Series(deletionEntity, deletionMetric);
        deletionSeries.addSamples(Sample.ofDateInteger(SAMPLE_TIME, VALUE));
        insertSeriesCheck(deletionSeries);

        String deleteURL = "/series/delete";
        SeriesQuery deleteQuery = new SeriesQuery(deletionEntity, deletionMetric);
        String deleteToken = TokenRepository.getToken(username, HttpMethod.POST, deleteURL);
        deleteSeries(Collections.singletonList(deleteQuery), deleteToken);
        //checking that series was successfully deleted
        Checker.check(new DeletionCheck(new SeriesCheck(Collections.singletonList(deletionSeries))));
    }
}
