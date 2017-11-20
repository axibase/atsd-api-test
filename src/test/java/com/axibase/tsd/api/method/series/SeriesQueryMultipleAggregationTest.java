package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.model.series.AggregationType.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SeriesQueryMultipleAggregationTest extends SeriesTest {
    private final String TEST_ENTITY = Mocks.entity();
    private final String TEST_METRIC = Mocks.metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        List<Sample> samples = new ArrayList<>(1500);
        for (int i = 0; i < 1500; i++) {
            samples.add(Sample.ofTimeInteger(i * 1000, i));
        }
        series.setSamples(samples);

        insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideSingleAggregatorFunctions() {
        return new Object[][] {
                {MIN, new String[] {"0", "1000"}},
                {MAX, new String[] {"999", "1499"}},
                {AVG, new String[] {"499.5", "1249.5"}},
                {SUM, new String[] {"499500", "624750"}},
                {COUNT, new String[] {"1000", "500"}},
                {FIRST, new String[] {"0", "1000"}},
                {LAST, new String[] {"999", "1499"}},
                {DELTA, new String[] {"999", "500"}},
                {COUNTER, new String[] {"999", "500"}},
                {PERCENTILE_999, new String[] {"998.999", "1499"}},
                {PERCENTILE_995, new String[] {"994.995", "1497.495"}},
                {PERCENTILE_99, new String[] {"989.99", "1494.99"}},
                {PERCENTILE_95, new String[] {"949.95", "1474.95"}},
                {PERCENTILE_90, new String[] {"899.9", "1449.9"}},
                {PERCENTILE_75, new String[] {"749.75", "1374.75"}},
                {PERCENTILE_50, new String[] {"499.5", "1249.5"}},
                {PERCENTILE_25, new String[] {"249.25", "1124.25"}},
                {PERCENTILE_10, new String[] {"99.1", "1049.1"}},
                {PERCENTILE_5, new String[] {"49.05", "1024.05"}},
                {PERCENTILE_1, new String[] {"9.01", "1004.01"}},
                {PERCENTILE_05, new String[] {"4.005", "1001.505"}},
                {PERCENTILE_01, new String[] {"0.001", "1000"}},
                {MEDIAN, new String[] {"499.5", "1249.5"}},
                //{STANDARD_DEVIATION, new String[] {"288.819", "144.482"}},
                {SLOPE, new String[] {"0.001", "0.001"}},
                {INTERCEPT, new String[] {"0", "1000"}},
                {WAVG, new String[] {"666", "1332.667"}},
                {WTAVG, new String[] {"666", "1332.667"}},
        };
    }

    @Issue("4717")
    @Test(
            description = "test series query with single aggregation functions",
            dataProvider = "provideSingleAggregatorFunctions")
    public void testSingleAggregatorFunctions(AggregationType function, String[] expectedValues) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC);
        query.setStartDate("1970-01-01T00:00:00Z");
        query.setEndDate("1970-01-01T00:25:00Z");
        query.setAggregate(new Aggregate(function, new Interval(1000, TimeUnit.SECOND)));
        List<Series> result = querySeriesAsList(query);
        assertEquals(result.size(), 1, "Incorrect response series count");

        List<Sample> resultSamples = result.get(0).getData();
        assertEquals(resultSamples.size(), 2, "Incorrect response samples count");

        for (int i = 0; i < 2; i++) {
            Sample actualSample = resultSamples.get(i);
            String expectedDate = TestUtil.addTimeUnitsInTimezone(
                    "1970-01-01T00:00:00.000Z",
                    ZoneId.of("Etc/UTC"),
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
