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

public class SqlTimeArithmetic extends SqlTest {
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
    public static Object[][] provideTimeExpression() {
        return new Object[][]{
                {"IS_WEEKDAY(time + 1000*60*60*24*1, 'RUS')", new String[]{"false", "true", "true"}, "IS_WEEKDAY"},
                {"IS_WEEKDAY(time - 1000*60*60*24*1, 'RUS')", new String[]{"true", "false", "false"}, "IS_WEEKDAY"},
                {"IS_WEEKDAY(time - 1000*60*60*24*2, 'RUS')", new String[]{"true", "true", "false"}, "IS_WEEKDAY"},
                {"IS_WEEKDAY(time + 1000*60*60*24*2, 'RUS')", new String[]{"true", "true", "true"}, "IS_WEEKDAY"},
                {"IS_WORKDAY(time + 1000*60*60*24*1, 'RUS')", new String[]{"false", "true", "true"}, "IS_WORKDAY"},
                {"IS_WORKDAY(time - 1000*60*60*24*1, 'RUS')", new String[]{"true", "false", "false"}, "IS_WORKDAY"},
                {"IS_WORKDAY(time - 1000*60*60*24*2, 'RUS')", new String[]{"true", "true", "false"}, "IS_WORKDAY"},
                {"IS_WORKDAY(time + 1000*60*60*24*2, 'RUS')", new String[]{"true", "true", "true"}, "IS_WORKDAY"},
                {"DATE_FORMAT(time + 1000*60*60*24*1, 'yyyy-MM-dd')", new String[]{"2018-07-22", "2018-07-23", "2018-07-24"}, "DATE_FORMAT"},
                {"DATE_FORMAT(time - 1000*60*60*24*1, 'yyyy-MM-dd')", new String[]{"2018-07-20", "2018-07-21", "2018-07-22"}, "DATE_FORMAT"},
                {"DATE_FORMAT(time - 1000*60*60*24*2, 'yyyy-MM-dd')", new String[]{"2018-07-19", "2018-07-20", "2018-07-21"}, "DATE_FORMAT"},
                {"DATE_FORMAT(time + 1000*60*60*24*2, 'yyyy-MM-dd')", new String[]{"2018-07-23", "2018-07-24", "2018-07-25"}, "DATE_FORMAT"},
                {"EXTRACT(DAY FROM time + 1000*60*60*24*1)", new String[]{"22", "23", "24"}, "EXTRACT"},
                {"EXTRACT(DAY FROM time - 1000*60*60*24*1)", new String[]{"20", "21", "22"}, "EXTRACT"},
                {"EXTRACT(DAY FROM time - 1000*60*60*24*2)", new String[]{"19", "20", "21"}, "EXTRACT"},
                {"EXTRACT(DAY FROM time + 1000*60*60*24*2)", new String[]{"23", "24", "25"}, "EXTRACT"},
                {"MONTH(time + 1000*60*60*24*1)", new String[]{"7", "7", "7"}, "MONTH"},
                {"MONTH(time - 1000*60*60*24*1)", new String[]{"7", "7", "7"}, "MONTH"},
                {"MONTH(time - 1000*60*60*24*2)", new String[]{"7", "7", "7"}, "MONTH"},
                {"MONTH(time + 1000*60*60*24*2)", new String[]{"7", "7", "7"}, "MONTH"},
                {"MONTH(time + 1000*60*60*24*20)", new String[]{"8", "8", "8"}, "MONTH"},
        };
    }

    @Issue("5490")
    @Test(
            description = "Test support of mathematical operators in the params",
            dataProvider = "provideTimeExpression"
    )
    public void testSqlFunctionTimeMathOperations(final String timeExpression, final String[] results, final String function) {
        final String query = String.format("SELECT %s FROM \"%s\"", timeExpression, METRIC_NAME);
        final String[][] expectedRows = Arrays.stream(results)
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        final String assertMessage = String.format("Fail to calculate \"%s\" as param of %s function", timeExpression, function);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
