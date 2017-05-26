package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.version.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import java.text.ParseException;
import java.util.*;

import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC_1 = metric();
    private static final String TEST_METRIC_2 = metric();

    private static final List<SeriesItem> calendarInterpolationTestSeries = new ArrayList<>();
    private final DateTimeZone serverTimezone;

    public InterpolationBoundaryValuesTest() {
        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        serverTimezone = DateTimeZone.forID(version.getDate().getTimeZone().getName());
    }

    @BeforeClass
    public void prepareData() throws Exception {
        String entity = entity();
        Series series1 = new Series(entity, TEST_METRIC_1);
        series1.addSamples(
                new Sample("1970-01-01T00:00:00Z", 0),
                new Sample("1972-01-01T00:00:00Z", 2),
                new Sample("1974-01-01T00:00:00Z", 4)
        );

        calendarInterpolationTestSeries.addAll(
                Arrays.asList(
                        new SeriesItem("2017-01-01T07:50:00.000Z", 0),
                        new SeriesItem("2017-01-01T10:50:00.000Z", 1),
                        new SeriesItem("2017-01-01T11:50:00.000Z", 2),
                        new SeriesItem("2017-01-01T12:50:00.000Z", 3),
                        new SeriesItem("2017-01-01T17:50:00.000Z", 7),
                        new SeriesItem("2017-01-01T18:50:00.000Z", 8),
                        new SeriesItem("2017-01-01T19:50:00.000Z", 9))
        );

        for (SeriesItem seriesItem : calendarInterpolationTestSeries) {
            series1.addSamples(new Sample(seriesItem.date.toString(), seriesItem.value));
        }

        Series series2 = new Series(entity, TEST_METRIC_2);
        series2.addSamples(
                new Sample("1971-01-01T00:00:00Z", 1),
                new Sample("1973-01-01T00:00:00Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    private static class SeriesItem {
        private static final DateTimeFormatter isoDateFormat = ISODateTimeFormat.dateTimeParser();

        public final DateTime date;
        public final int value;

        SeriesItem(String date, int value) {
            this.date = isoDateFormat.parseDateTime(date);
            this.value = value;
        }
    }

    private static class DateRange {
        private static final DateTimeFormatter isoDateFormat = ISODateTimeFormat.dateTimeParser();

        private final DateTime startDate;
        private final DateTime endDate;

        private DateRange(String startDate, String endDate) throws ParseException {
            this.startDate = isoDateFormat.parseDateTime(startDate);
            this.endDate = isoDateFormat.parseDateTime(endDate);
        }

        private DateRange(DateTime startDate, DateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public int hashCode() {
            return startDate.hashCode() ^ endDate.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) return false;
            if (other == this) return true;
            if (!(other instanceof DateRange))return false;
            DateRange otherRange = (DateRange)other;
            return this.startDate.equals(otherRange.startDate) &&
                    this.endDate.equals(otherRange.endDate);
        }
    }

    private String[][] generateCalendarInterpolationOutput(String mode, DateRange... ranges) {
        List<DateRange> serverLocalHourlyRanges = new ArrayList<>();

        for (DateRange range : ranges) {
            DateTime startHour = range.startDate.toDateTime(serverTimezone).withMinuteOfHour(0);
            if (serverTimezone.getOffset(startHour) % (1000 * 3600) != 0) {
                startHour = startHour.plusHours(1);
            }

            DateTime lastHour = range.endDate.toDateTime(serverTimezone).withMinuteOfHour(0);
            lastHour = lastHour.plusHours(1);
            while (startHour.plusHours(1).compareTo(lastHour) <= 0) {
                serverLocalHourlyRanges.add(new DateRange(startHour, startHour.plusHours(1)));
                startHour = startHour.plusHours(1);
            }
        }

        Map<DateRange, String> rangeValues = new TreeMap<>(new Comparator<DateRange>() {
            @Override
            public int compare(DateRange o1, DateRange o2) {
                return o1.startDate.compareTo(o2.startDate);
            }
        });

        for (int i = 0; i < serverLocalHourlyRanges.size(); i++) {
            DateRange hourlyRange = serverLocalHourlyRanges.get(i);
            String value = null;
            for (SeriesItem seriesItem : calendarInterpolationTestSeries) {
                if (mode.equals("INNER")) {
                    if (i == 0) {
                        break;
                    }

                    DateRange previousRange = serverLocalHourlyRanges.get(i - 1);
                    Period timeDifferenceToPrevRange = new Period(previousRange.endDate, hourlyRange.startDate);
                    if (timeDifferenceToPrevRange.getHours() >= 1) {
                        break;
                    }

                    if (seriesItem.date.isBefore(previousRange.startDate)) {
                        continue;
                    }
                }

                if (seriesItem.date.isAfter(hourlyRange.startDate)) {
                    break;
                }

                Period timeDifference = new Period(seriesItem.date, hourlyRange.startDate);
                if (timeDifference.getHours() >= 1) {
                    continue;
                }

                value = String.valueOf(seriesItem.value);
            }

            if (value != null) {
                rangeValues.put(hourlyRange, value);
                continue;
            }

            if (i == 0) {
                rangeValues.put(hourlyRange, "NaN");
                continue;
            }

            DateRange previousRange = serverLocalHourlyRanges.get(i - 1);
            Period timeDifferenceToPrevRange = new Period(previousRange.endDate, hourlyRange.startDate);
            if (timeDifferenceToPrevRange.getHours() >= 1) {
                rangeValues.put(hourlyRange, "NaN");
                continue;
            }

            value = rangeValues.get(previousRange);
            if (value == null) {
                rangeValues.put(hourlyRange, "NaN");
                continue;
            }

            rangeValues.put(hourlyRange, value);
        }

        int resultLength = rangeValues.size();
        String[][] result = new String[resultLength][1];
        int i = 0;
        for (Map.Entry<DateRange, String> entry : rangeValues.entrySet()) {
            result[i][0] = entry.getValue();
            i++;
        }

        return result;
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolation() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "INNER",
                new DateRange("2017-01-01T09:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T16:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows("Incorrect inner interpolation", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolationWithPeriodIntersection() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "INNER",
                new DateRange("2017-01-01T09:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T16:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolationWithSingleValueInPeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "INNER",
                new DateRange("2017-01-01T12:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolationWithNoValueInPeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "INNER",
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T20:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationEntirePeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "OUTER",
                new DateRange("2017-01-01T10:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T16:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows("Incorrect outer interpolation by entire period", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithPeriodIntersection() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T17:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "OUTER",
                new DateRange("2017-01-01T09:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T17:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows("Incorrect inner interpolation with period intersection", expectedRows, sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithSingleValueInPeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "OUTER",
                new DateRange("2017-01-01T12:00:00Z", "2017-01-01T13:00:00Z"),
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T21:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithNoValueInPeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "OUTER",
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T20:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4069
     */
    @Test
    public void testOuterInterpolationWithOuterBoundValue() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE datetime BETWEEN '2017-01-01T13:00:00Z' AND '2017-01-01T15:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T19:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                "OUTER",
                new DateRange("2017-01-01T13:00:00Z", "2017-01-01T15:00:00Z"),
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T19:00:00Z"));

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
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NONE, CALENDAR)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNanCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NAN, CALENDAR)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateExtendCalendar() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, CALENDAR)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNoneStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NONE, START_TIME)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateNanStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, NAN, START_TIME)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithMinDateExtendStartTime() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1970-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, START_TIME)",
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

    /**
     * #4181
     */
    @Test
    public void testJoinWithDateBeforeMin() {
        String sqlQuery = String.format(
                "SELECT m1.value, m2.value " +
                        "FROM '%s' m1 " +
                        "JOIN '%s' m2 " +
                        "WHERE m1.datetime >= '1969-01-01T00:00:00Z' AND m1.datetime < '1975-01-01T00:00:00Z' " +
                        "WITH INTERPOLATE(1 YEAR, PREVIOUS, INNER, EXTEND, START_TIME)",
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
