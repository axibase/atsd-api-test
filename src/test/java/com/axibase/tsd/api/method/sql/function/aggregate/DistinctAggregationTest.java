package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

public class DistinctAggregationTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final Integer[] DISTINCT_DATA = {1, 2, 3}; //each of the values will be inserted 2 times

    @DataProvider
    public Object[][] distinctDataProvider() {
        return TestUtil.convertTo2DimArray(DISTINCT_DATA);
    }

    @BeforeClass
    public void prepareData() throws Exception {
        long time = Mocks.MILLS_TIME;
        Series series = new Series(Mocks.entity(), METRIC);
        for(int i = 0; i < 3; i++) {
            for(int data: DISTINCT_DATA) {
                series.addSamples(Sample.ofTimeInteger(time++, data));
            }
        }
        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("6536")
    @Test(description = "Test COUNT(distinct value) function")
    public void testCountAggregation() {
        String sqlQuery = String.format("SELECT COUNT(DISTINCT value) FROM \"%s\"", METRIC);
        String[][] expectedResult = {
                {String.valueOf(DISTINCT_DATA.length)}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Test SUM(distinct value) function")
    public void testSumAggregation() {
        String sqlQuery = String.format("SELECT SUM(DISTINCT value) FROM \"%s\"", METRIC);
        String[][] expectedResult = {
                {String.valueOf(Arrays.stream(DISTINCT_DATA).mapToInt(n -> n).sum())}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Test AVG(distinct value) function")
    public void testAvgAggregation() {
        String sqlQuery = String.format("SELECT AVG(DISTINCT value) FROM \"%s\"", METRIC);
        String[][] expectedResult = {
                {String.valueOf(Arrays.stream(DISTINCT_DATA).mapToInt(n -> n).sum() / DISTINCT_DATA.length)}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Issue("6536")
    @Test(
            description = "Tests that COUNT(DISTINCT value) returns 1 if value is specified",
            dataProvider = "distinctDataProvider"
    )
    public void testCountEachValue(Integer data) {
        String sqlQuery = String.format("SELECT COUNT(DISTINCT value) FROM \"%s\" WHERE value=%d", METRIC, data);
        String[][] expectedResult = {
                {"1"}
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

}
