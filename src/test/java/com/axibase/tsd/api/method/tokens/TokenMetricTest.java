package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.metric.MetricTest;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.metric.MetricSeriesResponse;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class TokenMetricTest extends MetricTest {
    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenMetricTest(String username) {
        this.username = username;
    }

    @Test(
            description = "Tests metric get endpoint."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName;
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Metric metric = new Metric(metricName);
        createOrReplaceMetricCheck(metric);

        Response response = queryMetric(metricName, token);
        assertTrue(compareJsonString(Util.prettyPrint(metric), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests metric update endpoint."
    )
    @Issue("6052")
    public void testUpdateMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName;
        String token = TokenRepository.getToken(username, "PATCH", url);
        Metric metric = new Metric(metricName);
        createOrReplaceMetricCheck(metric);

        metric.setLabel(Mocks.LABEL);
        updateMetric(metric, token);
        Checker.check(new MetricCheck(metric));
    }

    @Test(
            description = "Tests metric create or replace endpoint."
    )
    @Issue("6052")
    public void testCreateMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName;
        String token = TokenRepository.getToken(username, HttpMethod.PUT, url);
        Metric metric = new Metric(metricName);

        createOrReplaceMetric(metric, token);
        Checker.check(new MetricCheck(metric));
    }

    @Test(
            description = "Tests metric rename endpoint."
    )
    @Issue("6052")
    public void testRenameMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName + "/rename";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        createOrReplaceMetricCheck(metricName);

        String newName = Mocks.metric();
        Metric newMetric = new Metric(newName);
        renameMetric(metricName, newName, token);
        Checker.check(new MetricCheck(newMetric));
    }

    @Test(
            description = "Tests metric delete endpoint."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName;
        String token = TokenRepository.getToken(username, HttpMethod.DELETE, url);
        Metric metric = new Metric(metricName);
        createOrReplaceMetricCheck(metric);

        deleteMetric(metricName, token);
        Checker.check(new DeletionCheck(new MetricCheck(metric)));
    }

    @Test(
            description = "Tests metric series endpoint."
    )
    @Issue("6052")
    public void testSeriesMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName + "/series";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Series series = new Series(Mocks.entity(), metricName)
                .addSamples(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(series);

        Response response = queryMetricSeries(metricName, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(new MetricSeriesResponse(series))),
                response.readEntity(String.class)));
    }

    @Test(
            description = "Tests metric series tags endpoint."
    )
    @Issue("6052")
    public void testSeriesTagsMethod() throws Exception {
        String metricName = Mocks.metric();
        String url = "/metrics/" + metricName + "/series/tags";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Series series = new Series(Mocks.entity(), metricName)
                .addSamples(Mocks.SAMPLE)
                .setTags(Mocks.TAGS);
        SeriesMethod.insertSeriesCheck(series);

        Response response = queryMetricSeriesTagsResponse(metricName, null, token);
        assertTrue(compareJsonString(Util.prettyPrint(TestUtil.toStringListMap(series.getTags())),
                response.readEntity(String.class)));
    }
}
