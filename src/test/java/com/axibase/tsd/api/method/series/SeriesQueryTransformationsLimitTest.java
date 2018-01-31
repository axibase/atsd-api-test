package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.Interpolate;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.InterpolateFunction;
import com.axibase.tsd.api.model.series.query.transformation.rate.Rate;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.*;

/** Test that limit applied correctly when transformations queried. */
public class SeriesQueryTransformationsLimitTest extends SeriesMethod {

    private final String METRIC = Mocks.metric();
    private final String ENTITY_1 = Mocks.entity();
    private final String ENTITY_2 = Mocks.entity();
    private final List<String> ENTITIES = Arrays.asList(ENTITY_1, ENTITY_2);
    private final Sample[] SAMPLES_1 = {
            Sample.ofDateInteger("2017-01-01T00:10:00Z", 1),
            Sample.ofDateInteger("2017-01-01T00:30:00Z", 3),
            Sample.ofDateInteger("2017-01-01T00:50:00Z", 5),
            Sample.ofDateInteger("2017-01-01T01:10:00Z", 7)
    };
    private final Sample[] SAMPLES_2 = {
            Sample.ofDateInteger("2017-01-01T00:10:00Z", 1),
            Sample.ofDateInteger("2017-01-01T00:20:00Z", 2),
            Sample.ofDateInteger("2017-01-01T00:40:00Z", 4),
            Sample.ofDateInteger("2017-01-01T00:50:00Z", 5),
            Sample.ofDateInteger("2017-01-01T01:10:00Z", 7)
    };
    private final Interpolate INTERPOLATE =
            new Interpolate(InterpolateFunction.LINEAR, new Period(10, TimeUnit.MINUTE, PeriodAlignment.START_TIME));
    private final Aggregate AGGREGATE =
            new Aggregate(AggregationType.MAX, new Period(15, TimeUnit.MINUTE, PeriodAlignment.START_TIME));
    private final Rate RATE =
            new Rate(new Period(10, TimeUnit.MINUTE));
    private final Group GROUP =
            new Group(GroupType.SUM, new Period(10, TimeUnit.MINUTE, PeriodAlignment.START_TIME));

    @BeforeClass
    public void prepareData() throws Exception {

        Series series1 = new Series(ENTITY_1, METRIC);
        series1.addSamples(SAMPLES_1);

        Series series2 = new Series(ENTITY_2, METRIC);
        series2.addSamples(SAMPLES_2);

        insertSeriesCheck(series1, series2);
    }

    @AfterMethod(alwaysRun = true)
    private void clearTransformationsOrders() {
        AGGREGATE.setOrder(0);
        GROUP.setOrder(0);
        RATE.setOrder(0);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after interpolation")
    public void testInterpolateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setInterpolate(INTERPOLATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after aggregation")
    public void testAggregateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setAggregate(AGGREGATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after rate calculation")
    public void testRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setRate(RATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after the group transformation")
    public void testGroupLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        query.setGroup(GROUP);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after interpolation and aggregation")
    public void testInterpolateAndAggregateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setInterpolate(INTERPOLATE);
        query.setAggregate(AGGREGATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after interpolation and rate calculation")
    public void testInterpolateAndRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setInterpolate(INTERPOLATE);
        query.setRate(RATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after interpolation and grouping")
    public void testInterpolateAndGroupLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        query.setInterpolate(INTERPOLATE);
        query.setGroup(GROUP);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after aggregation and rate")
    public void testAggregateAndRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        AGGREGATE.setOrder(1);
        query.setAggregate(AGGREGATE);
        RATE.setOrder(2);
        query.setRate(RATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after rate calculation and aggregation")
    public void testRateAndAggregateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        RATE.setOrder(1);
        query.setRate(RATE);
        AGGREGATE.setOrder(2);
        query.setAggregate(AGGREGATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after grouping and rate calculation")
    public void testGroupAndRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        GROUP.setOrder(1);
        query.setGroup(GROUP);
        RATE.setOrder(2);
        query.setRate(RATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after rate calculation and grouping")
    public void testRateAndGroupLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        RATE.setOrder(1);
        query.setRate(RATE);
        GROUP.setOrder(2);
        query.setGroup(GROUP);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after grouping and aggregation")
    public void testGroupAndAggregateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        GROUP.setOrder(1);
        query.setGroup(GROUP);
        AGGREGATE.setOrder(2);
        query.setAggregate(AGGREGATE);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after aggregation and grouping")
    public void testAggregateAndGroupLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        AGGREGATE.setOrder(1);
        query.setAggregate(AGGREGATE);
        GROUP.setOrder(2);
        query.setGroup(GROUP);
        checkResponse(query);
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after all permutations of grouping, aggregation, and rate")
    public void testGroupAggregateRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                for (int k = 1; k < 3; k++) {
                    if (i != j && j != k && k != i) {
                        GROUP.setOrder(i);
                        query.setGroup(GROUP);
                        AGGREGATE.setOrder(j);
                        query.setAggregate(AGGREGATE);
                        RATE.setOrder(k);
                        query.setRate(RATE);
                        checkResponse(query);

                    }
                }
            }
        }
    }

    @Issue("4835")
    @Test(description = "test that limit is applied correctly after interpolation and all permutations of grouping, aggregation, and rate")
    public void testInterpolateGroupAggregateRateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery("*", METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query.setEntities(ENTITIES);
        query.setInterpolate(INTERPOLATE);
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                for (int k = 1; k < 3; k++) {
                    if (i != j && j != k && k != i) {
                        GROUP.setOrder(i);
                        query.setGroup(GROUP);
                        AGGREGATE.setOrder(j);
                        query.setAggregate(AGGREGATE);
                        RATE.setOrder(k);
                        query.setRate(RATE);
                        checkResponse(query);

                    }
                }
            }
        }
    }

    private void checkResponse(SeriesQuery query) {
        for (int i = 1; i < 4; i++) {
            query.setLimit(i);
            List<Series> result = querySeriesAsList(query);
            assertNotNull("Not null response expected", result);
            assertEquals("Single series expected in response", 1, result.size());
            int responseSeriesSize = result.get(0).getData().size();
            assertEquals("Wrong number of samples in response", i, responseSeriesSize);
        }
    }

    @Issue("4836")
    @Test(description = "test two identical limited queries with interpolation")
    public void testTwoIdenticalQueriesWithInterpolateLimit() throws Exception {
        SeriesQuery query1 = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query1.setInterpolate(INTERPOLATE);
        SeriesQuery query2 = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query2.setInterpolate(INTERPOLATE);
        checkResponse(query1, query2);
    }

    @Issue("4836")
    @Test(description = "test two different limited queries with interpolation")
    public void testTwoDifferentQueriesWithInterpolateLimit() throws Exception {
        SeriesQuery query1 = new SeriesQuery(ENTITY_1, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query1.setInterpolate(INTERPOLATE);
        SeriesQuery query2 = new SeriesQuery(ENTITY_2, METRIC, "2017-01-01T00:00:00Z", "2017-01-01T02:00:00Z");
        query2.setInterpolate(INTERPOLATE);
        checkResponse(query1, query2);
    }

    private void checkResponse(SeriesQuery query1, SeriesQuery query2) {
        for (int i = 1; i < 4; i++) {
            for (int j = 1; j < 4; j++) {
                query1.setLimit(i);
                query2.setLimit(j);
                List<Series> result = querySeriesAsList(query1, query2);
                assertNotNull("Not null response expected", result);
                assertEquals("Two series expected in response", 2, result.size());
                checkLimits(result.get(0).getData().size(), result.get(1).getData().size(), i, j);
            }
        }

    }

    private void checkLimits(int size1, int size2, int limit1, int limit2) {
        assertTrue("Series size in response doesn't match series limit in query",
                (size1 == limit1 && size2 == limit2) || (size1 == limit2 && size2 == limit1));
    }
}
