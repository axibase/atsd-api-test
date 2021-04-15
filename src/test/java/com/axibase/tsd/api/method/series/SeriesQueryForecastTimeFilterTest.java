package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.Transformation;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Forecast;
import com.axibase.tsd.api.model.series.query.transformation.forecast.TimeFilter;
import com.axibase.tsd.api.model.series.query.transformation.forecast.HoltWintersSettings;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Horizon;
import com.axibase.tsd.api.util.TimeUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.method.series.SeriesMethod.querySeriesAsList;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SeriesQueryForecastTimeFilterTest {
    private final String entity = entity();
    private final String metric = metric();
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private final String inputSeriesStartTime = "2021-04-05T00:00:00Z"; // regular Monday
    private final String inputSeriesEndTime = "2021-04-16T00:00:00Z"; // next week Friday midnight (beginning)
    private final Period inputSeriesPeriod = new Period(1, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
    private SeriesQuery query;
    private Forecast forecastSettings;
    private TimeFilter schedule;

    @BeforeClass
    public void prepareData() throws Exception {
        insertInputSeries();
        schedule = new TimeFilter().setFrom("10:00").setTo("18:00");
        forecastSettings = buildForecastSettings();
        query = buildQuery();
    }

    private void insertInputSeries() throws Exception {
        Series series = new Series(entity, metric);
        long time = TimeUtil.epochMillis(inputSeriesStartTime);
        long seriesEndMillis = TimeUtil.epochMillis(inputSeriesEndTime);
        long periodMillis = inputSeriesPeriod.toMilliseconds();
        while (time < seriesEndMillis) {
            series.addSample(Sample.ofTimeDecimal(time, BigDecimal.ONE));
            time += periodMillis;
        }
        insertSeriesCheck(series);
    }

    @DataProvider(parallel = false)
    public Object[][] testData() {
        Interval days_2 = new Interval(2, TimeUnit.DAY);
        Interval days_3 = new Interval(3, TimeUnit.DAY);
        return new Object[][] {
                {new Horizon().setLength(111), false, false, 10 * 24 * 60, 111},
                {new Horizon().setLength(111), true, false, 11 * 8 * 60 - 24 * 60, 111}, // - 24*60: first period (= 1 day) of Holt-Winters reconstructed series is always zero, and not included in response
                {new Horizon().setLength(111), true, true, 9 * 8 * 60 - 24 * 60, 111},
                {new Horizon().setLength(2 * 24 * 60), true, true, 9 * 8 * 60 - 24 * 60, 2 * 24 * 60},
                {new Horizon().setInterval(days_2), false, false, 10 * 24 * 60, 2 * 24 * 60},
                {new Horizon().setInterval(days_2), true, false, 11 * 8 * 60 - 24 * 60, 2 * 8 * 60 - 1}, // -1: latest timestamp 17:59 is not included because it equals to end time of 2 day interval
                {new Horizon().setInterval(days_2), true, true, 9 * 8 * 60 - 24 * 60, 1 * 8 * 60},
                {new Horizon().setEndDate("2021-04-18T00:00:00Z"), false, false, 10 * 24 * 60, 2 * 24 * 60 + 1}, // +1: forecastEndTime implemented "inclusive"
                {new Horizon().setEndDate("2021-04-18T00:00:00Z"), true, false, 11 * 8 * 60 - 24 * 60, 2 * 8 * 60},
                {new Horizon().setEndDate("2021-04-18T00:00:00Z"), true, true, 9 * 8 * 60 - 24 * 60, 1 * 8 * 60},

                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setLength(111), false, false, 9 * 24 * 60, 111},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setLength(111), true, false, 10 * 8 * 60 - 24 * 60, 111},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setLength(111), true, true, 8 * 8 * 60 - 24 * 60, 111},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setLength(3 * 24 * 60), true, true, 8 * 8 * 60 - 24 * 60, 3 * 24 * 60},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setInterval(days_3), false, false, 9 * 24 * 60, 3 * 24 * 60},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setInterval(days_3), true, false, 10 * 8 * 60 - 24 * 60, 3 * 8 * 60 - 1},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setInterval(days_3), true, true, 8 * 8 * 60 - 24 * 60, 2 * 8 * 60},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setEndDate("2021-04-18T00:00:00Z"), false, false, 9 * 24 * 60, 3 * 24 * 60 + 1},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setEndDate("2021-04-18T00:00:00Z"), true, false, 10 * 8 * 60 - 24 * 60, 3 * 8 * 60},
                {new Horizon().setStartDate("2021-04-15T00:00:00Z").setEndDate("2021-04-18T00:00:00Z"), true, true, 8 * 8 * 60 - 24 * 60, 2 * 8 * 60},
        };
    }

    @Test(dataProvider="testData")
    public void test(Horizon horizonSettings, boolean applySchedule, boolean workingDaysOnly, int expectedReconstructedLength, int expectedForecastLength) {
        forecastSettings.setHorizon(horizonSettings);
        schedule.setCalendar(workingDaysOnly ? "rus" : null);
        forecastSettings.setTimeFilter(applySchedule ? schedule : null);
        List<Series> seriesList = querySeriesAsList(query);
        Assert.assertEquals(seriesList.size(), 2);
        for (Series series : seriesList) {
            int actualSeriesLength = series.getData().size();
            switch (series.getType()) {
                case RECONSTRUCTED:
                    Assert.assertEquals(actualSeriesLength, expectedReconstructedLength);
                    break;
                case FORECAST:
                    Assert.assertEquals(actualSeriesLength, expectedForecastLength);
                    break;
                default:
                    Assert.fail("Unexpected series type in response: " + series.getType());
            }
        }
    }

    private Forecast buildForecastSettings() {
        HoltWintersSettings hwSettings = new HoltWintersSettings()
                .setAlpha(0.5).setBeta(0.5).setGamma(0.5)
                .setAuto(false)
                .setPeriod(new Period(1, TimeUnit.DAY));
        List<SeriesType> include = Arrays.asList(SeriesType.RECONSTRUCTED, SeriesType.FORECAST);
        return new Forecast()
                .setHw(hwSettings)
                .setInclude(include)
                .setScoreInterval(new Interval(1, TimeUnit.DAY));
    }

    private SeriesQuery buildQuery() {
        Aggregate aggregate = new Aggregate()
                .setType(AggregationType.AVG)
                .setInterpolate(new AggregationInterpolate(AggregationInterpolateType.LINEAR, true))
                .setPeriod(inputSeriesPeriod);
        return new SeriesQuery(entity, metric, inputSeriesStartTime, inputSeriesEndTime)
                .setTransformationOrder(Arrays.asList(Transformation.AGGREGATE, Transformation.FORECAST))
                .setAggregate(aggregate)
                .setForecast(forecastSettings)
                .setTimezone(timeZone);
    }
}
