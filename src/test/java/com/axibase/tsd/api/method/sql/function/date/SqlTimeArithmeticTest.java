package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class SqlTimeArithmeticTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();
    private static final ZonedDateTime TWENTY_FIRST = ZonedDateTime.parse("2018-07-21T00:00:00Z");
    private static final ZonedDateTime TWENTY_SECOND = ZonedDateTime.parse("2018-07-22T00:00:00Z");
    private static final ZonedDateTime TWENTY_THIRD = ZonedDateTime.parse("2018-07-23T00:00:00Z");

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME)
                // Saturday (is not weekday, next day is not weekday)
                .addSamples(Sample.ofTimeInteger(TWENTY_FIRST.toEpochSecond() * 1000, 20))
                // Sunday (is not weekday, next day is weekday)
                .addSamples(Sample.ofTimeInteger(TWENTY_SECOND.toEpochSecond() * 1000, 20))
                // Monday (is weekday, next day is weekday)
                .addSamples(Sample.ofTimeInteger(TWENTY_THIRD.toEpochSecond() * 1000, 20));

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideParams() {
        return toArray(
                testCase("IS_WEEKDAY(time + 1000*60*60*24*1, 'RUS')", "false", "true", "true"),
                testCase("IS_WEEKDAY(time - 1000*60*60*24*1, 'RUS')", "true", "false", "false"),
                testCase("IS_WEEKDAY(time - 1000*60*60*24*2, 'RUS')", "true", "true", "false"),
                testCase("IS_WEEKDAY(time + 1000*60*60*24*2, 'RUS')", "true", "true", "true"),
                testCase("IS_WORKDAY(time + 1000*60*60*24*1, 'RUS')", "false", "true", "true"),
                testCase("IS_WORKDAY(time - 1000*60*60*24*1, 'RUS')", "true", "false", "false"),
                testCase("IS_WORKDAY(time - 1000*60*60*24*2, 'RUS')", "true", "true", "false"),
                testCase("IS_WORKDAY(time + 1000*60*60*24*2, 'RUS')", "true", "true", "true"),
                testCase("DATE_FORMAT(time + 1000*60*60*24*1, 'yyyy-MM-dd')",
                        zonedDateTimeToString(TWENTY_FIRST.plusSeconds(60 * 60 * 24)),
                        zonedDateTimeToString(TWENTY_SECOND.plusSeconds(60 * 60 * 24)),
                        zonedDateTimeToString(TWENTY_THIRD.plusSeconds(60 * 60 * 24))
                ),
                testCase("DATE_FORMAT(time - 1000*60*60*24*1, 'yyyy-MM-dd')",
                        zonedDateTimeToString(TWENTY_FIRST.minusSeconds(60 * 60 * 24)),
                        zonedDateTimeToString(TWENTY_SECOND.minusSeconds(60 * 60 * 24)),
                        zonedDateTimeToString(TWENTY_THIRD.minusSeconds(60 * 60 * 24))
                ),
                testCase("DATE_FORMAT(time - 1000*60*60*24*2, 'yyyy-MM-dd')",
                        zonedDateTimeToString(TWENTY_FIRST.minusSeconds(60 * 60 * 24 * 2)),
                        zonedDateTimeToString(TWENTY_SECOND.minusSeconds(60 * 60 * 24 * 2)),
                        zonedDateTimeToString(TWENTY_THIRD.minusSeconds(60 * 60 * 24 * 2))
                ),
                testCase("DATE_FORMAT(time + 1000*60*60*24*2, 'yyyy-MM-dd')",
                        zonedDateTimeToString(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2)),
                        zonedDateTimeToString(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2)),
                        zonedDateTimeToString(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2))
                ),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*1)",
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24).getDayOfMonth()),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24).getDayOfMonth()),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24).getDayOfMonth())
                ),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*1)",
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24).getDayOfMonth()),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24).getDayOfMonth()),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24).getDayOfMonth())
                ),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*2)",
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24 * 2).getDayOfMonth()),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24 * 2).getDayOfMonth()),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24 * 2).getDayOfMonth())
                ),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*2)",
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2).getDayOfMonth()),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2).getDayOfMonth()),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2).getDayOfMonth())
                ),
                testCase("MONTH(time + 1000*60*60*24*1)",
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2).getMonthValue())
                ),
                testCase("MONTH(time - 1000*60*60*24*1)",
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24).getMonthValue()),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24).getMonthValue()),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24).getMonthValue())
                ),
                testCase("MONTH(time - 1000*60*60*24*2)",
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24 * 2).getMonthValue())
                ),
                testCase("MONTH(time + 1000*60*60*24*2)",
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2).getMonthValue()),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2).getMonthValue())
                ),
                testCase("MONTH(time + 1000*60*60*24*20)",
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 20).getMonthValue()),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 20).getMonthValue()),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 20).getMonthValue())
                )
        );
    }

    private static String zonedDateTimeToString(final ZonedDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static Object[] testCase(final String params, final String... columns) {
        return toArray(params, toArray(columns));
    }

    @Issue("5490")
    @Test(
            description = "Test support of mathematical operators in the params",
            dataProvider = "provideParams"
    )
    public void testSqlFunctionTimeMathOperations(final String expression, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\"", expression, METRIC_NAME);
        final String[][] expectedRows = Arrays.stream(results)
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        final String assertMessage = String.format("Fail to calculate \"%s\"", expression);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @DataProvider
    public Object[][] provideClauses() {
        return toArray(
                testCase(String.format("SELECT time + 1000*60*60*24*1 FROM \"%s\"", METRIC_NAME),
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000)
                ),
                testCase(String.format("SELECT time - 1000*60*60*24*1 FROM \"%s\"", METRIC_NAME),
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000)
                ),
                testCase(String.format("SELECT time - 1000*60*60*24*2 FROM \"%s\"", METRIC_NAME),
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000)
                ),
                testCase(String.format("SELECT time + 1000*60*60*24*2 FROM \"%s\"", METRIC_NAME),
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time + 1000*60*60*24*1 FROM \"%s\" GROUP BY time + 1000*60*60*24*1",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24).toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time - 1000*60*60*24*1 FROM \"%s\" GROUP BY time - 1000*60*60*24*1",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24).toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time - 1000*60*60*24*2 FROM \"%s\" GROUP BY time - 1000*60*60*24*2",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.minusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time + 1000*60*60*24*2 FROM \"%s\" GROUP BY time + 1000*60*60*24*2",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.plusSeconds(60 * 60 * 24 * 2).toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" WHERE time + 1000*60*60*24*1 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" WHERE time - 1000*60*60*24*1 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" WHERE time - 1000*60*60*24*2 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" WHERE time + 1000*60*60*24*2 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') " +
                                "HAVING time + 1000*60*60*24*1 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') " +
                                "HAVING time - 1000*60*60*24*1 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') " +
                                "HAVING time - 1000*60*60*24*2 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                ),
                testCase(String.format(
                        "SELECT time FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') " +
                                "HAVING time + 1000*60*60*24*2 BETWEEN '2018-01-01' AND '2019-01-01'",
                        METRIC_NAME
                        ),
                        String.valueOf(TWENTY_FIRST.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_SECOND.toEpochSecond() * 1000),
                        String.valueOf(TWENTY_THIRD.toEpochSecond() * 1000)
                )
        );
    }

    @Issue("5492")
    @Test(
            description = "Test support of mathematical operators in clauses",
            dataProvider = "provideClauses"
    )
    public void testSqlClauseTimeMathOperations(final String clause, final String[] results) {
        final String[][] expectedRows = Arrays.stream(results)
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        final String assertMessage = String.format("Fail to calculate \"%s\"", clause);
        assertSqlQueryRows(assertMessage, expectedRows, clause);
    }
}
