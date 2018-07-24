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

public class SqlIsWeekDayTest extends SqlTest {
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
    public static Object[][] provideTimeExpressionAndResult() {
        return new Object[][]{
                {"time + 1000*60*60*24*1", new Boolean[]{false, true, true}},
                {"time - 1000*60*60*24*1", new Boolean[]{true, false, false}},
                {"time - 1000*60*60*24*2", new Boolean[]{true, true, false}},
                {"time + 1000*60*60*24*2", new Boolean[]{true, true, true}},
        };
    }

    @Issue("5490")
    @Test(
            description = "Test support of mathematical operators in the first argument",
            dataProvider = "provideTimeExpressionAndResult"
    )
    public void testIsWeekDayFunctionAddOperator(final String timeExpression, final Boolean[] isWeekDay) {
        final String query = String.format("SELECT IS_WEEKDAY(%s, 'RUS') FROM \"%s\"", timeExpression, METRIC_NAME);
        final String[][] expectedRows = Arrays.stream(isWeekDay)
                .map(String::valueOf)
                .map(ArrayUtils::toArray)
                .toArray(String[][]::new);
        final String assertMessage = String.format("Fail to calculate \"%s\" as param of IS_WEEKDAY function", timeExpression);
        assertSqlQueryRows(assertMessage, expectedRows, query);
    }
}
