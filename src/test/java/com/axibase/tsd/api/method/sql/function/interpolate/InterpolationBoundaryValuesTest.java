package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC_1 = metric();
    private static final String TEST_METRIC_2 = metric();
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
        Registry.Entity.register(entity);
        
        Registry.Metric.register(TEST_METRIC_1);
        Registry.Metric.register(TEST_METRIC_2);

        Series series1 = new Series();
        series1.setEntity(entity);
        series1.setMetric(TEST_METRIC_1);

        series1.addData(new Sample("1970-01-01T00:00:00Z", 0));
        series1.addData(new Sample("1972-01-01T00:00:00Z", 2));
        series1.addData(new Sample("1974-01-01T00:00:00Z", 4));

        series1.addData(new Sample(replaceTimezone("2017-01-01T07:30:00.000Z"), 0));
        series1.addData(new Sample(replaceTimezone("2017-01-01T10:30:00.000Z"), 1));
        series1.addData(new Sample(replaceTimezone("2017-01-01T11:30:00.000Z"), 2));
        series1.addData(new Sample(replaceTimezone("2017-01-01T12:30:00.000Z"), 3));

        series1.addData(new Sample(replaceTimezone("2017-01-01T17:30:00.000Z"), 7));
        series1.addData(new Sample(replaceTimezone("2017-01-01T18:30:00.000Z"), 8));
        series1.addData(new Sample(replaceTimezone("2017-01-01T19:30:00.000Z"), 9));

        Series series2 = new Series();
        series2.setEntity(entity);
        series2.setMetric(TEST_METRIC_2);

        series2.addData(new Sample("1971-01-01T00:00:00Z", 1));
        series2.addData(new Sample("1973-01-01T00:00:00Z", 3));

        SeriesMethod.insertSeriesCheck(series1, series2);
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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

        String[][] expectedRows = {
                {"NaN"},
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
    public void testInnerInterpolationWithNoValueInPeriod() {
        String sqlQuery = String.format(
                replaceTimezone(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime"),
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

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
                TEST_METRIC_1);

        Response response = queryResponse(sqlQuery);

        String expectedErrorMessage =
                "Overlapping time intervals: " +
                        "2017-01-01T11:00:00Z - 2017-01-01T13:00:00Z " +
                        "and 2017-01-01T12:00:00Z - 2017-01-01T14:00:00Z";

        assertBadRequest("Incorrect overlapping time intervals error handling",
                expectedErrorMessage, response);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNoneCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NONE, CALENDAR)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNanCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NAN, CALENDAR)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1970-01-01T00:00:00.000Z", "0", "1970-01-01T00:00:00.000Z", "NaN"},
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateExtendCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, CALENDAR)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1970-01-01T00:00:00.000Z", "0", "1970-01-01T00:00:00.000Z", "1"},
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNoneStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NONE, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNanStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NAN, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1970-01-01T00:00:00.000Z", "0", "1970-01-01T00:00:00.000Z", "NaN"},
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateExtendStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1970-01-01T00:00:00.000Z", "0", "1970-01-01T00:00:00.000Z", "1"},
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4181
     */
    @Test
    public void testJoinWithDateBeforeMin() {
        String sqlQuery = String.format(
                "SELECT m1.datetime, m1.value, m2.datetime, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1969-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"1970-01-01T00:00:00.000Z", "0", "1970-01-01T00:00:00.000Z", "1"},
                {"1971-01-01T00:00:00.000Z", "0", "1971-01-01T00:00:00.000Z", "1"},
                {"1972-01-01T00:00:00.000Z", "2", "1972-01-01T00:00:00.000Z", "1"},
                {"1973-01-01T00:00:00.000Z", "2", "1973-01-01T00:00:00.000Z", "3"},
                {"1974-01-01T00:00:00.000Z", "4", "1974-01-01T00:00:00.000Z", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
