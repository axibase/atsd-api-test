package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.apache.commons.lang3.ArrayUtils.toArray;


public class SqlJoinOfSubQueriesTest extends SqlTest {
    private static final String FIRST_QUERY_METRIC = Mocks.metric();
    private static final String SECOND_QUERY_METRIC = Mocks.metric();


    @BeforeClass
    public void initData() throws Exception {
        final String entity = Mocks.entity();
        SeriesMethod.insertSeriesCheck(Arrays.asList(
                new Series()
                        .setMetric(FIRST_QUERY_METRIC)
                        .setEntity(entity)
                        .addSamples(Sample.ofDateInteger("2018-08-06T12:23:36.489Z", 1)),
                new Series()
                        .setMetric(SECOND_QUERY_METRIC)
                        .setEntity(entity)
                        .addSamples(Sample.ofDateInteger("2018-08-06T12:23:36.589Z", 2))
        ));
    }

    @Issue("5107")
    @Test(dataProvider = "provideJoinOnSubQueriesData")
    public void testCorrectOuterJoin(final TestData testData) {
        final String sqlQuery = String.format(
                "SELECT co.value, po.value %n" +
                        "FROM (%s) co %n" +
                        "OUTER JOIN (%s) po %n" +
                        "ON po.entity = co.entity AND po.time = co.time",
                testData.firstQuery, testData.secondQuery);
        assertOkRequest("Can't perform request with valid subqueries syntax", sqlQuery);
    }

    @DataProvider
    private Object[][] provideJoinOnSubQueriesData() {
        return toArray(
                testCase(
                        String.format("SELECT value, entity, time %n" +
                                        "FROM \"%s\"",
                                FIRST_QUERY_METRIC),
                        String.format("SELECT value, entity, time %n" +
                                        "FROM \"%s\"",
                                SECOND_QUERY_METRIC)
                ),
                testCase(
                        String.format("SELECT value, entity, time %n" +
                                "FROM \"%s\"", FIRST_QUERY_METRIC),
                        String.format("SELECT value, entity, time %nFROM \"%s\"", SECOND_QUERY_METRIC)
                )
        );
    }

    private Object[] testCase(final String firstQuery, final String secondQuery) {
        return toArray(
                TestData.of(firstQuery, secondQuery)
        );
    }


    @AllArgsConstructor(staticName = "of")
    @ToString
    private static class TestData {
        private String firstQuery;
        private String secondQuery;
    }
}
