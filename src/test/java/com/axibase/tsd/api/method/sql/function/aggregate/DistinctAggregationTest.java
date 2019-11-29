package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DistinctAggregationTest extends SqlTest {
    private static final String METRIC = Mocks.metric();

    private static final int[] DISTINCT_DATA = {1, 2, 3};

    @DataProvider
    public Object[][] distinctDataProvider() {
        return TestUtil.convertTo2DimArray(ArrayUtils.toObject(DISTINCT_DATA));
    }

    /**
     * Shuffle data before insertion to eliminate locality effects
     */
    @BeforeClass
    public static void prepareData() throws Exception {
        long time = Mocks.MILLS_TIME;
        Series series = new Series(Mocks.entity(), METRIC);
        final List<Integer> values = Arrays.stream(DISTINCT_DATA)
                .flatMap(i -> IntStream.generate(() -> i).limit(i))
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(values);
        for (Integer value : values) {
            series.addSamples(Sample.ofTimeInteger(time++, value));
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
                { NumberFormat.getInstance().format(Arrays.stream(DISTINCT_DATA).sum()) }
        };
        assertSqlQueryRows(expectedResult, sqlQuery);
    }

    @Issue("6536")
    @Test(description = "Test AVG(distinct value) function")
    public void testAvgAggregation() {
        String sqlQuery = String.format("SELECT AVG(DISTINCT value) FROM \"%s\"", METRIC);
        String[][] expectedResult = {
                { NumberFormat.getInstance().format(Arrays.stream(DISTINCT_DATA).average().getAsDouble()) }
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
