package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class IsWeekDayTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME)
                // Sunday
                .addSamples(Sample.ofDateInteger("2018-07-22T12:54:00+0300", 20))
                // Monday
                .addSamples(Sample.ofDateInteger("2018-07-23T12:54:00+0300", 20));

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("5490")
    @Test(description = "Test support of add operator in the first argument")
    public void testIsWeekDayFunctionAddOperator() {
        final String query = String.format("SELECT IS_WEEKDAY(time + 1000*60*60*24, 'RUS') FROM \"%s\"", METRIC_NAME);
        final String[][] expectedRows = {{"true", "true"}};
        assertSqlQueryRows("Fail to calculate next day in isWeekday()", expectedRows, query);
    }
}
