package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class SqlWhereIsWeekdayTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series translatedSeries = new Series(ENTITY_NAME, METRIC_NAME);

        Stream.of(// Holiday in Russia, Tuesday (weekday)
                Sample.ofDateInteger("2018-01-02T00:00:00Z", 2),
                // Not a holiday in Russia, Sunday (day off)
                Sample.ofDateInteger("2018-07-01T00:00:00Z", 1)
        )
                .map(TestUtil::sampleToServerTimezone)
                .forEach(translatedSeries::addSamples);

        SeriesMethod.insertSeriesCheck(translatedSeries);
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
                testCase("is_weekday(time, 'RUS')", "true", "false"),
                testCase("is_workday(time, 'RUS')", "false", "false"),
                testCase("is_weekday(time, 'RUS') AND is_workday(time, 'RUS')",
                        "false", "false"),
                testCase("not is_weekday(time, 'RUS') AND is_workday(time, 'RUS')",
                        "false", "false"),
                testCase("is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')",
                        "true", "false"),
                testCase("not is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')",
                        "false", "true")
        );
    }

    @DataProvider
    public static Object[][] provideSelectWhereQueries() {
        return toArray(
                testCase("is_weekday(time, 'RUS')", "true"),
                testCase("is_workday(time, 'RUS')"),
                testCase("is_weekday(time, 'RUS') AND is_workday(time, 'RUS')"),
                testCase("not is_weekday(time, 'RUS') AND is_workday(time, 'RUS')"),
                testCase("is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')", "true"),
                testCase("not is_weekday(time, 'RUS') AND not is_workday(time, 'RUS')", "true")
        );
    }

    @Issue("5494")
    @Test(
            description = "Test the functions in SELECT WHERE clause",
            dataProvider = "provideSelectWhereQueries"
    )
    public void testSelectWhere(final String params, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\" WHERE %s", params, METRIC_NAME, params);
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
        final String query = String.format("SELECT %s FROM \"%s\" GROUP BY PERIOD(1 day) HAVING %s",
                params, METRIC_NAME, params);
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
        final String query = String.format("SELECT %s FROM \"%s\" WHERE %s GROUP BY PERIOD(1 day) HAVING %s",
                params, METRIC_NAME, params, params);
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
        final String query = String.format("SELECT %s FROM \"%s\"", params, METRIC_NAME);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage =
                String.format("Fail to use boolean expression \"%s\" after SELECT keyword", params);
        assertOkRequest(assertMessage, query);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
