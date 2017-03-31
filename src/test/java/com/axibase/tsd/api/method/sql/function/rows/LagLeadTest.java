package com.axibase.tsd.api.method.sql.function.rows;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class LagLeadTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(entity(), METRIC_NAME);

        series.addData(new Sample("2017-01-01T12:00:00.000Z", BigDecimal.valueOf(1), "a"));
        series.addData(new Sample("2017-01-02T12:00:00.000Z", BigDecimal.valueOf(2), "a"));
        series.addData(new Sample("2017-01-03T12:00:00.000Z", BigDecimal.valueOf(4), "a"));
        series.addData(new Sample("2017-01-04T12:00:00.000Z", BigDecimal.valueOf(7), "b"));
        series.addData(new Sample("2017-01-05T12:00:00.000Z", BigDecimal.valueOf(11), "b"));
        series.addData(new Sample("2017-01-06T12:00:00.000Z", BigDecimal.valueOf(16), "b"));
        series.addData(new Sample("2017-01-07T12:00:00.000Z", BigDecimal.valueOf(23), "c"));
        series.addData(new Sample("2017-01-08T12:00:00.000Z", BigDecimal.valueOf(31), "c"));
        series.addData(new Sample("2017-01-09T12:00:00.000Z", BigDecimal.valueOf(40), "c"));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4032
     */
    @Test
    public void testLagLeadInSelectClause() {
        String sqlQuery = String.format(
                "SELECT value, lag(value), lead(value) FROM '%s'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1", "null",    "2"},
                {"2",    "1",    "4"},
                {"4",    "2",    "7"},
                {"7",    "4",   "11"},
                {"11",   "7",   "16"},
                {"16",  "11",   "23"},
                {"23",  "16",   "31"},
                {"31",  "23",   "40"},
                {"40",  "31", "null"}
        };

        assertSqlQueryRows("Wrong result for LAG/LEAD functions in SELECT clause", expectedRows, sqlQuery);
    }

    /**
     * #4032
     */
    @Test
    public void testLagLeadInSelectExpression() {
        String sqlQuery = String.format(
                "SELECT isnull(lag(sum(value)) - sum(value), 0), " +
                       "isnull(lead(sum(value)) - sum(value), 0) " +
                "FROM '%s' " +
                "GROUP BY text",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"0",   "27"},
                {"-27", "60"},
                {"-60",  "0"},
        };

        assertSqlQueryRows("Wrong result for LAG/LEAD functions in SELECT expression", expectedRows, sqlQuery);
    }
}
