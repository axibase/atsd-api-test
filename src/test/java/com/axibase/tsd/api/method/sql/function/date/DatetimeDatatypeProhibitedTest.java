package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DatetimeDatatypeProhibitedTest extends SqlTest {
    private static final String TEST_PREFIX = "datetime-datatype-prohibited-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_DATETIME_VALUE = "2018-11-07T09:30:06.000Z";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE)
        );
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideProhibitedAggregateFunctions() {
        return new Object[][]{{"avg"}, {"correl"}, {"median"}, {"stddev"}, {"sum"}, {"wavg"}, {"wtavg"}};
    }

    @DataProvider
    public static Object[][] provideMathFunctions() {
        return new Object[][]{{"abs"}, {"ceil"}, {"floor"}, {"round"}, {"exp"}, {"ln"}, {"sqrt"}};
    }

    @DataProvider
    public static Object[][] provideMathFunctionsTwoParamaters() {
        return new Object[][]{{"mod"}, {"power"}, {"log"}};
    }

    @Issue("5757")
    @Test(dataProvider = "provideProhibitedAggregateFunctions", enabled = false)
    public void testProhibitedAggregationFunction(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        assertBadSqlRequest("Syntax error", sqlQuery);
    }

    @Issue("5757")
    @Test(dataProvider = "provideMathFunctions", enabled = false)
    public void testProhibitedMathFunction(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        assertBadSqlRequest("Syntax error", sqlQuery);
    }

    @Issue("5757")
    @Test(dataProvider = "provideMathFunctionsTwoParamaters", enabled = false)
    public void testProhibitedMathFunctionTwoParameters(String functionName) {
        String sqlQuery = String.format(
                "SELECT %s(datetime, 1) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE entity = '%s' %n",
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        assertBadSqlRequest("Syntax error", sqlQuery);
    }
}
