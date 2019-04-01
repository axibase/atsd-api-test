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
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;


/**
 * Take series transformations {@link Transformation#values()} with multiple sets of output series (@link Transformation#AGGREGATE,
 * @link Transformation#GROUP, @link Transformation#Forcast) with different state of parameter DETAIL
 * Check that response contains correct number of generated series for each permutation of the transformations.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series
 *
 * Methods setUpAggregation(), setUpGrouping(), and setUpForecasting() create test set of queries
 */


public class SeriesQueryTransformationWithDifferentForecast extends SeriesMethod {
    /**
     * Number of transform series
     */
    private static final int INPUT_SERIES_COUNT = 5;

    /**
     * Parameters of compilation of series
     */
    private static final int TIME_INTERVAL = 1;
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final int HALF_MINETES = 2;
    private static final String ZONE_ID = "Etc/UTC";
    private static final String TAG_NAME = "tag-name-1";
    private static final String TAG_VALUE = "tag-value-1";
    private static final String QUERY_ENTITY = "*";

    /**
     * Parameters of forecasting algorithms
     */
    private static final int HORIZON_LENGTH = 100;
    private static final double ALPHA = 0.5;
    private static final double BETTA = 0.5;
    private static final double GAMMA = 0.5;
    private static final int EIGENTRIPLE_LIMIT = 100;
    private static final int SINGULAR_VALUE_THRESHOLD = 5;

    /**
     * Period count for aggregate and group, respectively
     */
    private static final int AGGREGATION_PERIOD_COUNT = 3;
    private static final int GROUP_PERIOD_COUNT = 5;

    /**
     * Expected error
     */
    private static final int STATUS_ERROR = 400;

    /**
     * Set of transformation and created queries
     */
    List<SeriesQuery> queryList = new ArrayList<>();
    List<Transformation> transformations = Arrays.asList(
            Transformation.AGGREGATE,
            Transformation.FORECAST,
            Transformation.GROUP);

    @BeforeClass
    private void prepareData() throws Exception {
        String metric = Mocks.metric();
        insertSeries(metric);

        List<Aggregate> aggregates = setUpAggregation();
        List<Group> groups = setUpGrouping();
        List<Forecast> forecasts = setUpForecasting();

        for (Aggregate aggregate: aggregates) {
            for (Group group: groups) {
                for (Forecast forecast: forecasts) {
                    queryList.add(new SeriesQuery(QUERY_ENTITY, metric, START_DATE, END_DATE).setAggregate(aggregate).setGroup(group).setForecast(forecast));
                }
            }
        }
    }

    private void insertSeries(String metric) throws Exception {
        String entity1 = Mocks.entity();
        String entity2 = Mocks.entity();
        String entity3 = Mocks.entity();

        Series series1 = new Series(entity1, metric);
        Series series2 = new Series(entity1, metric, TAG_NAME, TAG_VALUE);
        Series series3 = new Series(entity2, metric, TAG_NAME, TAG_VALUE);
        Series series4 = new Series(entity2, metric, TAG_NAME, TAG_VALUE);
        Series series5 = new Series(entity3, metric);
        addSamplesToSeries(series1, series2, series3, series4, series5);
        insertSeriesCheck(series1, series2, series3, series4, series5);
    }

    private void addSamplesToSeries(Series series1, Series series2, Series series3, Series series4, Series series5) {
        long totalSamplesCount = TIME_INTERVAL * java.util.concurrent.TimeUnit.DAYS.toMinutes(1) * HALF_MINETES;
        for (int i = 0; i < totalSamplesCount; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, SECONDS_IN_HALF_MINUTE * i);
            Sample sample = Sample.ofDateInteger(time, SERIES_VALUE);
            series1.addSamples(sample);
            series2.addSamples(sample);
            series3.addSamples(sample);
            series4.addSamples(sample);
            series5.addSamples(sample);
        }
    }

    private List<Aggregate> setUpAggregation() {
        List<Aggregate> aggregates = new ArrayList<>();
        List<List<AggregationType>> setsAggregationType = Arrays.asList(Arrays.asList(AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST),
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

    private List<Group> setUpGrouping() {
        List<Group> groups = new ArrayList<>();
        List<List<GroupType>> setsGroupType = Arrays.asList(Arrays.asList(GroupType.AVG, GroupType.SUM),
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

    private List<Forecast> setUpForecasting() {
        List<Forecast> forecasts = new ArrayList<>();
        List<List<SeriesType>> setsSeriesType = Arrays.asList(Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED),
                Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED, SeriesType.HISTORY));
        Horizon horizon = new Horizon().setLength(HORIZON_LENGTH);
        HoltWintersSettings holtWintersSettings = new HoltWintersSettings().setAlpha(ALPHA).setBeta(BETTA).setGamma(GAMMA).setAuto(false);
        SSASettings ssaSettings = new SSASettings().setDecompose(new SSADecompositionSettings().setMethod(SvdMethod.AUTO).setEigentripleLimit(EIGENTRIPLE_LIMIT).setSingularValueThreshold(SINGULAR_VALUE_THRESHOLD));

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

    @DataProvider(name = "permutations", parallel = true)
    Object[][] permuteTransformations() {

        int permutationsCount = (int) CombinatoricsUtils.factorial(transformations.size());
        int queryCount = queryList.size();
        Object[][] permutations = new Object[permutationsCount*queryCount][2];

        PermutationIterator<Transformation> iterator = new PermutationIterator<>(transformations);
        int permutationIndex = 0;
        while (iterator.hasNext()) {
            List<Transformation> permutation = iterator.next();
            for (SeriesQuery query: queryList) {
                permutations[permutationIndex][0] = permutation;
                permutations[permutationIndex][1] = query;
                permutationIndex++;
            }
        }
        return permutations;
    }

    @Test(dataProvider = "permutations",

            description = "Take series transformations {@link Transformation#values()} with multiple set of output series. " +
                    "Create query which has these transformations with different status of parameter DETAIL in AGGREGATE and GROUP " +
                    "Check that response contains correct number of generated series for each permutation of the transformations.")
    public void testDifferentOrderTransformations(List<Transformation> permutation, SeriesQuery query) {
        query.setTransformationOrder(permutation);

        if (irregularSeriesForForecastException(permutation, query)) {
            assertEquals(querySeries(query).getStatus(), STATUS_ERROR);
        } else {
            List<Series> seriesList = querySeriesAsList(query);
            int expectedSeriesCount = countExpectedSeries(permutation, query);
            assertEquals(seriesList.size(), expectedSeriesCount);
        }
    }

    private int countExpectedSeries(List<Transformation> permutation, SeriesQuery query) {
        int seriesCount = INPUT_SERIES_COUNT;
        for (Transformation transformation: permutation) {
            switch (transformation) {
                case GROUP:
                    int groupingFunctionsCount = query.getGroup().getTypes().size();
                    if (query.getGroup().getTypes().contains(GroupType.DETAIL)) {
                        seriesCount += (seriesCount / INPUT_SERIES_COUNT) * (groupingFunctionsCount-1);
                    } else {
                        seriesCount = (seriesCount / INPUT_SERIES_COUNT) * groupingFunctionsCount;
                    }
                    break;
                case AGGREGATE:
                    int aggregationFunctionsCount = query.getAggregate().getTypes().size();
                    seriesCount *= aggregationFunctionsCount;
                    break;
                case FORECAST:
                    int factor=0;
                    if (query.getForecast().includeHistory()) factor++;
                    int algorithmsCount = query.getForecast().algorithmsCount();
                    if (query.getForecast().includeReconstructed()) factor += algorithmsCount;
                    if (query.getForecast().includeForecast()) factor += algorithmsCount;
                    seriesCount *= factor;
                    break;
            }
        }
        return seriesCount;
    }

    private boolean irregularSeriesForForecastException(List<Transformation> permutation, SeriesQuery query) {
        if (permutation.get(0).equals(Transformation.FORECAST)) {
            return true;
        } else {
            if (permutation.indexOf(Transformation.FORECAST)==1) {
                if (permutation.get(0).equals(Transformation.AGGREGATE) && query.getAggregate().getTypes().contains(AggregationType.DETAIL)) {
                    return true;
                }

                if (permutation.get(0).equals(Transformation.GROUP) && query.getGroup().getTypes().contains(GroupType.DETAIL)) {
                    return true;
                }

                return false;

            } else {
                return (query.getAggregate().getTypes().contains(AggregationType.DETAIL) && query.getGroup().getTypes().contains(GroupType.DETAIL));
            }
        }
    }
}
