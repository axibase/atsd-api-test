package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class DatetimeAggregationTest extends SqlTest {
    private String TEST_METRIC;

    @BeforeTest
    public void prepareData() throws Exception {
        String entity = entity();
        TEST_METRIC = metric();
        Series series = new Series(entity, TEST_METRIC);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3)
        );

        insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideAggregators() {
        return new Object[][] {
                {"FIRST(datetime), FIRST(time)",
                        "2017-01-01T00:00:00.000Z",
                        String.valueOf(Util.getUnixTime("2017-01-01T00:00:00Z"))},

                {"LAST(datetime), LAST(time)",
                        "2017-01-03T00:00:00.000Z",
                        String.valueOf(Util.getUnixTime("2017-01-03T00:00:00Z"))},

                {"MAX(datetime), MAX(time)",
                        "2017-01-03T00:00:00.000Z",
                        String.valueOf(Util.getUnixTime("2017-01-03T00:00:00Z"))},

                {"MIN(datetime), MIN(time)",
                        "2017-01-01T00:00:00.000Z",
                        String.valueOf(Util.getUnixTime("2017-01-01T00:00:00Z"))},

                {"COUNT(datetime), COUNT(time)",
                        "3",
                        "3"}
        };
    }

    @Issue("5021")
    @Test(dataProvider = "provideAggregators", description = "Test datetime and time in aggregators")
    public void testDatetimeAggregators(String aggregators, String expectedDatetime, String expectedTime) {
        String sqlQuery = String.format("SELECT %s FROM \"%s\" GROUP BY entity", aggregators, TEST_METRIC);

        String[][] expectedRows = {
                {expectedDatetime, expectedTime}
        };

        assertSqlQueryRows("Incorrect datetime aggregation result",
                expectedRows,
                sqlQuery);
    }
}
