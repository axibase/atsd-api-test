package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SlidingTimeWindowTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final int[] DATA = {1, 2, 3, 4};

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

    @Test(description = "Tests that `first(value)` aggregation respects window boundaries.")
    @Issue("6560")
    public void testFirstAggregation() {
        String sqlQuery = String.format("SELECT value, first(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", METRIC);
        List<List<String>> expectedResult = Arrays.stream(DATA)
                .boxed()
                .map(data -> Arrays.asList(
                        String.valueOf(data), String.valueOf(data)
                ))
                .collect(Collectors.toList());
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Test(description = "Tests that `first_value(value)` respects window boundaries.")
    @Issue("6560")
    public void testFirstValue() {
        String sqlQuery = String.format("SELECT value, first_value(value) FROM \"%s\" WITH ROW_NUMBER(entity ORDER BY time) BETWEEN 1 MINUTE PRECEDING AND CURRENT ROW", METRIC);
        List<List<String>> expectedResult = Arrays.stream(DATA)
                .boxed()
                .map(data -> Arrays.asList(
                        String.valueOf(data), String.valueOf(data)
                ))
                .collect(Collectors.toList());
        assertSqlQueryRows(expectedResult, sqlQuery);
    }
}
