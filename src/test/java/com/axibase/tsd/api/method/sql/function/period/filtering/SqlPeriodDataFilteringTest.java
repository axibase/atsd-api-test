package com.axibase.tsd.api.method.sql.function.period.filtering;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.TestUtil.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class SqlPeriodDataFilteringTest extends SqlTest {
    private static final String TEST_METRIC_NAME = TestNames.metric();
    private static final String TEST_ENTITY_NAME = TestNames.entity();


    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addData(new Sample("2017-01-01T00:00:00.000Z", 1));
        series.addData(new Sample("2017-01-01T00:01:00.000Z", 2));
        series.addData(new Sample("2017-01-01T01:00:00.000Z", 3));
        series.addData(new Sample("2017-01-01T02:00:00.000Z", 4));
        series.addData(new Sample("2017-01-01T02:01:00.000Z", 5));
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #2967, #4146
     */
    @Test
    public void testPeriodFilterLeftBound() {
        final String sqlQuery = String.format(
                "SELECT datetime, sum(value), count(value) FROM '%s' " +
                        "WHERE datetime > '2017-01-01T00:00:00.000Z'" +
                        "GROUP BY PERIOD(1 HOUR)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-01-01T00:00:00.000Z", "2", "1"},
                {"2017-01-01T01:00:00.000Z", "3", "1"},
                {"2017-01-01T02:00:00.000Z", "9", "2"},
        };

        assertSqlQueryRows("Wrong result if period is not in range (left bound)", expectedRows, sqlQuery);
    }

    /**
     * #2967, #4146
     */
    @Test
    public void testPeriodFilterRightBound() {
        final String sqlQuery = String.format(
                "SELECT datetime, sum(value), count(value) FROM '%s' " +
                        "WHERE datetime <= '2017-01-01T02:00:00.000Z'" +
                        "GROUP BY PERIOD(1 HOUR)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-01-01T00:00:00.000Z", "3", "2"},
                {"2017-01-01T01:00:00.000Z", "3", "1"},
                {"2017-01-01T02:00:00.000Z", "4", "1"},
        };

        assertSqlQueryRows("Wrong result if period is not in range (right bound)", expectedRows, sqlQuery);
    }

    /**
     * #2967, #4146
     */
    @Test
    public void testPeriodFilterBothBounds() {
        final String sqlQuery = String.format(
                "SELECT datetime, sum(value), count(value) FROM '%s' " +
                        "WHERE datetime > '2017-01-01T00:00:00.000Z' AND datetime <= '2017-01-01T02:00:00.000Z' " +
                        "GROUP BY PERIOD(1 HOUR)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-01-01T00:00:00.000Z", "2", "1"},
                {"2017-01-01T01:00:00.000Z", "3", "1"},
                {"2017-01-01T02:00:00.000Z", "4", "1"},
        };
        assertSqlQueryRows("Wrong result if period is not in range (both bounds)", expectedRows, sqlQuery);
    }

    /**
     * #2967, #4146
     */
    @Test
    public void testPeriodNonFilterBothBounds() {
        final String sqlQuery = String.format(
                "SELECT datetime, sum(value), count(value) FROM '%s' " +
                        "WHERE datetime >= '2017-01-01T00:00:00.000Z' AND datetime <= '2017-01-01T02:01:00.000Z' " +
                        "GROUP BY PERIOD(1 HOUR)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-01-01T00:00:00.000Z", "3", "2"},
                {"2017-01-01T01:00:00.000Z", "3", "1"},
                {"2017-01-01T02:00:00.000Z", "9", "2"},
        };
        assertSqlQueryRows("Wrong result if all periods are in range", expectedRows, sqlQuery);
    }
}
