package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Util.getServerTimeZone;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.temporal.ChronoField.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class SqlWithTimezoneTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME = metric();
    private static final ZoneId[] timeZones = toArray(
            getServerTimeZone().toZoneId(), ZoneId.of("Asia/Kathmandu"), ZoneId.of("Europe/Moscow"),
            ZoneId.of("UTC"), ZoneId.of("Pacific/Marquesas")
    );
    private static final List<Sample> SAMPLES = Sample.withOffset(
            ChronoUnit.MINUTES, 20,
            ZonedDateTime.parse("2017-12-01T00:00:00Z", ISO_DATE_TIME),
            ZonedDateTime.parse("2018-07-01T00:00:00Z", ISO_DATE_TIME)
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME)
                .addSamples(SAMPLES);

        SeriesMethod.insertSeriesCheck(series);
    }

    private static Stream<String> between(final LocalDateTime from, final LocalDateTime to, final ZoneId timeZone) {
        final ZonedDateTime start = from.atZone(timeZone);
        final ZonedDateTime end = to.atZone(timeZone);
        return getStream(timeZone).filter((time) -> time.compareTo(start) >= 0)
                .filter((time) -> time.compareTo(end) <= 0)
                .map((time) -> time.format(ISO_LOCAL_DATE_TIME));
    }

    private static <T> Stream<String> modifyDate(final Function<ZonedDateTime, T> function, final ZoneId timeZone) {
        return getStream(timeZone).map(function).map(String::valueOf);
    }

    private static Stream<ZonedDateTime> getStream(final ZoneId timeZone) {
        return SAMPLES.stream().map((sample) -> sample.getZonedDateTime().withZoneSameInstant(timeZone));
    }

    private static long getMillisOnDayStart(final ZonedDateTime dateTime) {
        return dateTime.with(SECOND_OF_DAY, 0).toEpochSecond() * 1000;
    }

    @DataProvider
    public static Object[][] provideSelectExpressions() {
        Object[][] results = null;
        for (final ZoneId timeZone : timeZones) {
            results = ArrayUtils.addAll(results, toArray(
                    testCase("date_format(time, 'yyyy-MM-ddTHH:mm:ss')", timeZone,
                            modifyDate((time) -> time.format(ISO_LOCAL_DATE_TIME), timeZone)
                    ),
                    testCase("date_format(dateadd(minute, -15, time), 'yyyy-MM-ddTHH:mm:ss')", timeZone,
                            modifyDate((time) -> time.plusMinutes(-15).format(ISO_LOCAL_DATE_TIME), timeZone)
                    ),
                    testCase("minute(time)", timeZone, modifyDate(ZonedDateTime::getMinute, timeZone)),
                    testCase("hour(time)", timeZone, modifyDate(ZonedDateTime::getHour, timeZone)),
                    testCase("day(time)", timeZone, modifyDate(ZonedDateTime::getDayOfMonth, timeZone)),
                    testCase("extract (month from time)", timeZone,
                            modifyDate(ZonedDateTime::getMonthValue, timeZone)
                    ),
                    testCase("is_weekday(time, 'USA')", timeZone,
                            modifyDate(SqlWithTimezoneTest::isWeekday, timeZone)
                    ),
                    testCase("date_format(date_parse('2018-02-02T15:30:00', 'yyyy-MM-ddTHH:mm:ss'), " +
                            "'yyyy-MM-ddTHH:mm:ss')", timeZone, SAMPLES.stream().map((sample) -> "2018-02-02T15:30:00")
                    ),
                    testCase("current_day", timeZone, SAMPLES.stream().map((sample) -> ZonedDateTime.now(timeZone))
                            .map(SqlWithTimezoneTest::getMillisOnDayStart)
                            .map(String::valueOf)
                    ),
                    testCase("next_week", timeZone, SAMPLES.stream().map((sample) -> ZonedDateTime.now(timeZone))
                            .map((zoned) -> zoned.plusWeeks(1))
                            .map((zoned) -> zoned.with(DAY_OF_WEEK, 1))
                            .map(SqlWithTimezoneTest::getMillisOnDayStart)
                            .map(String::valueOf)
                    )
            ));
        }

        return results;
    }

    @DataProvider
    public static Object[][] provideWhereExpressions() {
        final LocalDateTime detailedStart = LocalDateTime.parse("2017-12-31T03:45:21");
        final LocalDateTime detailedEnd = LocalDateTime.parse("2018-01-03T14:35:42");
        final LocalDateTime dayStart = LocalDateTime.parse("2018-02-25T00:00:00");
        final LocalDateTime dayEnd = LocalDateTime.parse("2018-03-05T00:00:00");
        final LocalDateTime monthStart = LocalDateTime.parse("2017-12-01T00:00:00");
        final LocalDateTime monthEnd = LocalDateTime.parse("2018-02-01T00:00:00");
        final LocalDateTime yearStart = LocalDateTime.parse("2017-01-01T00:00:00");
        final LocalDateTime yearEnd = LocalDateTime.parse("2018-01-01T00:00:00");
        Object[][] results = null;
        for (final ZoneId timeZone : timeZones) {
            results = ArrayUtils.addAll(results, toArray(
                    testCase("datetime BETWEEN date_parse('2017-12-31T03:45:21', 'yyyy-MM-ddTHH:mm:ss') " +
                                    "AND date_parse('2018-01-03T14:35:42', 'yyyy-MM-ddTHH:mm:ss')", timeZone,
                            between(detailedStart, detailedEnd, timeZone)
                    ),
                    testCase("datetime BETWEEN '2018-02-25' AND '2018-03-05'", timeZone,
                            between(dayStart, dayEnd, timeZone)
                    ),
                    testCase("datetime BETWEEN '2017-12' AND '2018-02'", timeZone,
                            between(monthStart, monthEnd, timeZone)
                    ),
                    testCase("datetime BETWEEN '2017' AND '2018'", timeZone,
                            between(yearStart, yearEnd, timeZone)
                    ))
            );
        }
        return results;
    }

    @DataProvider
    public static Object[][] provideGroupBy() {
        Object[][] results = null;
        for (final ZoneId timeZone : timeZones) {
            results = ArrayUtils.addAll(results, toArray(
                    testCase("1 day", timeZone, SAMPLES.stream().collect(
                            groupingBy(sample -> sample.getZonedDateTime().withZoneSameInstant(timeZone)
                                            .with(SECOND_OF_DAY, 0),
                                    summingInt((sample) -> sample.getValue().intValue()))
                            ).entrySet().stream().sorted(comparing(Map.Entry::getKey))
                                    .map(Map.Entry::getValue).map(String::valueOf)
                    ),
                    testCase("1 week", timeZone, SAMPLES.stream().collect(
                            groupingBy(sample -> sample.getZonedDateTime().withZoneSameInstant(timeZone)
                                            .with(DAY_OF_WEEK, 1)
                                            .with(SECOND_OF_DAY, 0),
                                    summingInt((sample) -> sample.getValue().intValue()))
                            ).entrySet().stream().sorted(comparing(Map.Entry::getKey))
                                    .map(Map.Entry::getValue).map(String::valueOf)
                    ),
                    testCase("1 month", timeZone, SAMPLES.stream().collect(
                            groupingBy(sample -> sample.getZonedDateTime().withZoneSameInstant(timeZone)
                                            .with(DAY_OF_MONTH, 1)
                                            .with(SECOND_OF_DAY, 0),
                                    summingInt((sample) -> sample.getValue().intValue()))
                            ).entrySet().stream().sorted(comparing(Map.Entry::getKey))
                                    .map(Map.Entry::getValue).map(String::valueOf)
                    )
            ));
        }
        return results;
    }


    @DataProvider
    public static Object[][] provideInterpolatedData() {
        return Arrays.stream(timeZones).map((timeZone) -> {
            final String oldQuery = String.format(
                    "SELECT value FROM \"%s\" WITH INTERPOLATE(10 MINUTE, LINEAR, INNER, false, CALENDAR, '%s')",
                    METRIC_NAME, timeZone.getId()
            );
            final String newQuery = String.format(
                    "SELECT value FROM \"%s\" WITH INTERPOLATE(10 MINUTE, LINEAR, INNER, false, CALENDAR) WITH TIMEZONE = '%s'",
                    METRIC_NAME, timeZone.getId()
            );
            final Stream<String> expected = SqlMethod.queryTable(oldQuery).getRows()
                    .stream().map((list) -> String.join(", ", list));
            return testCase(newQuery, expected);
        }).toArray(Object[][]::new);
    }

    @Issue("5542")
    @Test(
            dataProvider = "provideInterpolatedData",
            description = "Test interpolation using 'WITH TIMEZONE"
    )
    public void testInterpolation(final String query, final String[][] expectedRows) {
        assertSqlQueryRows("Fail to interpolate using 'WITH TIMEZONE'", expectedRows, query);
    }

    private static Object[] testCase(final String query, final ZoneId timeZone, final Stream<String> results) {
        final String[][] expectedRows = results
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        return toArray(query, timeZone.getId(), expectedRows);
    }

    private static Object[] testCase(final String query, final Stream<String> results) {
        final String[][] expectedRows = results
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        return toArray(query, expectedRows);
    }

    private static boolean isWeekday(final ZonedDateTime dateTime) {
        return !dateTime.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !dateTime.getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }

    @Issue("5542")
    @Test(
            description = "Test same where condition in different timezones",
            dataProvider = "provideWhereExpressions"
    )
    public void testWhereExpression(final String expression, final String timeZone, final String[][] expectedRows) {
        final String condition = String.format("SELECT date_format(time, 'yyyy-MM-ddTHH:mm:ss') FROM \"%s\"\n" +
                "WHERE %s WITH TIMEZONE = \'%s\'", METRIC_NAME, expression, timeZone
        );
        final String assertMessage =
                String.format("Failed to filter rows using WITH TIMEZONE in WHERE \"%s\"", expression);
        assertSqlQueryRows(assertMessage, expectedRows, condition);
    }

    @Issue("5542")
    @Test(
            description = "Test same select expressions in different timezones",
            dataProvider = "provideSelectExpressions"
    )
    public void testSelectExpressions(final String expression, final String timeZone, final String[][] expectedRows) {
        final String query = String.format("SELECT %s FROM \"%s\" WITH TIMEZONE = \'%s\'",
                expression, METRIC_NAME, timeZone
        );
        final String assertMessage =
                String.format("Failed to query rows using WITH TIMEZONE in SELECT \"%s\"", expression);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5542")
    @Test(
            dataProvider = "provideGroupBy",
            description = "Test that GROUP BY can filter using timezone"
    )
    public void testGroupBy(final String period, final String timeZone, final String[][] expectedRows) {
        final String query = String.format("SELECT SUM(value) FROM \"%s\" GROUP BY PERIOD(%s) WITH TIMEZONE = \"%s\"",
                METRIC_NAME, period, timeZone
        );
        assertSqlQueryRows("Fail to group by date using WITH TIMEZONE", expectedRows, query);
    }
}
