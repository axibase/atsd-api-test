package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.Transformation;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.downsample.Downsample;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Forecast;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.Interpolate;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.InterpolateFunction;
import com.axibase.tsd.api.model.series.query.transformation.rate.Rate;
import com.axibase.tsd.api.model.series.query.transformation.smooth.Smooth;
import com.axibase.tsd.api.model.series.query.transformation.smooth.SmoothingType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.apache.commons.collections4.iterators.PermutationIterator;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Take series transformations {@link Transformation#values()} except for {@link Transformation#FORECAST} and {@link Transformation#EVALUATE}.
 * Check that response contains correct number of generated series for each permutation of the transformations.
 */
public class TransformationPermutationsTest extends SeriesMethod {

    private int inputSeriesCount;
    private SeriesQuery query;

    int days = 1;
    String startDate = "2019-01-01T00:00:00Z";
    String endDate = "2019-01-02T00:00:00Z";

    /** Transformations under test. */
    List<Transformation> transformations = Arrays.asList(
            Transformation.AGGREGATE,
            Transformation.DOWNSAMPLE,
            Transformation.GROUP,
            Transformation.INTERPOLATE,
            Transformation.RATE,
            Transformation.SMOOTH);
    Aggregate aggregationSettings;
    Group groupSettings;
    Rate rateSettings;
    Interpolate interpolationSettings;
    Smooth smoothSettings;
    Downsample downsampleSettings;
    Forecast forecastSettings;

    @BeforeClass
    public void prepareData() throws Exception {
        String metric = Mocks.metric();
        insertSeries(metric);
        query = new SeriesQuery( "*", metric, startDate, endDate);
        setUpAggregation();
        setUpGrouping();
        setUpRate();
        setUpInterpolate();
        setUpSmooth();
        seUpDownsample();
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

    /**
     * Regular series.
     * Duration - {@link #days}.
     * From {@link #startDate} to {@link #endDate}.
     * Inter-samples interval 30 seconds.
     * All created series are the same constant function. */
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
        List<AggregationType> aggregationFunctions = Arrays.asList(
                AggregationType.AVG, AggregationType.SUM, AggregationType.FIRST);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);
        aggregationSettings = new Aggregate()
                .setPeriod(period)
                .setTypes(aggregationFunctions)
                .setInterpolate(interp);
        query.setAggregate(aggregationSettings);
    }

    private void setUpGrouping() {
        Period period = new Period(5, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        List<GroupType> aggregationFunctions = Arrays.asList(GroupType.FIRST, GroupType.SUM);
        AggregationInterpolate interp = new AggregationInterpolate(AggregationInterpolateType.LINEAR, true);
        groupSettings = new Group()
                .setPeriod(period)
                .setTypes(aggregationFunctions)
                .setInterpolate(interp);
        query.setGroup(groupSettings);
    }

    private void setUpRate() {
        Period period = new Period(1, TimeUnit.MINUTE);
        rateSettings = new Rate(period);
        rateSettings.setCounter(false);
        query.setRate(rateSettings);
    }

    private void setUpInterpolate() {
        Period period = new Period(10, TimeUnit.MINUTE, PeriodAlignment.START_TIME);
        interpolationSettings = new Interpolate(InterpolateFunction.LINEAR, period);
        query.setInterpolate(interpolationSettings);
    }

    private void setUpSmooth() {
        smoothSettings = new Smooth().setType(SmoothingType.EMA).setFactor(0.9);
        query.setSmooth(smoothSettings);
    }

    private void seUpDownsample() {
        downsampleSettings = new Downsample().setDifference(BigDecimal.ZERO);
        query.setDownsample(downsampleSettings);
    }

    /**
     * @return Array of all permutations of the {@link #transformations}.
     */
    @DataProvider(name = "permutations", parallel = true)
    Object[][] permuteTransformations() {

        int permutationsCount = (int) CombinatoricsUtils.factorial(transformations.size());
        Object[][] permutations = new Object[permutationsCount][1];

        PermutationIterator<Transformation> iterator = new PermutationIterator<>(transformations);
        int permutationIndex = 0;
        while (iterator.hasNext()) {
            List<Transformation> permutation = iterator.next();
            permutations[permutationIndex][0] = permutation;
            permutationIndex++;
        }
        return permutations;
    }


    @Test(dataProvider = "permutations",
            description = "Take series transformations {@link Transformation#values()} except for " +
                    "{@link Transformation#FORECAST}. Create query which has these transformations. " +
                    "Check that response contains correct number of generated series " +
                    "for each permutation of the transformations.")
    public void test(List<Transformation> permutation) {
        SeriesQuery testQuery = query.withTransformationOrder(permutation);
        List<Series> seriesList = querySeriesAsList(testQuery);
        int expectedSeriesCount = countExpectedSeries(permutation);
        assertEquals(seriesList.size(), expectedSeriesCount);
    }

    private int countExpectedSeries(List<Transformation> permutation) {
        int seriesCount = inputSeriesCount;
        for (Transformation transformation : permutation) {
            switch (transformation) {
                case INTERPOLATE:
                case RATE:
                case SMOOTH:
                case DOWNSAMPLE:
                    break;
                case GROUP:
                    int groupingFunctionsCount = groupSettings.getTypes().size();
                    seriesCount = (seriesCount / inputSeriesCount) * groupingFunctionsCount;
                    break;
                case AGGREGATE:
                    int aggregationFunctionsCount = aggregationSettings.getTypes().size();
                    seriesCount *= aggregationFunctionsCount;
                    break;
                case FORECAST:
                    int factor = 0;
                    if (forecastSettings.includeHistory()) factor++;
                    int algorithmsCount = forecastSettings.algorithmsCount();
                    if (forecastSettings.includeReconstructed()) factor += algorithmsCount;
                    if (forecastSettings.includeForecast()) factor += algorithmsCount;
                    seriesCount *= factor;
                    break;
            }
        }
        return seriesCount;
    }

}
