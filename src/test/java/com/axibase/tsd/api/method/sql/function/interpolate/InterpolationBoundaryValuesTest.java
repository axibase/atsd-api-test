package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC_1 = metric();
    private static final String TEST_METRIC_2 = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        String entity = entity();
        Series series1 = new Series(entity, TEST_METRIC_1);

        series1.addSamples(
                Sample.ofDateInteger("1970-01-01T00:00:00Z", 0),
                Sample.ofDateInteger("1972-01-01T00:00:00Z", 2),
                Sample.ofDateInteger("1974-01-01T00:00:00Z", 4),

                Sample.ofDateInteger("2017-01-01T07:50:00Z", 0),
                Sample.ofDateInteger("2017-01-01T10:50:00Z", 1),
                Sample.ofDateInteger("2017-01-01T11:50:00Z", 2),
                Sample.ofDateInteger("2017-01-01T12:50:00Z", 3),
                Sample.ofDateInteger("2017-01-01T17:50:00Z", 7),
                Sample.ofDateInteger("2017-01-01T18:50:00Z", 8),
                Sample.ofDateInteger("2017-01-01T19:50:00Z", 9)
        );

        Series series2 = new Series(entity, TEST_METRIC_2);
        series2.addSamples(
                Sample.ofDateInteger("1971-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("1973-01-01T00:00:00Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testInnerInterpolation() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T09:00:00.000Z", "NaN"},
                {"2017-01-01T10:00:00.000Z", "NaN"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T16:00:00.000Z", "NaN"},
                {"2017-01-01T17:00:00.000Z", "NaN"},
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect inner interpolation", expectedRows, sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testInnerInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T09:00:00.000Z", "NaN"},
                {"2017-01-01T10:00:00.000Z", "NaN"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T16:00:00.000Z", "NaN"},
                {"2017-01-01T17:00:00.000Z", "NaN"},
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testInnerInterpolationWithSingleValueInPeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T12:00:00.000Z", "NaN"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T18:00:00.000Z", "NaN"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testInnerInterpolationWithNoValueInPeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T18:00:00.000Z", "NaN"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4217")
    @Issue("4814")
    @Test
    public void testInnerInterpolationWithWithOuterBoundValueSinglePeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T08:00:00.000Z", "NaN"},
                {"2017-01-01T09:00:00.000Z", "NaN"},
                {"2017-01-01T10:00:00.000Z", "NaN"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4217")
    @Issue("4814")
    @Test
    public void testInnerInterpolationWithWithOuterBoundValueDoublePeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "OR datetime BETWEEN '2017-01-01T14:00:00Z' AND '2017-01-01T16:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T08:00:00.000Z", "NaN"},
                {"2017-01-01T09:00:00.000Z", "NaN"},
                {"2017-01-01T10:00:00.000Z", "NaN"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testOuterInterpolationEntirePeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T10:00:00.000Z", "0"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T16:00:00.000Z", "3"},
                {"2017-01-01T17:00:00.000Z", "3"},
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect outer interpolation by entire period", expectedRows, sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithPeriodIntersection() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T17:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T09:00:00.000Z", "0"},
                {"2017-01-01T10:00:00.000Z", "0"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T17:00:00.000Z", "3"},
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithSingleValueInPeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"},
                {"2017-01-01T21:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithNoValueInPeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
                {"2017-01-01T20:00:00.000Z", "9"}
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4069")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithOuterBoundValue() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T13:00:00Z' AND '2017-01-01T15:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T19:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T18:00:00.000Z", "7"},
                {"2017-01-01T19:00:00.000Z", "8"},
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4217")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithWithOuterBoundValueSinglePeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T08:00:00.000Z", "0"},
                {"2017-01-01T09:00:00.000Z", "0"},
                {"2017-01-01T10:00:00.000Z", "0"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4217")
    @Issue("4814")
    @Test
    public void testOuterInterpolationWithWithOuterBoundValueDoublePeriod() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "OR datetime BETWEEN '2017-01-01T14:00:00Z' AND '2017-01-01T16:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NaN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = new String[][] {
                {"2017-01-01T08:00:00.000Z", "0"},
                {"2017-01-01T09:00:00.000Z", "0"},
                {"2017-01-01T10:00:00.000Z", "0"},
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"}

        };

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    @Issue("4069")
    @Issue("4821")
    @Issue("4814")
    @Test
    public void testInterpolationWithOverlappingPeriods() {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T11:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "OR datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T14:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, VALUE NAN, CALENDAR, 'UTC') " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = {
                {"2017-01-01T11:00:00.000Z", "1"},
                {"2017-01-01T12:00:00.000Z", "2"},
                {"2017-01-01T13:00:00.000Z", "3"},
                {"2017-01-01T14:00:00.000Z", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateNoneCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, FALSE, CALENDAR, 'UTC')",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateNanCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, VALUE NAN, CALENDAR, 'UTC')",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "NaN"},
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateExtendCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, TRUE, CALENDAR, 'UTC')",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateNoneStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, FALSE, START_TIME, 'UTC')",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateNanStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, FALSE, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithMinDateExtendStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, TRUE, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"},
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("4181")
    @Issue("4814")
    @Test
    public void testJoinWithDateBeforeMin() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
                        "WHERE m1.datetime >= '1969-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, TRUE, START_TIME)",
                TEST_METRIC_1,
                TEST_METRIC_2);

        String[][] expectedRows = {
                {"0", "1"},
                {"0", "1"},
                {"2", "1"},
                {"2", "3"},
                {"4", "3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
