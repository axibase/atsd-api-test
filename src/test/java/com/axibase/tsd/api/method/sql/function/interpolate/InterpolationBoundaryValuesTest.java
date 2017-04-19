package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entity = entity();

        Series series = new Series(entity, TEST_METRIC);

        series.addData(new Sample("2017-01-01T07:30:00.000Z", 0));
        series.addData(new Sample("2017-01-01T10:30:00.000Z", 1));
        series.addData(new Sample("2017-01-01T11:30:00.000Z", 2));
        series.addData(new Sample("2017-01-01T12:30:00.000Z", 3));

        series.addData(new Sample("2017-01-01T17:30:00.000Z", 7));
        series.addData(new Sample("2017-01-01T18:30:00.000Z", 8));
        series.addData(new Sample("2017-01-01T19:30:00.000Z", 9));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolation() {
        String sqlQuery = String.format(
                "SELECT value " +
                "FROM '%s' " +
                "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                "ORDER BY datetime",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"NaN"},
                {"1"},
                {"2"},
                {"3"},
                {"NaN"},
                {"NaN"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                "SELECT value " +
                "FROM '%s' " +
                "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                 "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                 "     OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z') " +
                 "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                 "ORDER BY datetime",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"NaN"},
                {"1"},
                {"2"},
                {"3"},
                {"NaN"},
                {"NaN"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationEntirePeriod() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"1"},
                {"2"},
                {"3"},
                {"NaN"},
                {"NaN"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows("Incorrect outer interpolation by entire period", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithOuterValue() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"NaN"},
                {"1"},
                {"2"},
                {"3"},
                {"NaN"},
                {"NaN"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows("Incorrect outer interpolation by entire period", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T17:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"NaN"},
                {"1"},
                {"2"},
                {"3"},
                {"NaN"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }
}
