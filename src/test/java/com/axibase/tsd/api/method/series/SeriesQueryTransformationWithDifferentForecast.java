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
 */


public class SeriesQueryTransformationWithDifferentForecast extends SeriesMethod {
    private int inputSeriesCount;
    private int queryCount;
    List<SeriesQuery> queryList = new ArrayList<>();



    int days = 1;
    String startDate = "2019-01-01T00:00:00Z";
    String endDate = "2019-01-02T00:00:00Z";

    List<Transformation> transformations = Arrays.asList(
            Transformation.AGGREGATE,
            Transformation.FORECAST,
            Transformation.GROUP);
    Aggregate aggregationSettings;
    Group groupSettings;
    Forecast forecastSettings;

    @BeforeClass
    public void prepareData() throws Exception {
        String metric = Mocks.metric();
        insertSeries(metric);
        queryCount = 12;

        for (int i=0; i<queryCount; i++) {
            queryList.add(new SeriesQuery( "*", metric, startDate, endDate));
        }

        setUpAggregation();
        setUpGrouping();
        setUpForecasting();
    }

    private void insertSeries(String metric) throws Exception {

        String entity1 = Mocks.entity();
        String entity2 = Mocks.entity();
        String entity3 = Mocks.entity();

        Series series1 = new Series(entity1, metric);
        Series series2 = new Series(entity1, metric, "tag-name-1", "tag-value-1");
        Series series3 = new Series(entity2, metric, "tag-name-1", "tag-value-1");
        Series series4 = new Series(entity2, metric, "tag-name-1", "tag-value-2");
        Series series5 = new Series(entity3, metric);
        addSamplesToSeries(series1, series2, series3, series4, series5);
        insertSeriesCheck(series1, series2, series3, series4, series5);
        inputSeriesCount = 5;
    }

    private void addSamplesToSeries(Series series1, Series series2, Series series3, Series series4, Series series5) {
        int totalSamplesCount = days * 24 * 60 * 2;
        for (int i = 0; i < totalSamplesCount; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(startDate, ZoneId.of("Etc/UTC"), TimeUnit.SECOND, 30 * i);
            Sample sample = Sample.ofDateInteger(time, 101);
            series1.addSamples(sample);
            series2.addSamples(sample);
            series3.addSamples(sample);
            series4.addSamples(sample);
            series5.addSamples(sample);
        }
    }

    private void setUpAggregation() {
        Period period = new Period(3, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);

        aggregationSettings = new Aggregate()
                .setPeriod(period)
                .setTypes(Arrays.asList(AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST))
                .setInterpolate(interp);
        for (int i=0; i<queryCount/2; i++) {
                queryList.get(i).setAggregate(aggregationSettings);
        }

        aggregationSettings = new Aggregate()
                .setPeriod(period)
                .setTypes(Arrays.asList(AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST, AggregationType.DETAIL))
                .setInterpolate(interp);
        for (int i=queryCount/2; i<queryCount; i++) {
                queryList.get(i).setAggregate(aggregationSettings);
        }
    }

    private void setUpGrouping() {
        Period period = new Period(5, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);

        groupSettings = new Group()
                .setPeriod(period)
                .setTypes(Arrays.asList(GroupType.AVG, GroupType.SUM))
                .setInterpolate(interp);
        for (int i=0; i<queryCount; i+=2) {
                queryList.get(i).setGroup(groupSettings);
        }

        groupSettings = new Group()
                .setPeriod(period)
                .setTypes(Arrays.asList(GroupType.AVG, GroupType.SUM, GroupType.DETAIL))
                .setInterpolate(interp);
        for (int i=1; i<queryCount; i+=2) {
                queryList.get(i).setGroup(groupSettings);
        }
    }

    private void setUpForecasting() {
        Horizon horizon = new Horizon().setLength(100);
        HoltWintersSettings holtWintersSettings = new HoltWintersSettings().setAlpha(0.5).setBeta(0.5).setGamma(0.5).setAuto(false);
        SSASettings ssaSettings = new SSASettings().setDecompose(new SSADecompositionSettings().setMethod(SvdMethod.AUTO).setEigentripleLimit(100).setSingularValueThreshold(5));
//        ARIMASettings arimaSettings = new ARIMASettings();

        forecastSettings = new Forecast()
                .setHw(holtWintersSettings)
                .setHorizon(horizon)
                .setInclude(Arrays.asList(SeriesType.FORECAST, SeriesType.HISTORY, SeriesType.RECONSTRUCTED));
        for (int i=0; i<queryCount; i+=3) {
                queryList.get(i).setForecast(forecastSettings);
        }

        forecastSettings = new Forecast()
                .setSsa(ssaSettings)
                .setHorizon(horizon)
                .setInclude(Arrays.asList(SeriesType.FORECAST, SeriesType.HISTORY, SeriesType.RECONSTRUCTED));
        for (int i=1; i<queryCount; i+=3) {
            queryList.get(i).setForecast(forecastSettings);
        }

        forecastSettings = new Forecast()
                .setHw(holtWintersSettings)
                .setSsa(ssaSettings)
                .setHorizon(horizon)
                .setInclude(Arrays.asList(SeriesType.FORECAST, SeriesType.HISTORY, SeriesType.RECONSTRUCTED));
        for (int i=2; i<queryCount; i+=3) {
            queryList.get(i).setForecast(forecastSettings);
        }
    }

    @DataProvider(name = "permutations"/*, parallel = true*/)
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
    public void Test(List<Transformation> permutation, SeriesQuery query) {
        query.setTransformationOrder(permutation);

        if (irregularSeriesForForecastException(permutation, query)) {
            assertEquals(querySeries(query).getStatus(), 400);
        } else {
            List<Series> seriesList = querySeriesAsList(query);
            int expectedSeriesCount = countExpectedSeries(permutation, query);
            assertEquals(seriesList.size(), expectedSeriesCount);
        }
    }

    private int countExpectedSeries(List<Transformation> permutation, SeriesQuery query) {
        int seriesCount = inputSeriesCount;
        for (int i=0; i<permutation.size(); i++) {
            switch (permutation.get(i)) {
                case GROUP:
                    int groupingFunctionsCount = query.getGroup().getTypes().size();
                    if (query.getGroup().getTypes().contains(GroupType.DETAIL)) {
                        seriesCount += (seriesCount / inputSeriesCount) * (groupingFunctionsCount-1);
                    } else {
                        seriesCount = (seriesCount / inputSeriesCount) * groupingFunctionsCount;
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
