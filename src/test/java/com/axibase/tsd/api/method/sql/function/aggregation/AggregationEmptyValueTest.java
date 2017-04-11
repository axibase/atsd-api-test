package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class AggregationEmptyValueTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(entity(), METRIC_NAME1);
        series1.addData(new Sample(Util.ISOFormat(1), "-2"));
        series1.addData(new Sample(Util.ISOFormat(2), "-1"));

        Series series2 = new Series(entity(), METRIC_NAME2);
        /* NaN value field */
        series2.addData(new TextSample(Util.ISOFormat(1), "text"));
        series2.addData(new TextSample(Util.ISOFormat(2), "text"));

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    /**
     * #4000
     */
    @Test
    public void testMinMaxValueTimeNegavtives() {
        String sqlQuery = String.format(
                "SELECT min_value_time(value), max_value_time(value) " +
                        "FROM '%s' " +
                        "GROUP BY entity",
                METRIC_NAME1
        );

        String[][] expextedRows = {{"1", "2"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with negatives",
                expextedRows, sqlQuery
        );
    }

    /**
     * #4000
     */
    @Test
    public void testMinMaxValueTimeNaN() {
        String sqlQuery = String.format(
                "SELECT min_value_time(value), max_value_time(value) " +
                        "FROM '%s' " +
                        "GROUP BY entity",
                METRIC_NAME2
        );

        String[][] expextedRows = {{"null", "null"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with NaNs",
                expextedRows, sqlQuery
        );
    }

    /**
     * #4000
     */
    @Test
    public void testMinMaxValueTimeNull() {
        String sqlQuery = String.format(
                "SELECT min_value_time(text), max_value_time(text) " +
                        "FROM '%s' " +
                        "GROUP BY entity",
                METRIC_NAME1
        );

        String[][] expextedRows = {{"null", "null"}};

        assertSqlQueryRows(
                "Incorrect result for min/max_value_time with NaNs",
                expextedRows, sqlQuery
        );
    }

    private void testAggregationByColumn(String column, String message) {
        String[] testFunctions = {
                "min", "max", "avg", "sum", "last", "first",
                "stddev", "delta", "counter", "correl", "median", "percentile",
                "wavg"
        };
        String[][] expectedRows = {new String[testFunctions.length]};

        StringBuilder testColumns = new StringBuilder();
        for (int i = 0; i < testFunctions.length; i++) {
            if (i > 0)
                testColumns.append(", ");
            testColumns.append(testFunctions[i]);
            switch (testFunctions[i]) {
                case "correl":
                    testColumns.append(String.format("(%s,%s)", column, column));
                    break;
                case "percentile":
                    testColumns.append(String.format("(90.5,%s)", column));
                    break;
                default:
                    testColumns.append(String.format("(%s)", column));
                    break;
            }
            expectedRows[0][i] = "NaN";
        }

        String sqlQuery = String.format(
                "SELECT %s " +
                        "FROM '%s' " +
                        "GROUP BY entity",
                testColumns.toString(),
                METRIC_NAME2
        );

        assertSqlQueryRows(message, expectedRows, sqlQuery);
    }

    /**
     * #4000
     */
    @Test
    public void testAggregationNaN() {
        testAggregationByColumn("value", "Incorrect result for one of aggregation functions with NaN");
    }

    /**
     * #4000
     */
    @Test
    public void testAggregationNull() {
        testAggregationByColumn("text", "Incorrect result for one of aggregation functions with null");
    }
}
