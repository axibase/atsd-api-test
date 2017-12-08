package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.*;

import static com.axibase.tsd.api.model.series.AggregationType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SeriesQueryMultipleAggregationTest extends SeriesTest {
    private final String TEST_ENTITY = Mocks.entity();
    private final String TEST_METRIC = Mocks.metric();
    private ZoneId serverTimezone;

    @BeforeClass
    public void prepareData() throws Exception {
        serverTimezone = Util.getServerTimeZone().toZoneId();
        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        String date = "2017-01-01T00:00:00Z";
        for (int i = 0; i < 1500; i++) {
            series.addSamples(Sample.ofDateInteger(
                    TestUtil.addTimeUnitsInTimezone(date, serverTimezone, TimeUnit.SECOND, i),
                    i));
        }

        insertSeriesCheck(series);
    }

    private Object[][] singleAggregationFunctions = new Object[][]{
            {MIN, new String[]{"0", "1000"}},
            {MAX, new String[]{"999", "1499"}},
            {AVG, new String[]{"499.5", "1249.5"}},
            {SUM, new String[]{"499500", "624750"}},
            {COUNT, new String[]{"1000", "500"}},
            {FIRST, new String[]{"0", "1000"}},
            {LAST, new String[]{"999", "1499"}},
            {DELTA, new String[]{"999", "500"}},
            {COUNTER, new String[]{"999", "500"}},
            {PERCENTILE_999, new String[]{"998.999", "1499"}},
            {PERCENTILE_995, new String[]{"994.995", "1497.495"}},
            {PERCENTILE_99, new String[]{"989.99", "1494.99"}},
            {PERCENTILE_95, new String[]{"949.95", "1474.95"}},
            {PERCENTILE_90, new String[]{"899.9", "1449.9"}},
            {PERCENTILE_75, new String[]{"749.75", "1374.75"}},
            {PERCENTILE_50, new String[]{"499.5", "1249.5"}},
            {PERCENTILE_25, new String[]{"249.25", "1124.25"}},
            {PERCENTILE_10, new String[]{"99.1", "1049.1"}},
            {PERCENTILE_5, new String[]{"49.05", "1024.05"}},
            {PERCENTILE_1, new String[]{"9.01", "1004.01"}},
            {PERCENTILE_05, new String[]{"4.005", "1001.505"}},
            {PERCENTILE_01, new String[]{"0.001", "1000"}},
            {MEDIAN, new String[]{"499.5", "1249.5"}},
            {STANDARD_DEVIATION, new String[] {"288.819", "144.482"}},
            {SLOPE, new String[]{"0.001", "0.001"}},
            {INTERCEPT, new String[]{"0", "1000"}},
            {WAVG, new String[]{"666", "1332.667"}},
            {WTAVG, new String[]{"666", "1332.667"}},
            {THRESHOLD_COUNT, new String[] {"1", "1"}},
            {THRESHOLD_DURATION, new String[] {"300000", "700000"}},
            {THRESHOLD_PERCENT, new String[] {"70", "30"}},
            {MIN_VALUE_TIME, new String[]{"1483228800000", "1483229800000"}},
            {MAX_VALUE_TIME, new String[]{"1483229799000", "1483230299000"}}
    };

    @DataProvider
    public Object[][] provideSingleAggregatorFunctions() {
        return singleAggregationFunctions;
    }

    @Issue("4717")
    @Test(
            description = "test series query with single aggregation functions",
            dataProvider = "provideSingleAggregatorFunctions")
    public void testSingleAggregatorFunctions(AggregationType function, String[] expectedValues) throws Exception {
        SeriesQuery query = createSeriesQuery(function);
        List<Series> result = querySeriesAsList(query);
        assertEquals(result.size(), 1, "Incorrect response series count");

        List<Sample> resultSamples = result.get(0).getData();
        assertEquals(resultSamples.size(), 2, "Incorrect response samples count");
        assertSamples(resultSamples, expectedValues);
    }

    @DataProvider
    public Object[][] provideDoubleAggregationFunctions() {
        List<Object[]> result = new ArrayList<>();
        for (int firstFilter = 0; firstFilter < singleAggregationFunctions.length; firstFilter++) {
            for (int secondFilter = 0; secondFilter < singleAggregationFunctions.length; secondFilter++) {
                AggregationType firstFilterType = (AggregationType) singleAggregationFunctions[firstFilter][0];
                String[] firstFilterExpectedResult = (String[]) singleAggregationFunctions[firstFilter][1];

                AggregationType secondFilterType = (AggregationType) singleAggregationFunctions[secondFilter][0];
                String[] secondFilterExpectedResult = (String[]) singleAggregationFunctions[secondFilter][1];

                result.add(new Object[]{
                        firstFilterType, firstFilterExpectedResult,
                        secondFilterType, secondFilterExpectedResult
                });
            }
        }

        return result.toArray(new Object[0][0]);
    }

    @Issue("4717")
    @Test(
            description = "test series query with double aggregation functions",
            dataProvider = "provideDoubleAggregationFunctions")
    public void testDoubleAggregatorFunctions(
            AggregationType firstFunction,
            String[] firstExpectedValues,
            AggregationType secondFunction,
            String[] secondExpectedValues) throws Exception {
        SeriesQuery firstQuery = createSeriesQuery(firstFunction);
        SeriesQuery secondQuery = createSeriesQuery(secondFunction);
        List<Series> result = querySeriesAsList(firstQuery, secondQuery);
        assertEquals(result.size(), 2, "Incorrect response series count");

        assertSamples(result.get(0).getData(), firstExpectedValues);
        assertSamples(result.get(1).getData(), secondExpectedValues);
    }

    private SeriesQuery createSeriesQuery(AggregationType function) {
        SeriesQuery query = new SeriesQuery(
                TEST_ENTITY,
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:25:00Z");
        Aggregate aggregate = new Aggregate(
                function,
                new Period(1000, TimeUnit.SECOND, PeriodAlignment.START_TIME));
        aggregate.setThreshold(new Threshold(300, 1300));
        query.setAggregate(aggregate);

        return query;
    }

    private void assertSamples(List<Sample> actualSamples, String[] expectedValues) {
        for (int i = 0; i < 2; i++) {
            Sample actualSample = actualSamples.get(i);
            String expectedDate = TestUtil.addTimeUnitsInTimezone(
                    "2017-01-01T00:00:00.000Z",
                    serverTimezone,
                    TimeUnit.SECOND,
                    i * 1000);

            assertEquals(actualSample.getRawDate(),
                    expectedDate,
                    "Incorrect sample date");

            BigDecimal expectedValue = new BigDecimal(expectedValues[i]).setScale(3, BigDecimal.ROUND_HALF_UP);
            BigDecimal actualValue = actualSample.getValue().setScale(3, BigDecimal.ROUND_HALF_UP);
            assertTrue(expectedValue.compareTo(actualValue) == 0,
                    String.format("Incorrect sample 1 value. Expected: %s actual: %s", expectedValue, actualValue));
        }
    }
}
