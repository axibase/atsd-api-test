package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.Version;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC = metric();
    private final String serverTimezoneOffset;

    // It is necessary to insert values with server timezone because interpolation works only by server local time
    public InterpolationBoundaryValuesTest() {
        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        int offsetMinutes = version.getDate().getTimeZone().getOffsetMinutes();
        int hours = offsetMinutes / 60;
        int minutes = Math.abs(offsetMinutes) % 60;
        serverTimezoneOffset = String.format("%+03d:%02d", hours, minutes);
    }

    private String replaceTimezone(String dateString) {
        return dateString.replace("Z", serverTimezoneOffset);
    }

    @BeforeClass
    public void prepareData() throws Exception {
        String entity = entity();

        Series series = new Series(entity, TEST_METRIC);

        series.addData(new Sample(replaceTimezone("2017-01-01T07:30:00.000Z"), 0));
        series.addData(new Sample(replaceTimezone("2017-01-01T10:30:00.000Z"), 1));
        series.addData(new Sample(replaceTimezone("2017-01-01T11:30:00.000Z"), 2));
        series.addData(new Sample(replaceTimezone("2017-01-01T12:30:00.000Z"), 3));

        series.addData(new Sample(replaceTimezone("2017-01-01T17:30:00.000Z"), 7));
        series.addData(new Sample(replaceTimezone("2017-01-01T18:30:00.000Z"), 8));
        series.addData(new Sample(replaceTimezone("2017-01-01T19:30:00.000Z"), 9));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolation() {
        String sqlQuery = String.format(
                replaceTimezone(
                    "SELECT value " +
                    "FROM '%s' " +
                    "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                    "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                    "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                    "ORDER BY datetime"),
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
                replaceTimezone(
                    "SELECT value " +
                    "FROM '%s' " +
                    "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                     "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                     "     OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z') " +
                     "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                     "ORDER BY datetime"),
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
    public void testInnerInterpolationWithSingleValueInPeriod() {
        String sqlQuery = String.format(
                replaceTimezone(
                    "SELECT value " +
                    "FROM '%s' " +
                    "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                    "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                    "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                    "ORDER BY datetime"),
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"3"},
                {"NaN"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolationWithNoValueInPeriod() {
        String sqlQuery = String.format(
                replaceTimezone(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime"),
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN"},
                {"8"},
                {"9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationEntirePeriod() {
        String sqlQuery = String.format(
                replaceTimezone(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime"),
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
                replaceTimezone(
                    "SELECT value " +
                    "FROM '%s' " +
                    "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                    "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                    "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                    "ORDER BY datetime"),
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
    public void testOuterInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                replaceTimezone(
                    "SELECT value " +
                    "FROM '%s' " +
                    "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                    "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                    "     OR datetime BETWEEN '2017-01-01T17:00:00Z' AND '2017-01-01T21:00:00Z') " +
                    "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                    "ORDER BY datetime"),
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

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithSingleValueInPeriod() {
        String sqlQuery = String.format(
                replaceTimezone(
                        "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime"),
                TEST_METRIC);

        String[][] expectedRows = {
                {"2"},
                {"3"},
                {"7"},
                {"8"},
                {"9"},
                {"9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithNoValueInPeriod() {
        String sqlQuery = String.format(
                replaceTimezone("SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime"),
                TEST_METRIC);

        String[][] expectedRows = {
                {"7"},
                {"8"},
                {"9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithOuterBoundValue() {
        String sqlQuery = String.format(
                replaceTimezone("SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T13:00:00Z' AND '2017-01-01T15:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T19:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime"),
                TEST_METRIC);

        String[][] expectedRows = {
                {"3"},
                {"3"},
                {"3"},
                {"7"},
                {"8"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInterpolationWithOverlappingPeriods() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T11:00:00Z' AND '2017-01-01T13:00:00Z' " +
                              "OR datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T14:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC);

        Response response = queryResponse(sqlQuery);

        String expectedErrorMessage =
                "Overlapping time intervals: " +
                        "2017-01-01T11:00:00Z - 2017-01-01T13:00:00Z " +
                        "and 2017-01-01T12:00:00Z - 2017-01-01T14:00:00Z";

        assertBadRequest("Incorrect overlapping time intervals error handling",
                expectedErrorMessage, response);
    }
}
