package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlFunctionMathAbs extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();

        series1.setMetric(TEST_METRIC1_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1"),
                new Sample("2016-06-03T09:20:01.000Z", "2"),
                new Sample("2016-06-03T09:20:02.000Z", "3")
                )
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1));
    }

    /**
     * #3738
     */
    @Test
    public void testAbsSimple() {
        String sqlQuery = String.format(
                "SELECT avg(value), abs(avg(value)), abs(max(value)), abs(avg(value)) * abs(max(value)) FROM '%s'",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"2", "2", "3", "6"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Abs function gives wrong result");
    }

    /**
     * #3738
     */
    @Test
    public void testAbs() {
        String sqlQuery = String.format(
                "SELECT avg(value), abs(avg(value)), abs(max(value)), abs(avg(value)) * abs(max(value)), abs(max(value)*avg(value)) FROM '%s'",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"2", "2", "3", "6", "6"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Abs function gives wrong result");
    }

    /**
     * #3738
     */
    @Test
    public void testAbsСomplex() {
        String sqlQuery = String.format(
                "SELECT abs(abs(max(abs(value))) * -3 * abs(abs(max(abs(value)) * abs(delta(abs(value)) * count(value) * min(value)) * abs(avg(abs(value)))))) FROM '%s'",
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {"648"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Abs complex function with many compounds gives wrong result");
    }
}
