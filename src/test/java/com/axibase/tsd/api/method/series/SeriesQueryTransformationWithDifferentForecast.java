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
 * Take series transformations with multiple sets of output series (@link Transformation#AGGREGATE,
 * @link Transformation#GROUP, @link Transformation#Forcast) with different state of parameters DETAIL in Aggregation and Grouping
 * and History in Forecasting. Forecasting carried out by the algorithms Holt-Winters, SSA, and Holt-Winters and SSA simultaneously.
 * Check that response contains correct number of generated series for each permutation of the transformations.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series
 *
 * Methods generationAggregationSet(), generationGroupingSet(), and generationForecastingSet() create test set of queries
 */


public class SeriesQueryTransformationWithDifferentForecast extends SeriesMethod {
    /**
     * Number of transform series
     */
    private static final int INPUT_SERIES_COUNT = 5;

    /**
     * Series parameters
     */
    private static final int TIME_INTERVAL = 1;
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final int HALF_MINUTES = 2;
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
     * Set of transformations and created queries
     */
    private List<SeriesQuery> queryList = new ArrayList<>();
    private List<Transformation> transformations = Arrays.asList(
            Transformation.AGGREGATE,
            Transformation.FORECAST,
            Transformation.GROUP);
    private List<Object[]> badResponseTestSet = new ArrayList<>();
    private List<Object[]> goodResponseTestSet = new ArrayList<>();

    @BeforeClass
    private void prepareData() throws Exception {
        String metric = Mocks.metric();
        insertSeries(metric);

        List<Aggregate> aggregates = generationAggregationSet();
        List<Group> groups = generationGroupingSet();
        List<Forecast> forecasts = generationForecastingSet();

        for (Aggregate aggregate: aggregates) {
            for (Group group: groups) {
                for (Forecast forecast: forecasts) {
                    queryList.add(new SeriesQuery(QUERY_ENTITY, metric, START_DATE, END_DATE)
                            .setAggregate(aggregate)
                            .setGroup(group)
                            .setForecast(forecast));
                }
            }
        }

        separationTestSets();
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
        long totalSamplesCount = TIME_INTERVAL * java.util.concurrent.TimeUnit.DAYS.toMinutes(1) * HALF_MINUTES;
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

    private List<Aggregate> generationAggregationSet() {
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

    private List<Group> generationGroupingSet() {
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

    private List<Forecast> generationForecastingSet() {
        List<Forecast> forecasts = new ArrayList<>();
        List<List<SeriesType>> setsSeriesType = Arrays.asList(
                Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED),
                Arrays.asList(SeriesType.FORECAST, SeriesType.RECONSTRUCTED, SeriesType.HISTORY));
        Horizon horizon = new Horizon().setLength(HORIZON_LENGTH);
        HoltWintersSettings holtWintersSettings = new HoltWintersSettings()
                .setAlpha(ALPHA)
                .setBeta(BETTA)
                .setGamma(GAMMA)
                .setAuto(false);
        SSASettings ssaSettings = new SSASettings()
                .setDecompose(new SSADecompositionSettings()
                        .setMethod(SvdMethod.AUTO)
                        .setEigentripleLimit(EIGENTRIPLE_LIMIT)
                        .setSingularValueThreshold(SINGULAR_VALUE_THRESHOLD));

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

    private void separationTestSets () {
        PermutationIterator<Transformation> iterator = new PermutationIterator<>(transformations);
        while (iterator.hasNext()) {
            List<Transformation> permutation = iterator.next();
            for (SeriesQuery query: queryList) {
                Object[] testSet = {permutation, query};
                if (irregularSeriesForForecastException(permutation, query)) {
                    badResponseTestSet.add(testSet);
                } else {
                    goodResponseTestSet.add(testSet);
                }
            }
        }
    }

    @DataProvider(name = "bad_response_data", parallel = true)
    Object[][] badResponseData() {
        Object[][] data = new Object[badResponseTestSet.size()][];
        int index=0;
        for (Object[] testSet: badResponseTestSet) {
            data[index] = testSet;
            index++;
        }
        return data;
    }

    @DataProvider(name = "good_response_data", parallel = true)
    Object[][] goodResponseData() {
        Object[][] data = new Object[goodResponseTestSet.size()][];
        int index=0;
        for (Object[] testSet: goodResponseTestSet) {
            data[index] = testSet;
            index++;
        }
        return data;
    }

    @Test(dataProvider = "bad_response_data",
            description = "Take order of series transformations with irregular series before forecast. " +
                    "Check that response contains error")
    public void testBadResponse(List<Transformation> permutation, SeriesQuery query) {
        query.setTransformationOrder(permutation);
        assertEquals(Response.Status.fromStatusCode(querySeries(query).getStatus()), Response.Status.BAD_REQUEST);
    }

    @Test(dataProvider = "good_response_data",
            description = "Check that response contains correct number of generated series for each permutation of the transformations.")
    public void testGoodResponse(List<Transformation> permutation, SeriesQuery query) {
        query.setTransformationOrder(permutation);
        List<Series> seriesList = querySeriesAsList(query);
        int expectedSeriesCount = countExpectedSeries(permutation, query);
        assertEquals(seriesList.size(), expectedSeriesCount);
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
        if (permutation.get(0)==Transformation.FORECAST) {
            return true;
        } else if (permutation.indexOf(Transformation.FORECAST)==1) {
            if (permutation.get(0)==Transformation.AGGREGATE && query.getAggregate().getTypes().contains(AggregationType.DETAIL)) {
                return true;
            }

            return (permutation.get(0)==Transformation.GROUP && query.getGroup().getTypes().contains(GroupType.DETAIL));

        } else {
            return (query.getAggregate().getTypes().contains(AggregationType.DETAIL) && query.getGroup().getTypes().contains(GroupType.DETAIL));
        }
    }
}
