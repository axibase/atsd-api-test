package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SlidingTimeWindowTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final int[] DATA = {1, 2, 3, 4};

    private static Function<Integer, String> toFunction(Function<Integer, String> function) {
        return function;
    }

    /**
     * @return 1st value - function name 2nd value - Function transforming data to function result
     */
    @DataProvider
    public Object[][] aggregateFunctions() {
        return new Object[][]{
                {"AVG", toFunction(String::valueOf)},
                //{"CORREL"},
                {"COUNT", toFunction(data -> "1")},
                {"COUNTER", toFunction(data -> "NaN")},
                //{"COVAR"},
                {"DELTA", toFunction(data -> "NaN")},
                {"FIRST", toFunction(String::valueOf)},
                {"LAST", toFunction(String::valueOf)},
                {"MAX", toFunction(String::valueOf)},
                //{"MAX_VALUE_TIME"},
                //{"MEDIAN"},
                //{"MEDIAN_ABS_DEV"},
                {"MIN", toFunction(String::valueOf)},
                //{"MIN_VALUE_TIME"},
                //{"PERCENTILE"},
                {"SUM", toFunction(String::valueOf)},
                {"STDDEV", toFunction(data -> "0.0")},
                {"WAVG", toFunction(String::valueOf)},
                {"WTAVG", toFunction(String::valueOf)}
        };
    }

    /**
     * @return 1st value - function name 2nd value - function transforming number of row in result set to a function result
     */
    @DataProvider
    public Object[][] analyticalFunctions() {
        return new Object[][]{
                {"first_value", toFunction(rowNumber -> String.valueOf(DATA[0]))},
                {"lag", toFunction(rowNumber -> (rowNumber == 0) ? "null" : String.valueOf(DATA[rowNumber - 1]))},
                {"lead", toFunction(rowNumber -> (rowNumber == (DATA.length - 1)) ? "null" : String.valueOf(DATA[rowNumber + 1]))}
        };
    }

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC);
        long interval = 1000 * 60 * 60; //interval is 1 hour
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
        String errorMessage = "row_number with time window requires ordering by time or datetime ascending";
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
        List<List<String>> expectedResult = Arrays.stream(DATA)
                .boxed()
                .map(data -> Arrays.asList(
                        String.valueOf(data), "1"
                ))
                .collect(Collectors.toList());
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(description = "Tests only two rows are affected by aggregation if ROW_NUMBER interval is more than interval between two values")
    @Issue("6560")
    public void testAggregationWithTwoRowsAffected() {
        String sqlQuery = String.format("SELECT value, COUNT(VALUE) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 2 HOUR PRECEDING AND CURRENT ROW", METRIC);
        List<List<String>> expectedResult = new ArrayList<>();
        for(int i = 0; i < DATA.length; i++) {
            List<String> row;
            if(i == 0) {
                row = Arrays.asList(String.valueOf(DATA[i]), "1");
            } else {
                row = Arrays.asList(String.valueOf(DATA[i]), "2");
            }
            expectedResult.add(row);
        }
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(
            description = "Tests that all aggregate functions respect boundaries",
            dataProvider = "aggregateFunctions"
    )
    @Issue("6560")
    public void testAllAggregateFunctions(String functionLiteral, Function<Integer, String> function) {
        String sqlQuery = String.format("SELECT value, %s(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", functionLiteral, METRIC);
        List<List<String>> expectedValues = Arrays.stream(DATA)
                .boxed()
                .map(data -> Arrays.asList(
                        String.valueOf(data), function.apply(data)
                ))
                .collect(Collectors.toList());
        assertSqlQueryRows(expectedValues, sqlQuery);
    }

    @Test(
            description = "Tests that analytical functions do not respect boundaries",
            dataProvider = "analyticalFunctions"
    )
    @Issue("6560")
    public void testAllAnalyticalFunctions(String functionLiteral, Function<Integer, String> function) {
        String sqlQuery = String.format("SELECT value, %s(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", functionLiteral, METRIC);
        List<List<String>> expectedValues = new ArrayList<>();
        for (int i = 0; i < DATA.length; i++) {
            expectedValues.add(Arrays.asList(String.valueOf(DATA[i]), function.apply(i)));
        }
        assertSqlQueryRows(expectedValues, sqlQuery);
    }
}
