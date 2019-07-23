package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.Transformation;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.forecast.*;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.time.ZoneId;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * Take different horizon options of forecast (including different state of parameter start date:
 * none, before end, and after end of input series).
 * Forecasting carried out by the algorithms Holt-Winters and SSA.
 * Check that response contains correct start date, period, and quantity of generated forecasting series.
 * For expected data is created a new class CheckedParameters.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series.
 */

public class SeriesQueryHorizonTest extends SeriesMethod{

    @Data
    @RequiredArgsConstructor
    private static class CheckedParameters {
        private final String startDate;
        private final long period;
        private final int count;

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }

    /**
     * Parameters of compilation of series
     */
    private static final int AGGREGATION_PERIOD = 3;
    private static final int SAMPLES_COUNT = 2880;
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final String ZONE_ID = "Asia/Kathmandu";
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC = "metric_6145";
    private static final String ENTITY = "entity_6145";

    /**
     * Parameters of forecasting algorithms
     */
    private static final double HW_ALPHA = 0.5;
    private static final double HW_BETA = 0.5;
    private static final double HW_GAMMA = 0.5;
    private static final int SSA_EIGENTRIPLE_LIMIT = 100;
    private static final int SSA_SINGULAR_VALUE_THRESHOLD = 5;

    /**
     * Parameters of Horizon
     */
    private static final int HORIZON_LENGTH = 15;
    private static final Interval HORIZON_INTERVAL = new Interval(50, TimeUnit.MINUTE);
    private static final String HORIZON_END_DATE = "2019-01-02T00:55:00Z";
    private static final String HORIZON_START_DATE_BEFORE_SERIES_END = "2019-01-01T23:50:00Z";
    private static final String HORIZON_START_DATE_AFTER_SERIES_END = "2019-01-02T00:10:00Z";

    /**
     * Expected values
     */
    private static final String START_DATE_NOT_SET = "2019-01-02T00:00:00.000Z";
    private static final int COUNT_WITH_INTERVAL = 16;
    private static final int COUNT_WITH_END_DATE = 19;
    private static final String START_DATE_BEFORE_SERIES_END = "2019-01-01T23:51:00.000Z";
    private static final int COUNT_WITH_INTERVAL_AND_START_DATE_BEFORE = 16;
    private static final int COUNT_WITH_END_DATE_AND_START_DATE_BEFORE = 22;
    private static final String START_DATE_AFTER_SERIES_END = "2019-01-02T00:00:00.000Z";
    private static final int COUNT_WITH_INTERVAL_AND_START_DATE_AFTER = 16;
    private static final int COUNT_WITH_END_DATE_AND_START_DATE_AFTER = 19;

    private HoltWintersSettings holtWintersSettings = new HoltWintersSettings()
            .setAlpha(HW_ALPHA)
            .setBeta(HW_BETA)
            .setGamma(HW_GAMMA)
            .setAuto(false);
    private SSASettings ssaSettings = new SSASettings()
            .setDecompose(new SSADecompositionSettings()
                    .setMethod(SvdMethod.AUTO)
                    .setEigentripleLimit(SSA_EIGENTRIPLE_LIMIT)
                    .setSingularValueThreshold(SSA_SINGULAR_VALUE_THRESHOLD));

    @BeforeClass
    private void insertSeries() throws Exception {
        Series series = new Series(ENTITY, METRIC);
        addSamplesToSeries(series);
        insertSeriesCheck(series);
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonInterval() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonIntervalWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL_AND_START_DATE_BEFORE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonIntervalWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL_AND_START_DATE_AFTER);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonLength() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonLengthWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonLengthWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonEndDate() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, COUNT_WITH_END_DATE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonEndDateWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_END_DATE_AND_START_DATE_BEFORE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testHWHorizonEndDateWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setHw(holtWintersSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_END_DATE_AND_START_DATE_AFTER);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonInterval() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonIntervalWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL_AND_START_DATE_BEFORE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonIntervalWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setInterval(HORIZON_INTERVAL)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_INTERVAL_AND_START_DATE_AFTER);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonLength() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonLengthWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonLengthWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setLength(HORIZON_LENGTH)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, HORIZON_LENGTH);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonEndDate() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_NOT_SET, AGGREGATION_PERIOD, COUNT_WITH_END_DATE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonEndDateWithStartDateBeforeSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)
                                .setStartDate(HORIZON_START_DATE_BEFORE_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_BEFORE_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_END_DATE_AND_START_DATE_BEFORE);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }

    @Issue("6145")
    @Test(description = "Checks that object CheckedParameters composed from response equals expected")
    public void testSSAHorizonEndDateWithStartDateAfterSeriesEnd() {
        SeriesQuery query = generateQuery()
                .setForecast(new Forecast()
                        .setSsa(ssaSettings)
                        .setHorizon(new Horizon()
                                .setEndDate(HORIZON_END_DATE)
                                .setStartDate(HORIZON_START_DATE_AFTER_SERIES_END)));
        CheckedParameters expectedData = new CheckedParameters(START_DATE_AFTER_SERIES_END, AGGREGATION_PERIOD, COUNT_WITH_END_DATE_AND_START_DATE_AFTER);
        CheckedParameters actualData = generateCheckedParameters(query);

        assertEquals(actualData, expectedData, "Checked parameters not match expected");
    }


    private void addSamplesToSeries(Series series) {
        for (int i = 0; i < SAMPLES_COUNT; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, SECONDS_IN_HALF_MINUTE * i);
            Sample sample = Sample.ofDateInteger(time, SERIES_VALUE);
            series.addSamples(sample);
        }
    }

    private Aggregate generateAggregationSettings() {
        Aggregate aggregate = new Aggregate();
        List<AggregationType> aggregationType = new ArrayList<>();
        aggregationType.add(AggregationType.AVG);
        Period period = new Period(AGGREGATION_PERIOD, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);

        aggregate.setPeriod(period)
                .setInterpolate(interp)
                .setTypes(aggregationType);

        return aggregate;
    }

    private SeriesQuery generateQuery() {
        return new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
                .setTransformationOrder(Arrays.asList(
                        Transformation.AGGREGATE,
                        Transformation.FORECAST))
                .setAggregate(generateAggregationSettings());
    }

    private CheckedParameters generateCheckedParameters(SeriesQuery query) {
        List<Series> seriesList = querySeriesAsList(query);
        String firsDate = seriesList.get(0).getData().get(0).getRawDate();
        String secondDate = seriesList.get(0).getData().get(1).getRawDate();
        long period = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(Util.parseDate(secondDate).getTime() - Util.parseDate(firsDate).getTime());

        return new CheckedParameters(firsDate, period, seriesList.get(0).getData().size());
    }
}
