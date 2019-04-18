package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.Transformation;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.forecast.*;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import javax.ws.rs.core.Response;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * Take series transformations with multiple sets of output series ({@link Transformation#AGGREGATE},
 * {@link Transformation#GROUP}, {@link Transformation#FORECAST}) with different state of parameters DETAIL in Aggregation and Grouping
 * and History in Forecasting. Forecasting carried out by the algorithms Holt-Winters, SSA, and Holt-Winters and SSA simultaneously.
 * Check that response contains correct number of generated series for each permutation of the transformations.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series
 *
 * Methods generateAggregationSet(), generateGroupingSet(), and generateForecastingSet() create test set of queries
 */


public class SeriesQueryTransformationWithDifferentForecastTest extends SeriesMethod {

    /**
     * Series parameters
     */
    private static final int TIME_INTERVAL = 1;
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final int HALF_MINUTES = 2;
    private static final String ZONE_ID = "Asia/Kathmandu";
    private static final String TAG_NAME = "tag-name-1";
    private static final String TAG_VALUE_FIRST = "tag-value-1";
    private static final String TAG_VALUE_SECOND = "tag-value-2";
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC_NAME = Mocks.metric();

    /**
     * Parameters of forecasting algorithms
     */
    private static final int HORIZON_LENGTH = 100;
    private static final double HW_ALPHA = 0.5;
    private static final double HW_BETA = 0.5;
    private static final double HW_GAMMA = 0.5;
    private static final int SSA_EIGENTRIPLE_LIMIT = 100;
    private static final int SSA_SINGULAR_VALUE_THRESHOLD = 5;

    /**
     * Period count for aggregate and group, respectively
     */
    private static final int AGGREGATION_PERIOD_COUNT = 3;
    private static final int GROUP_PERIOD_COUNT = 5;

    private Series[] seriesArray;

    @BeforeClass
    private void insertSeries() throws Exception {
        String entity1 = Mocks.entity();
        String entity2 = Mocks.entity();
        String entity3 = Mocks.entity();

        seriesArray = new Series[] {
            new Series(entity1, METRIC_NAME),
            new Series(entity1, METRIC_NAME, TAG_NAME, TAG_VALUE_FIRST),
            new Series(entity2, METRIC_NAME, TAG_NAME, TAG_VALUE_FIRST),
            new Series(entity2, METRIC_NAME, TAG_NAME, TAG_VALUE_SECOND),
            new Series(entity3, METRIC_NAME)
        };
        addSamplesToSeries(seriesArray);
        insertSeriesCheck(seriesArray);
    }

    private void addSamplesToSeries(Series... seriesList) {
        long totalSamplesCount = TIME_INTERVAL * java.util.concurrent.TimeUnit.DAYS.toMinutes(1) * HALF_MINUTES;
        for (int i = 0; i < totalSamplesCount; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, SECONDS_IN_HALF_MINUTE * i);
            Sample sample = Sample.ofDateInteger(time, SERIES_VALUE);
            for(Series series: seriesList) {
                series.addSamples(sample);
            }
        }
    }

    @DataProvider(name = "bad_response_data", parallel = true)
    public Object[][] badResponseData() {
        return generateTestData(true);
    }

    @DataProvider(name = "good_response_data", parallel = true)
    public Object[][] goodResponseData() {
        return generateTestData(false);
    }

    @Test(dataProvider = "bad_response_data",
            description = "Take order of series transformations with irregular series before forecast. " +
                    "Check that response contains error")
    public void testBadResponse(SeriesQuery query) {
        assertEquals(Response.Status.fromStatusCode(querySeries(query).getStatus()), Response.Status.BAD_REQUEST,
                "Incorrect status of response (Expected: 400, Bad request)");
    }

    @Test(dataProvider = "good_response_data",
            description = "Check that response contains correct number of generated series for each permutation of the transformations.")
    public void testGoodResponse(SeriesQuery query) {
        List<Series> seriesList = querySeriesAsList(query);
        int expectedSeriesCount = countExpectedSeries(query);
        assertEquals(seriesList.size(), expectedSeriesCount, "Quantity of series in response not match expected");
    }

    private Object[][] generateTestData(boolean forBadResponse) {
        List<Transformation> transformations = Arrays.asList(
                Transformation.AGGREGATE,
                Transformation.FORECAST,
                Transformation.GROUP);
        List<Aggregate> aggregates = generateAggregationSet();
        List<Group> groups = generateGroupingSet();
        List<Forecast> forecasts = generateForecastingSet();
        List<SeriesQuery> queryList = new ArrayList<>();

        PermutationIterator<Transformation> iterator = new PermutationIterator<>(transformations);
        while (iterator.hasNext()) {
            List<Transformation> permutation = iterator.next();
            for (Aggregate aggregate : aggregates) {
                for (Group group : groups) {
                    for (Forecast forecast : forecasts) {
                        SeriesQuery query = new SeriesQuery(QUERY_ENTITY, METRIC_NAME, START_DATE, END_DATE)
                                .setAggregate(aggregate)
                                .setGroup(group)
                                .setForecast(forecast)
                                .setTransformationOrder(permutation);
                        if (isIrregularSeriesForForecastExceptionExpected(query) == forBadResponse) {
                            queryList.add(query);
                        }
                    }
                }
            }
        }

        return TestUtil.convertTo2DimArray(queryList.toArray());
    }

    private List<Aggregate> generateAggregationSet() {
        List<Aggregate> aggregates = new ArrayList<>();
        List<List<AggregationType>> setsAggregationType = Arrays.asList(
                Arrays.asList(AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST),
                Arrays.asList(AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST, AggregationType.DETAIL));
        Period period = new Period(AGGREGATION_PERIOD_COUNT, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);

        for (List<AggregationType> setAggregationType: setsAggregationType) {
            aggregates.add(new Aggregate()
                    .setPeriod(period)
                    .setInterpolate(interp)
                    .setTypes(setAggregationType));
        }

        return aggregates;
    }

    private List<Group> generateGroupingSet() {
        List<Group> groups = new ArrayList<>();
        List<List<GroupType>> setsGroupType = Arrays.asList(
                Arrays.asList(GroupType.AVG, GroupType.SUM),
                Arrays.asList(GroupType.AVG, GroupType.SUM, GroupType.DETAIL));
        Period period = new Period(GROUP_PERIOD_COUNT, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);

        for (List<GroupType> setGroupType: setsGroupType) {
            groups.add(new Group()
                    .setPeriod(period)
                    .setInterpolate(interp)
                    .setTypes(setGroupType));
        }

        return groups;
    }

    private List<Forecast> generateForecastingSet() {
        List<Forecast> forecasts = new ArrayList<>();
        List<List<SeriesType>> setsSeriesType = Arrays.asList(
                Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED),
                Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED, SeriesType.HISTORY));
        Horizon horizon = new Horizon().setLength(HORIZON_LENGTH);
        HoltWintersSettings holtWintersSettings = new HoltWintersSettings()
                .setAlpha(HW_ALPHA)
                .setBeta(HW_BETA)
                .setGamma(HW_GAMMA)
                .setAuto(false);
        SSASettings ssaSettings = new SSASettings()
                .setDecompose(new SSADecompositionSettings()
                        .setMethod(SvdMethod.AUTO)
                        .setEigentripleLimit(SSA_EIGENTRIPLE_LIMIT)
                        .setSingularValueThreshold(SSA_SINGULAR_VALUE_THRESHOLD));

        for (List<SeriesType> setSeriesType: setsSeriesType) {
            forecasts.add(new Forecast()
                    .setHw(holtWintersSettings)
                    .setHorizon(horizon)
                    .setInclude(setSeriesType));

            forecasts.add(new Forecast()
                    .setSsa(ssaSettings)
                    .setHorizon(horizon)
                    .setInclude(setSeriesType));

            forecasts.add(new Forecast()
                    .setHw(holtWintersSettings)
                    .setSsa(ssaSettings)
                    .setHorizon(horizon)
                    .setInclude(setSeriesType));
        }

        return forecasts;
    }

    private int countExpectedSeries(SeriesQuery query) {
        final int inputSeriesCount = seriesArray.length;
        int expectedSeriesCount = seriesArray.length;
        List<Transformation> permutation = query.getTransformationOrder();
        for (Transformation transformation: permutation) {
            switch (transformation) {
                case GROUP:
                    int groupingFunctionsCount = query.getGroup().getTypes().size();
                    if (query.getGroup().getTypes().contains(GroupType.DETAIL)) {
                        expectedSeriesCount += (expectedSeriesCount / inputSeriesCount) * (groupingFunctionsCount-1);
                    } else {
                        expectedSeriesCount = (expectedSeriesCount / inputSeriesCount) * groupingFunctionsCount;
                    }
                    break;
                case AGGREGATE:
                    int aggregationFunctionsCount = query.getAggregate().getTypes().size();
                    expectedSeriesCount *= aggregationFunctionsCount;
                    break;
                case FORECAST:
                    int factor = 0;
                    if (query.getForecast().includeHistory()) factor++;
                    int algorithmsCount = query.getForecast().algorithmsCount();
                    if (query.getForecast().includeReconstructed()) factor += algorithmsCount;
                    if (query.getForecast().includeForecast()) factor += algorithmsCount;
                    expectedSeriesCount *= factor;
                    break;
            }
        }
        return expectedSeriesCount;
    }

    private boolean isIrregularSeriesForForecastExceptionExpected(SeriesQuery query) {
        List<Transformation> permutation = query.getTransformationOrder();
        Transformation transformation = permutation.get(0);
        if (transformation == Transformation.FORECAST) {
            return true;
        }

        boolean hasGroupDetail = query.getGroup().getTypes().contains(GroupType.DETAIL);
        boolean hasAggregationDetail = query.getAggregate().getTypes().contains(AggregationType.DETAIL);

        if (permutation.indexOf(Transformation.FORECAST) == 1) {
            if (transformation == Transformation.AGGREGATE && hasAggregationDetail) {
                return true;
            }

            return (transformation == Transformation.GROUP && hasGroupDetail);
        }

        return (hasAggregationDetail && hasGroupDetail);
    }
}