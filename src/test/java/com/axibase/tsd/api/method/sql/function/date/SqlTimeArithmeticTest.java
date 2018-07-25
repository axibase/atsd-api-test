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
                .addSamples(Sample.ofDateInteger("2018-07-21T12:54:00+0300", 20))
                // Sunday (is not weekday, next day is weekday)
                .addSamples(Sample.ofDateInteger("2018-07-22T12:54:00+0300", 20))
                // Monday (is weekday, next day is weekday)
                .addSamples(Sample.ofDateInteger("2018-07-23T12:54:00+0300", 20));

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideParams() {
        return new Object[][]{
                testCase("IS_WEEKDAY(time + 1000*60*60*24*1, 'RUS')", "false", "true", "true"),
                testCase("IS_WEEKDAY(time - 1000*60*60*24*1, 'RUS')", "true", "false", "false"),
                testCase("IS_WEEKDAY(time - 1000*60*60*24*2, 'RUS')", "true", "true", "false"),
                testCase("IS_WEEKDAY(time + 1000*60*60*24*2, 'RUS')", "true", "true", "true"),
                testCase("IS_WORKDAY(time + 1000*60*60*24*1, 'RUS')", "false", "true", "true"),
                testCase("IS_WORKDAY(time - 1000*60*60*24*1, 'RUS')", "true", "false", "false"),
                testCase("IS_WORKDAY(time - 1000*60*60*24*2, 'RUS')", "true", "true", "false"),
                testCase("IS_WORKDAY(time + 1000*60*60*24*2, 'RUS')", "true", "true", "true"),
                testCase("DATE_FORMAT(time + 1000*60*60*24*1, 'yyyy-MM-dd')", "2018-07-22", "2018-07-23", "2018-07-24"),
                testCase("DATE_FORMAT(time - 1000*60*60*24*1, 'yyyy-MM-dd')", "2018-07-20", "2018-07-21", "2018-07-22"),
                testCase("DATE_FORMAT(time - 1000*60*60*24*2, 'yyyy-MM-dd')", "2018-07-19", "2018-07-20", "2018-07-21"),
                testCase("DATE_FORMAT(time + 1000*60*60*24*2, 'yyyy-MM-dd')", "2018-07-23", "2018-07-24", "2018-07-25"),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*1)", "22", "23", "24"),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*1)", "20", "21", "22"),
                testCase("EXTRACT(DAY FROM time - 1000*60*60*24*2)", "19", "20", "21"),
                testCase("EXTRACT(DAY FROM time + 1000*60*60*24*2)", "23", "24", "25"),
                testCase("MONTH(time + 1000*60*60*24*1)", "7", "7", "7"),
                testCase("MONTH(time - 1000*60*60*24*1)", "7", "7", "7"),
                testCase("MONTH(time - 1000*60*60*24*2)", "7", "7", "7"),
                testCase("MONTH(time + 1000*60*60*24*2)", "7", "7", "7"),
                testCase("MONTH(time + 1000*60*60*24*20)", "8", "8", "8"),
        };
    }

    private static Object[] testCase(final String params, final String... columns) {
        return new Object[]{params, toArray(columns)};
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
}
