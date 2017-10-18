package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Issue;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcpParsingTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @DataProvider
    public Object[][] provideTestCases() throws IOException {
        TcpParsingTestData[] testCases = TcpParsingTestLoader.loadFromResources();
        List<Object[]> casesList = new ArrayList<>();
        for (TcpParsingTestData testCase : testCases) {
            casesList.add(new Object[]{testCase});
        }
        casesList.add(new Object[]{TcpParsingTestLoader.getRandomData(false)});
        casesList.add(new Object[]{TcpParsingTestLoader.getRandomData(true)});
        Object[][] result = new Object[casesList.size()][];
        casesList.toArray(result);
        return result;
    }

    private void checkMetrics(List<String> jsonList) throws IOException, JSONException {
        for (String expectedJson : jsonList) {
            Metric expectedMetric = MAPPER.readValue(expectedJson, Metric.class);
            String metricName = expectedMetric.getName();
            Checker.check(new MetricCheck(expectedMetric));
            String actualJson = MetricMethod.queryMetric(metricName).readEntity(String.class);
            JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
        }
    }

    private void checkSeries(List<String> jsonList) throws IOException, JSONException {
        for (String expectedJson : jsonList) {
            Series expectedSeries = MAPPER.readValue(expectedJson, Series[].class)[0];
            SeriesQuery query = new SeriesQuery(
                    expectedSeries.getEntity(),
                    expectedSeries.getMetric(),
                    Util.MIN_QUERYABLE_DATE,
                    Util.MAX_QUERYABLE_DATE
            );
            Checker.check(new SeriesCheck(Collections.singletonList(expectedSeries)));
            String actualJson = SeriesMethod.querySeries(query).readEntity(String.class);
            JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
        }
    }

    @Issue("4411")
    @Test(dataProvider = "provideTestCases")
    public void testNetworkCommandParser(TcpParsingTestData data) throws Exception {
        TCPSender.send(data.getCommandsText());

        checkMetrics(data.getMetricsJsonList());
        checkSeries(data.getSeriesJsonList());
    }
}
