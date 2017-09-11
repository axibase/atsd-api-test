package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.DateRange;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.function.interpolate.Boundary;
import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class InterpolationBoundaryValuesTest extends SqlTest {
    private static final String TEST_METRIC_1 = metric();
    private static final String TEST_METRIC_2 = metric();
    private static final String HBASE_VERSION = Util.getHBaseVersion();

    private Sample[] calendarInterpolationTestSamples;
    private final ZoneId serverTimezone;

    public InterpolationBoundaryValuesTest() {
        Version version = VersionMethod.queryVersion().readEntity(Version.class);
        serverTimezone = ZoneId.of(version.getDate().getTimeZone().getName());
    }

    @BeforeClass
    public void prepareData() throws Exception {
        String entity = entity();
        Series series1 = new Series(entity, TEST_METRIC_1);
        series1.addSamples(
                Sample.ofDateInteger("1970-01-01T00:00:00Z", 0),
                Sample.ofDateInteger("1972-01-01T00:00:00Z", 2),
                Sample.ofDateInteger("1974-01-01T00:00:00Z", 4)
        );

        calendarInterpolationTestSamples = new Sample[]{
                Sample.ofDateInteger("2017-01-01T07:50:00.000Z", 0),
                Sample.ofDateInteger("2017-01-01T10:50:00.000Z", 1),
                Sample.ofDateInteger("2017-01-01T11:50:00.000Z", 2),
                Sample.ofDateInteger("2017-01-01T12:50:00.000Z", 3),
                Sample.ofDateInteger("2017-01-01T17:50:00.000Z", 7),
                Sample.ofDateInteger("2017-01-01T18:50:00.000Z", 8),
                Sample.ofDateInteger("2017-01-01T19:50:00.000Z", 9)
        };

        series1.addSamples(calendarInterpolationTestSamples);

        Series series2 = new Series(entity, TEST_METRIC_2);
        series2.addSamples(
                Sample.ofDateInteger("1971-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("1973-01-01T00:00:00Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    private String[][] generateCalendarInterpolationOutput(Boundary boundary, DateRange... ranges) {
        List<DateRange> serverLocalHourlyRanges = getHourlyRanges(serverTimezone, ranges);

        Map<DateRange, String> rangeValues = new TreeMap<>(Comparator.comparing(o -> o.startDate));

        for (int i = 0; i < serverLocalHourlyRanges.size(); i++) {
            DateRange hourlyRange = serverLocalHourlyRanges.get(i);
            DateRange previousRange = null;
            if (i > 0) {
                previousRange = serverLocalHourlyRanges.get(i - 1);
            }

            String value = searchHourlyRangeValue(hourlyRange, previousRange, boundary, calendarInterpolationTestSamples);

            if (value != null) {
                rangeValues.put(hourlyRange, value);
                continue;
            }

            final boolean isHbase1 = HBASE_VERSION.startsWith("1");

            if (previousRange == null) {
                if (isHbase1 && boundary == Boundary.OUTER) {
                    rangeValues.put(hourlyRange, "0");
                } else {
                    rangeValues.put(hourlyRange, "NaN");
                }
                continue;
            }


            if (ChronoUnit.HOURS.between(previousRange.endDate, hourlyRange.startDate) >= 1) {
                value = rangeValues.get(previousRange);
                if (isHbase1 && boundary == Boundary.OUTER && value != null) {
                    int offset = serverTimezone.getRules().getOffset(previousRange.endDate.toLocalDateTime()).getTotalSeconds();
                    if (offset / 3600 == offset / 3600.0) {
                        rangeValues.put(hourlyRange, value);
                    } else {
                        rangeValues.put(hourlyRange, "" + (Integer.parseInt(value) + 1));
                    }
                } else {
                    rangeValues.put(hourlyRange, "NaN");
                }
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

    private static List<DateRange> getHourlyRanges(ZoneId serverTimezone, DateRange... ranges) {
        List<DateRange> serverLocalHourlyRanges = new ArrayList<>();

        for (DateRange range : ranges) {
            ZonedDateTime startHour = range.startDate.withZoneSameInstant(serverTimezone).withMinute(0);

            if (serverTimezone.getRules().getOffset(range.startDate.toLocalDateTime()).getTotalSeconds() % 3600 != 0) {
                startHour = startHour.plusHours(1);
            }

            ZonedDateTime lastHour = range.endDate.withZoneSameInstant(serverTimezone).withMinute(0);

            lastHour = lastHour.plusHours(1);
            while (startHour.plusHours(1).compareTo(lastHour) <= 0) {
                serverLocalHourlyRanges.add(new DateRange(startHour, startHour.plusHours(1)));
                startHour = startHour.plusHours(1);
            }
        }

        return serverLocalHourlyRanges;
    }

    private static String searchHourlyRangeValue(
            DateRange hourlyRange,
            DateRange previousRange,
            Boundary boundary,
            Sample[] calendarInterpolationTestSeries) {
        String value = null;
        for (Sample sample : calendarInterpolationTestSeries) {
            ZonedDateTime sampleTime = sample.getZonedDateTime();
            if (boundary == Boundary.INNER) {
                if (previousRange == null) {
                    break;
                }

                if (ChronoUnit.HOURS.between(previousRange.endDate, hourlyRange.startDate) >= 1) {
                    break;
                }

                if (sampleTime.isBefore(previousRange.startDate)) {
                    continue;
                }
            }

            if (sampleTime.isAfter(hourlyRange.startDate)) {
                break;
            }

            if (ChronoUnit.HOURS.between(sampleTime, hourlyRange.startDate) >= 1) {
                continue;
            }

            value = String.valueOf(sample.getValue());
        }

        return value;
    }

    /**
     * #4069
     */
    @Test
    public void testInnerInterpolation() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T20:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4217
     */
    @Test
    public void testInnerInterpolationWithWithOuterBoundValueSinglePeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
                new DateRange("2017-01-01T08:00:00Z", "2017-01-01T13:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4217
     */
    @Test
    public void testInnerInterpolationWithWithOuterBoundValueDoublePeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                            "OR datetime BETWEEN '2017-01-01T14:00:00Z' AND '2017-01-01T16:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, INNER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.INNER,
                new DateRange("2017-01-01T08:00:00Z", "2017-01-01T13:00:00Z"));

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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T10:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T16:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "     AND (datetime BETWEEN '2017-01-01T09:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "     OR datetime BETWEEN '2017-01-01T17:00:00Z' AND '2017-01-01T21:00:00Z') " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T12:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T21:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T20:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T22:00:00Z' AND '2017-01-01T23:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
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
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T13:00:00Z' AND '2017-01-01T15:00:00Z' " +
                        "      OR datetime BETWEEN '2017-01-01T18:00:00Z' AND '2017-01-01T19:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
                new DateRange("2017-01-01T13:00:00Z", "2017-01-01T15:00:00Z"),
                new DateRange("2017-01-01T18:00:00Z", "2017-01-01T19:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4217
     */
    @Test
    public void testOuterInterpolationWithWithOuterBoundValueSinglePeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
                new DateRange("2017-01-01T08:00:00Z", "2017-01-01T13:00:00Z"));

        assertSqlQueryRows(
                "Incorrect inner interpolation with single value in period",
                expectedRows,
                sqlQuery);
    }

    /**
     * #4217
     */
    @Test
    public void testOuterInterpolationWithWithOuterBoundValueDoublePeriod() throws ParseException {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-01-01T08:00:00Z' AND '2017-01-01T13:00:00Z' " +
                            "OR datetime BETWEEN '2017-01-01T14:00:00Z' AND '2017-01-01T16:00:00Z' " +
                        "WITH INTERPOLATE(1 HOUR, PREVIOUS, OUTER, NAN) " +
                        "ORDER BY datetime",
                TEST_METRIC_1);

        String[][] expectedRows = generateCalendarInterpolationOutput(
                Boundary.OUTER,
                new DateRange("2017-01-01T08:00:00Z", "2017-01-01T13:00:00Z"));

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
                        "FROM \"%s\" " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
                        "FROM \"%s\" m1 " +
                        "JOIN \"%s\" m2 " +
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
