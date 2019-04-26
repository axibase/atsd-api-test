package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Forecast;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Horizon;
import com.axibase.tsd.api.model.series.query.transformation.forecast.SSASettings;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

/**
 * Test of auto aggregation. Applied if flag {@link Forecast#autoAggregate} is true.
 * Check that response contains correct forecast series.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series.
 */

public class SeriesQueryForecastAutoAggregateTest extends SeriesMethod {
    /**
     * Series parameters
     */
    private static final String START_DATE = "2019-04-01T00:00:00Z";
    private static final String END_DATE = "2019-04-01T02:00:00Z";
    private static final int TOTAL_SAMPLES_COUNT = 480;
    private static final int TIMESTAMP_PERIOD = 15;
    private static final String ZONE_ID = "Asia/Kathmandu";
    private static final String ENTITY = "e-6171";
    private static final String METRIC = "m-6171";

    private static final SeriesQuery query = new SeriesQuery(ENTITY, METRIC, START_DATE, END_DATE)
            .setForecast(new Forecast()
                    .setAutoAggregate(true)
                    .setHorizon(new Horizon()
                            .setInterval(new Interval(1, TimeUnit.HOUR)))
                    .setInclude(Arrays.asList(SeriesType.FORECAST))
                    .setSsa(new SSASettings()));

    /**
     * Series is filling irregularly with different values in samples.
     * Map samplesData contains in key field shift of time and in value field - value of sample.
     */
    @BeforeClass
    private void insertSeries() throws Exception {
        Map<Integer, Integer> samplesData = new HashMap<>();
        samplesData.put(0, 102);
        samplesData.put(1, -100);
        samplesData.put(3, 1);
        samplesData.put(6, 1);
        samplesData.put(10, 1);

        Series series = new Series(ENTITY, METRIC);
        addSamplesToSeries(series, samplesData);
        insertSeriesCheck(series);
    }

    @Test(description = "Checks that response have single series'")
    public void testResponseHaveSingleSeries() {
        List<Series> seriesList = querySeriesAsList(query);

        assertEquals(seriesList.size(), 1, "Wrong count of series");
    }

    @Test(dependsOnMethods = "testResponseHaveSingleSeries", description = "Checks that output series in response have type 'Forecast'")
    public void testResponseHaveForecastSeries() {
        List<Series> seriesList = querySeriesAsList(query);

        assertSame(seriesList.get(0).getType(), SeriesType.FORECAST, "Output series have type not 'forecast'");
    }

    @Test(dependsOnMethods = "testResponseHaveForecastSeries", description = "Checks that forecast series is regular" +
            " and time span between samples is correct")
    public void testOfMatchTimeSpanBetweenSamplesAndPeriodCount() {
        List<Series> seriesList = querySeriesAsList(query);
        Series series = seriesList.get(0);
        List<Sample> samples = series.getData();
        long timeStampPeriodMs = timeStampDifference(samples.get(0), samples.get(1));

        for (int i = 2; i < samples.size(); i++) {
            long timeSpan = timeStampDifference(samples.get(i - 1), samples.get(i));
            if (timeSpan != timeStampPeriodMs) {
                throw new AssertionError("Output series is irregular");
            }
        }

        Period period = series.getAggregate().getPeriod();
        long countPeriodMs = toMilliseconds(period.getUnit(), period.getCount());

        assertEquals(timeStampPeriodMs, countPeriodMs,"Count in period of aggregation not match time span between samples");
    }

    private void addSamplesToSeries(Series series, Map<Integer, Integer> samplesData) {
        for (int i = 0; i < TOTAL_SAMPLES_COUNT; i++) {
            for (Map.Entry<Integer, Integer> pair: samplesData.entrySet()) {
                String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, i * TIMESTAMP_PERIOD + pair.getKey());
                Sample sample = Sample.ofDateInteger(time, pair.getValue());
                series.addSamples(sample);
            }
        }
    }

    private long timeStampDifference(Sample firstTimeStamp, Sample secondTimeStamp) {
        return Util.parseDate(secondTimeStamp.getRawDate()).getTime() - Util.parseDate(firstTimeStamp.getRawDate()).getTime();
    }

    private long toMilliseconds(TimeUnit timeUnit, long count) {
        switch (timeUnit) {
            case NANOSECOND: return java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(count);
            case MILLISECOND: return count;
            case SECOND: return java.util.concurrent.TimeUnit.SECONDS.toMillis(count);
            case MINUTE: return java.util.concurrent.TimeUnit.MINUTES.toMillis(count);
            case HOUR: return java.util.concurrent.TimeUnit.HOURS.toMillis(count);
            case DAY: return java.util.concurrent.TimeUnit.DAYS.toMillis(count);
        }

        throw new AssertionError("Incorrect time unit in response");
    }
}
