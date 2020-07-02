package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class AggregationEmptyValueTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(entity(), METRIC_NAME1);
        series1.addSamples(
                Sample.ofDateInteger(Util.ISOFormat(1), -2),
                Sample.ofDateInteger(Util.ISOFormat(2), -1)
        );

        Series series2 = new Series(entity(), METRIC_NAME2);
        /* NaN value field */
        series2.addSamples(
                Sample.ofDateText(Util.ISOFormat(1), "text"),
                Sample.ofDateText(Util.ISOFormat(2), "text")
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("4000")
    @Test
    public void testMinMaxValueTimeNegavtives() {
        String sqlQuery = String.format(
                "SELECT min_value_time(value), max_value_time(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity",
                METRIC_NAME1
        );

        String[][] expextedRows = {{"1", "2"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with negatives",
                expextedRows, sqlQuery
        );
    }

    @Issue("4000")
    @Test
    public void testMinMaxValueTimeNaN() {
        String sqlQuery = String.format(
                "SELECT min_value_time(value), max_value_time(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity",
                METRIC_NAME2
        );

        String[][] expextedRows = {{"null", "null"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with NaNs",
                expextedRows, sqlQuery
        );
    }

    @Issue("4000")
    @Test
    public void testMinMaxValueTimeNull() {
        String sqlQuery = String.format(
                "SELECT min_value_time(text), max_value_time(text) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity",
                METRIC_NAME1
        );

        String[][] expextedRows = {{"null", "null"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with NaNs",
                expextedRows, sqlQuery
        );
    }

    @DataProvider(name = "aggregationFunctionFormats")
    Object[][] provideAgregationFunctionFormat() {
        return new Object[][]{
                {"min(%s)", "null"},
                {"max(%s)", "null"},
                {"avg(%s)", "null"},
                {"sum(%s)", "null"},
                {"last(%s)", "null"},
                {"first(%s)", "null"},
                {"stddev(%s)", "null"},
                {"delta(%s)", "NaN"},
                {"counter(%s)", "NaN"},
                {"correl(%1$s,%1$s)", "NaN"},
                {"median(%s)", "null"},
                {"percentile(90,%s)", "null"},
                {"wavg(%s)", "NaN"},
                {"wtavg(%s)", "NaN"},
        };
    }

    @Issue("4000")
    @Test(dataProvider = "aggregationFunctionFormats")
    public void testAggregationNaN(String functionFormat, String expected) {
        String functionWithArgument = String.format(functionFormat, "value");

        String sqlQuery = String.format(
                "SELECT %s " +
                        "FROM \"%s\" " +
                        "GROUP BY entity",
                functionWithArgument,
                METRIC_NAME2
        );

        String[][] expectedRows = {{expected}};

        assertSqlQueryRows("Incorrect result for one of aggregation functions with NaN", expectedRows, sqlQuery);
    }

    @Issue("4000")
    @Test(dataProvider = "aggregationFunctionFormats")
    public void testAggregationNull(String functionFormat, String expected) {
        String functionWithArgument = String.format(functionFormat, "text");

        String sqlQuery = String.format(
                "SELECT %s " +
                        "FROM \"%s\" " +
                        "GROUP BY entity",
                functionWithArgument,
                METRIC_NAME2
        );

        String[][] expectedRows = {{expected}};

        assertSqlQueryRows("Incorrect result for one of aggregation functions with null", expectedRows, sqlQuery);
    }
}
