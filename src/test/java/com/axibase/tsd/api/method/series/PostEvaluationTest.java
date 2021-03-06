package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.evaluate.Evaluate;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Forecast;
import com.axibase.tsd.api.model.series.query.transformation.forecast.Horizon;
import com.axibase.tsd.api.model.series.query.transformation.forecast.SSASettings;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.Interpolate;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.InterpolateFunction;
import com.axibase.tsd.api.model.series.query.transformation.rate.Rate;
import com.axibase.tsd.api.util.CommonAssertions;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.model.series.query.transformation.Transformation.*;
import static com.axibase.tsd.api.util.CommonAssertions.*;
import static org.testng.Assert.*;


/**
 * Test that transformations are properly applied to series generated during the evaluation stage.
 * We need this test because ATSD uses special code to organize series pre-/post- processing
 * before/after evaluation of user defined expression.
 *
 * Note that keys and other meta-information (grouping, aggregation, etc.) are not checked.
 * Check count of series, count of samples, and series values in response.
 */
public class PostEvaluationTest extends SeriesMethod {

    /** Invariant part of the query. Test methods append appropriate series transformations to it. */
    private SeriesQuery baseQuery;

    /**
     User defined expression of the {@link #baseQuery} generates the following series.
     We test that series transformations properly handle these series.
     Date: 2019-01-01

     series | series key                           |          |          |          |          |          |
            | metric:entity:tags                   | 00:00:00 | 00:05:00 | 00:15:00 | 00:20:00 | 00:30:00 |
     -------|--------------------------------------|----------|----------|----------|----------|----------|
          0 | metric-1:entity-1:                   |       1  |       2  |       3  |       4  |       5  |
          1 | metric-1:entity-1:tag-name=tag-value |       2  |       4  |       6  |       8  |      10  |
          2 | metric-1:entity-2:                   |       3  |       6  |       9  |      12  |      15  |
          3 | metric-1:entity-2:tag-name=tag-value |       4  |       8  |      12  |      16  |      20  |
          4 | metric-2:entity-1:                   |       5  |      10  |      15  |      20  |      25  |

     */
    int seriesCount = 5;

    @BeforeClass
    public void prepare() throws Exception {
        // Inserted series doesn't matter because we test series generated by expression.
        String metric = Mocks.metric();
        String entity = Mocks.entity();
        String startDate = "2019-01-01T00:00:00Z";
        String endDate = "2019-01-02T00:00:00Z";
        insertSeries(metric, entity, startDate, endDate);
        buildBaseQuery(metric, entity, startDate, endDate);
    }

    @Test(description = "Test that evaluation produces expected series.")
    public void testBaseQuery() {
        List<Series> seriesList = querySeriesAsList(baseQuery);
        assertEquals(seriesList.size(), seriesCount);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "1", "2", "3", "4", "5");
        checkValues(seriesList.get(1), "2", "4", "6", "8", "10");
        checkValues(seriesList.get(2), "3", "6", "9", "12", "15");
        checkValues(seriesList.get(3), "4", "8", "12", "16", "20");
        checkValues(seriesList.get(4), "5", "10", "15", "20", "25");
    }

    @Test(description = "Test samples limit with evaluation.")
    public void testSampleLimit() {
        SeriesQuery query = baseQuery.withLimit(3);
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), seriesCount);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "3", "4", "5");
        checkValues(seriesList.get(1), "6", "8", "10");
        checkValues(seriesList.get(2), "9", "12", "15");
        checkValues(seriesList.get(3), "12", "16", "20");
        checkValues(seriesList.get(4), "15", "20", "25");
    }

    @Test(description = "Test series limit with evaluation.")
    public void testSeriesLimit() {
        SeriesQuery query = baseQuery.withSeriesLimit(4);
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 4);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "1", "2", "3", "4", "5");
        checkValues(seriesList.get(1), "2", "4", "6", "8", "10");
        checkValues(seriesList.get(2), "3", "6", "9", "12", "15");
        checkValues(seriesList.get(3), "4", "8", "12", "16", "20");
    }

    @Test(description = "Calculate rate of evaluation-generated series.")
    public void testRate() {
        SeriesQuery query = baseQuery
                .withRate(new Rate(new Period(10, TimeUnit.MINUTE)))
                .withTransformationOrder(Arrays.asList(EVALUATE, RATE));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), seriesCount);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "2", "1", "2", "1");
        checkValues(seriesList.get(1), "4", "2", "4", "2");
        checkValues(seriesList.get(2), "6", "3", "6", "3");
        checkValues(seriesList.get(3), "8", "4", "8", "4");
        checkValues(seriesList.get(4), "10", "5", "10", "5");
    }

    @Test(description = "Calculate rate of evaluation-generated series and limit count of series and samples in response.")
    public void testRateAndLimits() {
        SeriesQuery query = baseQuery
                .withRate(new Rate(new Period(10, TimeUnit.MINUTE)))
                .withTransformationOrder(Arrays.asList(EVALUATE, RATE))
                .withLimit(2)
                .withSeriesLimit(3);
        List<Series> seriesList = querySeriesAsList(query);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "2", "1");
        checkValues(seriesList.get(1), "4", "2");
        checkValues(seriesList.get(2), "6", "3");
    }

    @Test(description = "Interpolate evaluation-generated series.")
    public void testInterpolation() {
        SeriesQuery query = baseQuery
                .withInterpolate(new Interpolate(InterpolateFunction.LINEAR, period(5)))
                .withTransformationOrder(Arrays.asList(EVALUATE, INTERPOLATE));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), seriesCount);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "1", "2", "2.5", "3", "4", "4.5", "5");
        checkValues(seriesList.get(1), "2", "4", "5", "6", "8", "9", "10");
        checkValues(seriesList.get(2), "3", "6", "7.5", "9", "12", "13.5", "15");
        checkValues(seriesList.get(3), "4", "8", "10", "12", "16", "18", "20");
        checkValues(seriesList.get(4), "5", "10", "12.5", "15", "20", "22.5", "25");
    }

    @Test(description = "Apply single aggregation function to evaluation-generated series.")
    public void testSingleAggregation() {
        SeriesQuery query = baseQuery
                .withAggregate(new Aggregate(AggregationType.COUNT, period(10)))
                .withTransformationOrder(Arrays.asList(EVALUATE, AGGREGATE));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), seriesCount);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "2", "1", "1", "1");
        checkValues(seriesList.get(1), "2", "1", "1", "1");
        checkValues(seriesList.get(2), "2", "1", "1", "1");
        checkValues(seriesList.get(3), "2", "1", "1", "1");
        checkValues(seriesList.get(4), "2", "1", "1", "1");
    }

    @Test(description = "Apply several aggregation functions to evaluation-generated series.")
    public void testMultiAggregation() {
        Aggregate aggregationSettings = new Aggregate()
                .setPeriod(period(20))
                .addType(AggregationType.COUNT)
                .addType(AggregationType.SUM)
                .addType(AggregationType.MAX);
        SeriesQuery query = baseQuery
                .withAggregate(aggregationSettings)
                .withTransformationOrder(Arrays.asList(EVALUATE, AGGREGATE));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 3 * seriesCount);
        CommonAssertions.assertSeriesSize(seriesList, 2);
        CommonAssertions.checkValues(seriesList,
                new String[][]{
                        {"3", "2"},
                        {"6", "9"},
                        {"12", "18"},
                        {"3", "5"},
                        {"6", "10"}
                }
        );
    }

    @Test(description = "Apply several aggregation functions and limits to evaluation-generated series.")
    public void testMultiAggregationWithLimits() {
        Aggregate aggregationSettings = new Aggregate()
                .setPeriod(period(10))
                .addType(AggregationType.COUNT)
                .addType(AggregationType.SUM)
                .addType(AggregationType.MAX);
        SeriesQuery query = baseQuery
                .withAggregate(aggregationSettings)
                .withTransformationOrder(Arrays.asList(EVALUATE, AGGREGATE))
                .withSeriesLimit(7)
                .withLimit(3);
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 7);
        CommonAssertions.assertSeriesSize(seriesList, 3);
    }

    @Test(description = "Group evaluation-generated series by metric.")
    public void testGroup() {
        SeriesQuery query = baseQuery
                .withGroup(new Group(GroupType.SUM))
                .withTransformationOrder(Arrays.asList(EVALUATE, GROUP));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 2);
        CommonAssertions.assertSeriesSize(seriesList, 5);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "10", "20", "30", "40", "50");
        checkValues(seriesList.get(1), "5", "10", "15", "20", "25");
    }

    @Test(description = "Group evaluation-generated series by metric and entity.")
    public void testGroupByEntity() {
        SeriesQuery query = baseQuery
                .withGroup(new Group(GroupType.SUM).setGroupByEntityAndTags(Collections.emptyList()))
                .withTransformationOrder(Arrays.asList(EVALUATE, GROUP));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 3);
        CommonAssertions.assertSeriesSize(seriesList, 5);
        Collections.sort(seriesList);
        checkValues(seriesList.get(0), "3", "6", "9", "12", "15");
        checkValues(seriesList.get(1), "7", "14", "21", "28", "35");
        checkValues(seriesList.get(2), "5", "10", "15", "20", "25");
    }

    @Test(description = "Group evaluation-generated series by metric and entity and limit count of samples and series in response.")
    public void testGroupAndLimits() {
        SeriesQuery query = baseQuery
                .withGroup(new Group(GroupType.SUM).setGroupByEntityAndTags(Collections.emptyList()))
                .withTransformationOrder(Arrays.asList(EVALUATE, GROUP))
                .setSeriesLimit(2)
                .setLimit(3).setDirection("ASC");
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 2);
        checkGrouped(seriesList.get(0));
        checkGrouped(seriesList.get(1));
    }

    @Test(description = "Aggregate and group evaluation-generated series by metric.")
    public void testAggregateAndGroup() {
        Aggregate aggregationSettings = new Aggregate()
                .setPeriod(period(20))
                .addType(AggregationType.COUNT)
                .addType(AggregationType.AVG)
                .addType(AggregationType.MIN);
        Group groupSettings = new Group(GroupType.SUM).setPeriod(period(20));
        SeriesQuery query = baseQuery
                .withAggregate(aggregationSettings)
                .withGroup(groupSettings);

        // aggregate then group
        query = query.withTransformationOrder(Arrays.asList(EVALUATE, AGGREGATE, GROUP));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 6);
        CommonAssertions.checkValues(seriesList,
                new String[][] {
                        {"12", "8"},
                        {"3", "2"},
                        {"10", "40"},
                        {"5", "20"},
                        {"20", "45"},
                        {"10", "22.5"}
                }
        );

        // group then aggregate
        query = query.withTransformationOrder(Arrays.asList(EVALUATE, GROUP, AGGREGATE));
        seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 6);
        CommonAssertions.assertSeriesSize(seriesList, 2);
        CommonAssertions.checkValues(seriesList,
                new String[]{"1", "1"}, new String[]{"60", "90"}, new String[]{"30", "45"});
    }

    @Test(description = "Interpolate and forecast evaluation-generated series.")
    public void testForecast() {
        Interpolate interpolationSettings = new Interpolate(InterpolateFunction.LINEAR, period(1));
        Forecast forecastSettings = new Forecast()
                .setHorizon(new Horizon().setLength(31))
                .setSsa(new SSASettings())
                .setInclude(Arrays.asList(SeriesType.HISTORY, SeriesType.FORECAST));
        SeriesQuery query = baseQuery
                .withInterpolate(interpolationSettings)
                .withForecast(forecastSettings)
                .withTransformationOrder(Arrays.asList(EVALUATE, INTERPOLATE, FORECAST));
        List<Series> seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 2 * seriesCount);
        CommonAssertions.assertSeriesSize(seriesList, 31);
        query = query.withSeriesLimit(5).withLimit(4);
        seriesList = querySeriesAsList(query);
        assertEquals(seriesList.size(), 5);
        CommonAssertions.assertSeriesSize(seriesList, 4);
    }

    /** Inserted series doesn't matter, because we test fixed series generated by user defined expression. */
    private void insertSeries(String metric, String entity, String startDate, String endDate) throws Exception {
        Series series = new Series(entity, metric);
        series.addSamples(Sample.ofDateInteger(startDate, 111));
        insertSeriesCheck(series);
    }

    private void buildBaseQuery(String metric, String entity, String startDate, String endDate) {
        baseQuery = new SeriesQuery(entity, metric, startDate, endDate);

        StringBuilder sb = new StringBuilder("List result = new ArrayList(); ")
                .append("String[] timestamps = new String[]{'2019-01-01T00:00:00Z', '2019-01-01T00:05:00Z', '2019-01-01T00:15:00Z', '2019-01-01T00:20:00Z', '2019-01-01T00:30:00Z'}; ");
        addSeriesToExpression(sb, "{1.0,  2.0,  3.0,  4.0,  5.0}, 'metric-1', 'entity-1'");
        addSeriesToExpression(sb, "{2.0,  4.0,  6.0,  8.0, 10.0}, 'metric-1', 'entity-1', 'tag-name', 'tag-value'");
        addSeriesToExpression(sb, "{3.0,  6.0,  9.0, 12.0, 15.0}, 'metric-1', 'entity-2'");
        addSeriesToExpression(sb, "{4.0,  8.0, 12.0, 16.0, 20.0}, 'metric-1', 'entity-2', 'tag-name', 'tag-value'");
        addSeriesToExpression(sb, "{5.0, 10.0, 15.0, 20.0, 25.0}, 'metric-2', 'entity-1'");
        sb.append("return result;");

        baseQuery.setEvaluate(new Evaluate(sb.toString()));
    }

    private void addSeriesToExpression(StringBuilder expressionBuilder, String series) {
        expressionBuilder.append("result.add(Series.of(timestamps, new double[]").append(series).append(")); ");
    }

    private Period period(int minutes) {
        return new Period(minutes, TimeUnit.MINUTE, PeriodAlignment.CALENDAR, "UTC");
    }

    private void checkGrouped(Series series) {
        String key = series.getMetric() + series.getEntity();
        switch(key) {
            case "metric-1entity-1":
                checkValues(series, "3", "6", "9");
                break;
            case "metric-1entity-2":
                checkValues(series, "7", "14", "21");
                break;
            case "metric-2entity-1":
                checkValues(series, "5", "10", "15");
                break;
        }
    }
}
