package com.axibase.tsd.api.method.sql.function.math;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlFunctionMathAbsTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME);
        series1.addSamples(
                new Sample("2016-06-03T09:20:00.000Z", 1),
                new Sample("2016-06-03T09:20:01.000Z", 2),
                new Sample("2016-06-03T09:20:02.000Z", 3)
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series1));
    }

    @DataProvider
    public Object[][] provideTestsDataForAbsTest() {
        return new Object[][]{
                {
                        "avg(value)",
                        "2"
                },
                {
                        "max(value)",
                        "3"
                },
                {
                        "abs(avg(value)) * abs(max(value))",
                        "6"
                },
                {
                        "max(value) * avg(value)",
                        "6"
                },
                {
                        "abs(max(abs(value))) * -3 * abs(abs(max(abs(value)) * abs(delta(abs(value)) * " +
                        "count(value) * min(value)) * abs(avg(abs(value)))))",
                        "324"
                }
        };
    }

    /**
     * #3738
     */
    @Test(dataProvider = "provideTestsDataForAbsTest")
    public void testAbsWithAggregateExpressionsInside(String query, String value) {
        String sqlQuery = String.format(
                "SELECT abs( %s ) FROM \"%s\"",
                query, TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {value}
        };

        String assertMessage = String.format("Wrong result of following expression %s", query);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }
}
