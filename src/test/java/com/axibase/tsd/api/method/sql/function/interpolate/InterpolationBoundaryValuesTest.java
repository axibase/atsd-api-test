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

        series.addData(new Sample("2012-01-01T00:00:00.000Z", 1));
        series.addData(new Sample("2012-01-02T00:00:00.000Z", 2));
        series.addData(new Sample("2012-01-03T00:00:00.000Z", 3));

        series.addData(new Sample("2012-01-07T00:00:00.000Z", 7));
        series.addData(new Sample("2012-01-08T00:00:00.000Z", 8));
        series.addData(new Sample("2012-01-09T00:00:00.000Z", 9));

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
                "WHERE datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-03T00:00:00Z' " +
                "      OR datetime BETWEEN '2012-01-06T00:00:00Z' AND '2012-01-10T00:00:00Z' " +
                "WITH INTERPOLATE(1 DAY, PREVIOUS, INNER, NAN)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"}, {"1"}, {"2"}, {"3"}, {"NaN"}, {"7"}, {"8"}, {"9"}, {"9"}
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
                "WHERE datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-10T00:00:00Z' " +
                 "     AND (datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-03T00:00:00Z' " +
                 "     OR datetime BETWEEN '2012-01-06T00:00:00Z' AND '2012-01-10T00:00:00Z') " +
                 "WITH INTERPOLATE(1 DAY, PREVIOUS, INNER, NAN)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"}, {"1"}, {"2"}, {"3"}, {"NaN"}, {"7"}, {"8"}, {"9"}, {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolation() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-03T00:00:00Z' " +
                        "      OR datetime BETWEEN '2012-01-06T00:00:00Z' AND '2012-01-10T00:00:00Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, NAN)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"}, {"1"}, {"2"}, {"3"}, {"3"}, {"7"}, {"8"}, {"9"}, {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-10T00:00:00Z' " +
                        "     AND (datetime BETWEEN '2011-12-31T00:00:00Z' AND '2012-01-03T00:00:00Z' " +
                        "     OR datetime BETWEEN '2012-01-06T00:00:00Z' AND '2012-01-10T00:00:00Z') " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, NAN)",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"}, {"1"}, {"2"}, {"3"}, {"3"}, {"7"}, {"8"}, {"9"}, {"9"}
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }
}
