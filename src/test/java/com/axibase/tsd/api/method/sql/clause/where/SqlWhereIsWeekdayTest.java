package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class SqlWhereIsWeekdayTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME_1 = metric();
    private static final String METRIC_NAME_2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series seriesFirst = new Series(ENTITY_NAME, METRIC_NAME_1)
                .addSamples(Sample.ofDateInteger("2018-01-05T00:00:00Z", 5))
                .addSamples(Sample.ofDateInteger("2018-01-06T00:00:00Z", 6))
                .addSamples(Sample.ofDateInteger("2018-01-07T00:00:00Z", 7))
                .addSamples(Sample.ofDateInteger("2018-03-02T00:00:00Z", 2))
                .addSamples(Sample.ofDateInteger("2018-03-03T00:00:00Z", 3))
                .addSamples(Sample.ofDateInteger("2018-03-04T00:00:00Z", 4));

        final Series seriesSecond = new Series(ENTITY_NAME, METRIC_NAME_2)
                .addSamples(Sample.ofDateInteger("2018-01-05T00:00:00Z", 5))
                .addSamples(Sample.ofDateInteger("2018-01-06T00:00:00Z", 6))
                .addSamples(Sample.ofDateInteger("2018-01-07T00:00:00Z", 7))
                .addSamples(Sample.ofDateInteger("2018-03-02T00:00:00Z", 2))
                .addSamples(Sample.ofDateInteger("2018-03-03T00:00:00Z", 3))
                .addSamples(Sample.ofDateInteger("2018-03-04T00:00:00Z", 4));

        SeriesMethod.insertSeriesCheck(seriesFirst, seriesSecond);
    }

    private static String[][] expectedRows(final String[] results) {
        return Arrays.stream(results).map(ArrayUtils::toArray).toArray(String[][]::new);
    }

    private static Object[] testCase(final String params, final String... results) {
        return toArray(params, toArray(results));
    }

    @DataProvider
    public static Object[][] provideSelectQueries() {
        return toArray(
                testCase("is_weekday(time, 'RUS')",
                        "true", "false", "false", "true", "false", "false"),
                testCase("is_workday(time, 'RUS')",
                        "false", "false", "false", "true", "false", "false"),
                testCase("is_weekday(time, 'RUS') AND is_workday(time, 'RUS')",
                        "false", "false", "false", "true", "false", "false"),
                testCase("not is_weekday(time, 'RUS') AND is_workday(time, 'RUS')",
                        "false", "false", "false", "false", "false", "false"),
                testCase("is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')",
                        "true", "false", "false", "false", "false", "false"),
                testCase("not is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')",
                        "false", "true", "true", "false", "true", "true")
        );
    }

    @DataProvider
    public static Object[][] provideSelectWhereQueries() {
        return toArray(
                testCase("is_weekday(time, 'RUS')", toArray("true", "true")),
                testCase("is_workday(time, 'RUS')", toArray("true")),
                testCase("is_weekday(time, 'RUS') AND is_workday(time, 'RUS')", "true"),
                testCase("not is_weekday(time, 'RUS') AND is_workday(time, 'RUS')"),
                testCase("is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')", "true"),
                testCase("not is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')",
                        "true", "true", "true", "true")
        );
    }

    @Issue("5494")
    @Test(
            description = "Test the functions in SELECT WHERE clause",
            dataProvider = "provideSelectWhereQueries"
    )
    public void testSelectWhere(final String params, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\" WHERE %s", params, METRIC_NAME_1, params);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage =
                String.format("Fail to use boolean expression \"%s\" after SELECT and WHERE keywords", params);
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5494")
    @Test(
            description = "Test the functions in SELECT HAVING clause",
            dataProvider = "provideSelectWhereQueries"
    )
    public void testSelectHaving(final String params, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') HAVING %s",
                params, METRIC_NAME_1, params);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage =
                String.format("Fail to use boolean expression \"%s\" after SELECT and HAVING keywords", params);
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5494")
    @Test(
            description = "Test the functions in SELECT WHERE HAVING clause",
            dataProvider = "provideSelectWhereQueries"
    )
    public void testSelectWhereHaving(final String params, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\" WHERE %s GROUP BY PERIOD(1 day, 'UTC') HAVING %s",
                params, METRIC_NAME_1, params, params);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage =
                String.format("Fail to use boolean expression \"%s\" after SELECT, WHERE, HAVING keywords", params);
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5494")
    @Test(
            description = "Test the functions in SELECT clause",
            dataProvider = "provideSelectQueries"
    )
    public void testSelect(final String params, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\"", params, METRIC_NAME_1);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage =
                String.format("Fail to use boolean expression \"%s\" after SELECT keyword", params);
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5494")
    @Test(description = "Test WHERE and HAVING with every possible clause in one query")
    public void testWhereCombinedWithEveryClause() {
        final String query = String.format(
                "select is_workday(datetime, 'RUS'), is_weekday(dateadd(day, -1, datetime), 'RUS'), \n" +
                        "       is_weekday(dateadd(month, 1, datetime), 'RUS'), is_workday(datetime, 'RUS')\n" +
                        "from (\n" +
                        "    select datetime from \"%s\" t1 \n" +
                        "    join using entity \"%s\" t2\n" +
                        "    where datetime between date_parse('2018', 'yyyy') and date_parse('2019', 'yyyy')\n" +
                        "    and is_weekday(dateadd(day, -1, datetime), 'RUS')\n" +
                        "    and not is_weekday(datetime, 'RUS')\n" +
                        "    with row_number(t1.entity order by time desc) >= 1\n" +
                        "    group by period(1 day, LINEAR)\n" +
                        "    having not is_workday(datetime, 'RUS')\n" +
                        "    and is_weekday(dateadd(month, 1, datetime), 'RUS')\n" +
                        ")\n" +
                        "group by period(1 day, PREVIOUS, EXTEND)\n" +
                        "with time >= last_time - 1*MONTH\n" +
                        "order by is_weekday(datetime, 'ISR')\n" +
                        "limit 10\n", METRIC_NAME_1, METRIC_NAME_2);
        final String[][] expectedRows = toArray(
                toArray("false", "true", "true", "false"),
                toArray("false", "true", "true", "false")
        );

        final String assertMessage = "Fail to execute a big query containing boolean expressions" +
                " with combination of every possible clause";
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
