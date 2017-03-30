package com.axibase.tsd.api.method.sql.function.rows;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class LagLeadTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(entity(), METRIC_NAME);

        series.addData(new TextSample("2017-01-01T12:00:00.000Z", "a"));
        series.addData(new TextSample("2017-01-02T12:00:00.000Z", "b"));
        series.addData(new TextSample("2017-01-03T12:00:00.000Z", "c"));
        series.addData(new TextSample("2017-01-04T12:00:00.000Z", "d"));
        series.addData(new TextSample("2017-01-05T12:00:00.000Z", "e"));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * 4032
     */
    @Test
    public void testLagLeadInSelectClause() {
        String sqlQuery = String.format(
                "SELECT text, lag(text), lead(text) FROM '%s'",
                METRIC_NAME
        );

        String[][] expectedRows = new String[][] {
                {"a", "null",    "b"},
                {"b",    "a",    "c"},
                {"c",    "b",    "d"},
                {"d",    "c",    "e"},
                {"e",    "d", "null"}
        };

        assertSqlQueryRows("Wrong result for LAG/LEAD functions in SELECT clause", expectedRows, sqlQuery);
    }
}
