package com.axibase.tsd.api.method.sql.function.date;

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

public class SqlTimeArithmeticTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series translatedSeries = new Series(ENTITY_NAME, METRIC_NAME);

        Stream.of(// Saturday (is not weekday, next day is not weekday)
                Sample.ofDateInteger("2018-07-21T00:00:00Z", 21),
                // Sunday (is not weekday, next day is weekday)
                Sample.ofDateInteger("2018-07-22T00:00:00Z", 22),
                // Monday (is weekday, next day is weekday)
                Sample.ofDateInteger("2018-07-23T00:00:00Z", 23)
        )
                .map(TestUtil::sampleToServerTimezone)
                .forEach(translatedSeries::addSamples);

        SeriesMethod.insertSeriesCheck(translatedSeries);
    }

    private static Object[] testCase(final String param, final String... results) {
        return toArray(param, toArray(results));
    }

    private static String[][] expectedRows(final String[] results) {
        return Arrays.stream(results).map(ArrayUtils::toArray).toArray(String[][]::new);
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
                        "2018-07-22", "2018-07-23", "2018-07-24"),
                testCase("DATE_FORMAT(time - 1000*60*60*24*1, 'yyyy-MM-dd')",
                        "2018-07-20", "2018-07-21", "2018-07-22"),
                testCase("DATE_FORMAT(time - 1000*60*60*24*2, 'yyyy-MM-dd')",
                        "2018-07-19", "2018-07-20", "2018-07-21"),
                testCase("DATE_FORMAT(time + 1000*60*60*24*2, 'yyyy-MM-dd')",
                        "2018-07-23", "2018-07-24", "2018-07-25"),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*1)", "22", "23", "24"),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*1)", "20", "21", "22"),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*2)", "19", "20", "21"),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*2)", "23", "24", "25"),
                testCase("MONTH(time + 1000*60*60*24*1)", "7", "7", "7"),
                testCase("MONTH(time - 1000*60*60*24*1)", "7", "7", "7"),
                testCase("MONTH(time - 1000*60*60*24*2)", "7", "7", "7"),
                testCase("MONTH(time + 1000*60*60*24*2)", "7", "7", "7"),
                testCase("MONTH(time + 1000*60*60*24*20)", "8", "8", "8")
        );
    }

    @DataProvider
    public static Object[][] provideSelectBetweenClauses() {
        return toArray(
                testCase("time + 1000*60*60*24*1", "2018-07-21", "2018-07-22", "2018-07-23"),
                testCase("time - 1000*60*60*24*1", "2018-07-21", "2018-07-22", "2018-07-23"),
                testCase("time - 1000*60*60*24*2", "2018-07-21", "2018-07-22", "2018-07-23"),
                testCase("time + 1000*60*60*24*2", "2018-07-21", "2018-07-22", "2018-07-23")
        );
    }

    @DataProvider
    public static Object[][] provideSelectClauses() {
        return toArray(
                testCase("time + 1000*60*60*24*1", "2018-07-22", "2018-07-23", "2018-07-24"),
                testCase("time - 1000*60*60*24*1", "2018-07-20", "2018-07-21", "2018-07-22"),
                testCase("time - 1000*60*60*24*2", "2018-07-19", "2018-07-20", "2018-07-21"),
                testCase("time + 1000*60*60*24*2", "2018-07-23", "2018-07-24", "2018-07-25")
        );
    }

    @Issue("5490")
    @Test(
            description = "Test support of mathematical operators in the params",
            dataProvider = "provideParams"
    )
    public void testSqlFunctionTimeMathOperations(final String expression, final String[] results) {
        final String query = String.format("SELECT %s FROM \"%s\"", expression, METRIC_NAME);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT", expression);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5492")
    @Test(
            description = "Test support of mathematical operators in select clause",
            dataProvider = "provideSelectClauses"
    )
    public void testSqlSelectClauseTimeMathOperations(final String operation, final String[] results) {
        final String query = String.format("SELECT date_format(%s, 'yyyy-MM-dd') FROM \"%s\"", operation, METRIC_NAME);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT", operation);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5492")
    @Test(
            description = "Test support of mathematical operators in select group by clause",
            dataProvider = "provideSelectClauses"
    )
    public void testSqlSelectGroupClauseTimeMathOperations(final String operation, final String[] results) {
        final String query = String.format("SELECT date_format(%s, 'yyyy-MM-dd') FROM \"%s\" GROUP BY %s", operation, METRIC_NAME, operation);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT and GROUP BY", operation);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5492")
    @Test(
            description = "Test support of mathematical operators in select where between clause",
            dataProvider = "provideSelectBetweenClauses"
    )
    public void testSqlSelectWhereBetweenClauseTimeMathOperations(final String operation, final String results[]) {
        final String query = String.format("SELECT date_format(time, 'yyyy-MM-dd') FROM \"%s\" WHERE %s " +
                        "BETWEEN date_parse('2018', 'yyyy') AND date_parse('2019', 'yyyy')",
                METRIC_NAME, operation);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT and WHERE", operation);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }

    @Issue("5492")
    @Test(
            description = "Test support of mathematical operators in select having between clause",
            dataProvider = "provideSelectBetweenClauses"
    )
    public void testSqlSelectHavingBetweenClauseTimeMathOperations(final String operation, final String[] results) {
        final String query = String.format(
                "SELECT date_format(time, 'yyyy-MM-dd') FROM \"%s\" GROUP BY PERIOD(1 day) HAVING %s " +
                        "BETWEEN date_parse('2018', 'yyyy') AND date_parse('2019', 'yyyy')",
                METRIC_NAME, operation);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT and HAVING", operation);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
