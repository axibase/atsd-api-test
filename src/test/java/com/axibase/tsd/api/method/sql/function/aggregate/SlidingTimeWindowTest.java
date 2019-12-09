package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import lombok.Data;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

public class SlidingTimeWindowTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final int[] DATA = {1, 2, 3, 4};

    /**
     * @return 1st value - function name 2nd value - function transforming number of row in result set to a function result
     */
    @DataProvider
    public Object[][] aggregateFunctions() {
        return new Object[][]{
                {SqlFunction.of("AVG", this::sampleValueAtRow)},
                //{"CORREL"},
                {SqlFunction.of("COUNT", rowNumber -> "1")},
                {SqlFunction.of("COUNTER", rowNumber -> "NaN")},
                //{"COVAR"},
                {SqlFunction.of("DELTA", rowNumber -> "NaN")},
                {SqlFunction.of("FIRST", this::sampleValueAtRow)},
                {SqlFunction.of("LAST", this::sampleValueAtRow)},
                {SqlFunction.of("MAX", this::sampleValueAtRow)},
                //{"MAX_VALUE_TIME"},
                //{"MEDIAN"},
                //{"MEDIAN_ABS_DEV"},
                {SqlFunction.of("MIN", this::sampleValueAtRow)},
                //{"MIN_VALUE_TIME"},
                //{"PERCENTILE"},
                {SqlFunction.of("SUM", this::sampleValueAtRow)},
                {SqlFunction.of("STDDEV", rowNumber -> "0.0")},
                {SqlFunction.of("WAVG", this::sampleValueAtRow)},
                {SqlFunction.of("WTAVG", this::sampleValueAtRow)}
        };
    }

    /**
     * @return 1st value - function name 2nd value - function transforming number of row in result set to a function result
     */
    @DataProvider
    public Object[][] analyticalFunctions() {
        return new Object[][]{
                {SqlFunction.of("first_value", rowNumber -> sampleValueAtRow(0))},
                {SqlFunction.of("lag", rowNumber -> (rowNumber == 0) ? "null" : sampleValueAtRow(rowNumber - 1))},
                {SqlFunction.of("lead", rowNumber -> (rowNumber == (DATA.length - 1)) ? "null" : sampleValueAtRow(rowNumber + 1))}
        };
    }

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC);
        long interval = TimeUnit.HOUR.toMilliseconds(1);
        long date = Mocks.MILLS_TIME;
        for(int data: DATA) {
            series.addSamples(Sample.ofTimeInteger(date, data));
            date += interval;
        }
        SeriesMethod.insertSeriesCheck(series);
    }

    @Test(description = "Tests that `BETWEEN N TIME_UNIT AND CURRENT ROW` cannot be applied to rows that are ordered not by time/datetime")
    @Issue("6560")
    public void testNotTimeOrder() {
        String sqlQuery = String.format("SELECT value FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY value) BETWEEN 1 HOUR PRECEDING AND CURRENT ROW", METRIC);
        String errorMessage = "row_number with time window requires ordering by time or datetime ascending " +
                "at line 1 position 114 near \"ROW_NUMBER\"";
        assertBadRequest("Request with ROW_NUMBER returned unexpected error", errorMessage, sqlQuery);
    }

    @Test(description = "Tests that if upper bound is not CURRENT ROW, error is thrown")
    @Issue("6560")
    public void testWrongUpperBound() {
        String sqlQuery = String.format("SELECT value FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND 4 MINUTE PRECEDING", METRIC);
        String errorMessage = "Syntax error at line 1 position 178: no viable alternative at input 'WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND 4'";
        assertBadRequest("Request with ROW_NUMBER returned unexpected error", errorMessage, sqlQuery);
    }

    @Test(description = "Tests only one row is affected by aggregation if ROW_NUMBER interval is less than interval between values")
    @Issue("6560")
    public void testAggregationWithLowerInterval() {
        String sqlQuery = String.format("SELECT value, COUNT(VALUE) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", METRIC);
        List<List<String>> expectedResult = prepareExpectedResult(row -> "1");
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(description = "Tests only two rows are affected by aggregation if ROW_NUMBER interval is more than interval between two values")
    @Issue("6560")
    public void testAggregationWithTwoRowsAffected() {
        String sqlQuery = String.format("SELECT value, COUNT(VALUE) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 2 HOUR PRECEDING AND CURRENT ROW", METRIC);
        List<List<String>> expectedResult = prepareExpectedResult(row -> (row == 0) ? "1" : "2");
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(
            description = "Tests that all aggregate functions respect boundaries",
            dataProvider = "aggregateFunctions"
    )
    @Issue("6560")
    public void testAllAggregateFunctions(SqlFunction function) {
        String sqlQuery = String.format("SELECT value, %s(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", function.literal, METRIC);
        List<List<String>> expectedValues = prepareExpectedResult(function.aggregationValueProviderAtRow);
        assertSqlQueryRows(expectedValues, sqlQuery);
    }

    @Test(
            description = "Tests that analytical functions do not respect boundaries",
            dataProvider = "analyticalFunctions"
    )
    @Issue("6560")
    public void testAllAnalyticalFunctions(SqlFunction function) {
        String sqlQuery = String.format("SELECT value, %s(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", function.literal, METRIC);
        List<List<String>> expectedValues = prepareExpectedResult(function.aggregationValueProviderAtRow);
        assertSqlQueryRows(expectedValues, sqlQuery);
    }

    private String sampleValueAtRow(int row) {
        return String.valueOf(DATA[row]);
    }

    private List<List<String>> prepareExpectedResult(IntFunction<String> rowFunction) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < DATA.length; i++) {
            result.add(Arrays.asList(sampleValueAtRow(i), rowFunction.apply(i)));
        }
        return result;
    }

    @Data(staticConstructor = "of")
    private static final class SqlFunction {
        private final String literal;
        private final IntFunction<String> aggregationValueProviderAtRow;
    }
}
