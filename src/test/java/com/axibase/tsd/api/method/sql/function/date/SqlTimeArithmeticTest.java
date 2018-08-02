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

import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class SqlTimeArithmeticTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME)
                // Saturday (is not weekday, next day is not weekday)
                .addSamples(Sample.ofDateInteger("2018-07-21T00:00:00Z", 20))
                // Sunday (is not weekday, next day is weekday)
                .addSamples(Sample.ofDateInteger("2018-07-22T00:00:00Z", 20))
                // Monday (is weekday, next day is weekday)
                .addSamples(Sample.ofDateInteger("2018-07-23T00:00:00Z", 20));

        SeriesMethod.insertSeriesCheck(series);
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
                //                                                  2018-07-21       2018-07-22       2018-07-23
                testCase("time + 1000*60*60*24*1", "1532131200000", "1532217600000", "1532304000000"),
                //                                                  2018-07-21       2018-07-22       2018-07-23
                testCase("time - 1000*60*60*24*1", "1532131200000", "1532217600000", "1532304000000"),
                //                                                  2018-07-21       2018-07-22       2018-07-23
                testCase("time - 1000*60*60*24*2", "1532131200000", "1532217600000", "1532304000000"),
                //                                                  2018-07-21       2018-07-22       2018-07-23
                testCase("time + 1000*60*60*24*2", "1532131200000", "1532217600000", "1532304000000")
        );
    }

    @DataProvider
    public Object[][] provideSelectClauses() {
        return toArray(
                //                                                  2018-07-22       2018-07-23       2018-07-24
                testCase("time + 1000*60*60*24*1", "1532217600000", "1532304000000", "1532390400000"),
                //                                                  2018-07-20       2018-07-21       2018-07-22
                testCase("time - 1000*60*60*24*1", "1532044800000", "1532131200000", "1532217600000"),
                //                                                  2018-07-19       2018-07-20       2018-07-21
                testCase("time - 1000*60*60*24*2", "1531958400000", "1532044800000", "1532131200000"),
                //                                                  2018-07-23       2018-07-24       2018-07-25
                testCase("time + 1000*60*60*24*2", "1532304000000", "1532390400000", "1532476800000")
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
        final String query = String.format("SELECT %s FROM \"%s\"", operation, METRIC_NAME);
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
        final String query = String.format("SELECT %s FROM \"%s\" GROUP BY %s", operation, METRIC_NAME, operation);
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
        final String query = String.format("SELECT time FROM \"%s\" WHERE %s BETWEEN '2018-01-01' AND '2019-01-01'",
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
                "SELECT time FROM \"%s\" GROUP BY PERIOD(1 day, 'UTC') HAVING %s BETWEEN '2018-01-01' AND '2019-01-01'",
                METRIC_NAME, operation);
        final String[][] expectedRows = expectedRows(results);
        final String assertMessage = String.format("Fail to calculate \"%s\" after SELECT and HAVING", operation);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
