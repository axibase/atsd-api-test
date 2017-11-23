package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class SeriesQueryAggregateGroupOrderTest extends SeriesMethod {
    private String TEST_ENTITY1;
    private String TEST_ENTITY2;
    private String TEST_METRIC;

    @BeforeClass
    public void prepareData() throws Exception {
        TEST_ENTITY1 = Mocks.entity();
        TEST_ENTITY2 = Mocks.entity();

        TEST_METRIC = Mocks.metric();

        Series series1 = new Series(TEST_ENTITY1, TEST_METRIC);
        Series series2 = new Series(TEST_ENTITY2, TEST_METRIC);
        for (int i = 0; i < 20; i++) {
            Series series = i % 2 != 0 ? series1 : series2;
            int value = i % 2 != 0 ? 100 + i : 200 + i;
            series.addSamples(Sample.ofDateInteger(
                    TestUtil.addTimeUnitsInTimezone(
                            "2017-01-01T00:00:00Z",
                            ZoneId.of("Etc/UTC"),
                            TimeUnit.SECOND,
                            i),
                    value)
            );
        }

        insertSeriesCheck(series1, series2);
    }

    @DataProvider
    public Object[][] provideOrders() {
        return new Object[][] {
                {null},
                {0},
                {5},
                {-5}
        };
    }

    @Issue("4729")
    @Test(
            dataProvider = "provideOrders",
            description = "test query result with default Group/Aggregate order")
    public void testAggregateOrder(Integer order) throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        Aggregate aggregate = new Aggregate(
                AggregationType.MIN,
                new Period(10, TimeUnit.SECOND));
        if (order != null) {
            aggregate.setOrder(order);
        }
        query.setAggregate(aggregate);

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries1 = createSeries(TEST_ENTITY1,
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("101.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("111.0")));

        Series expectedSeries2 = createSeries(TEST_ENTITY2,
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("200.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("210.0")));

        assertEquals(
                result,
                Arrays.asList(expectedSeries1, expectedSeries2),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(
            dataProvider = "provideOrders",
            description = "test query result with default Group/Aggregate order")
    public void testGroupOrder(Integer order) throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        Group group = new Group(
                GroupType.MAX,
                new Period(5, TimeUnit.SECOND));
        if (order != null) {
            group.setOrder(order);
        }
        query.setGroup(group);

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("204.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:05.000Z", new BigDecimal("208.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("214.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:15.000Z", new BigDecimal("218.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with default Group/Aggregate order")
    public void testDefaultOrderGroupAggregate() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.COUNT,
                new Period(10, TimeUnit.SECOND)
        ));
        query.setGroup(new Group(
                GroupType.MAX,
                new Period(5, TimeUnit.SECOND)
        ));

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("2.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testExplicitEqualsOrderGroupAggregate() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.MIN,
                new Period(10, TimeUnit.SECOND),
                0
        ));
        query.setGroup(new Group(
                GroupType.COUNT,
                new Period(5, TimeUnit.SECOND),
                0
        ));

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("5.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("5.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testExplicitOrderGroupAggregate() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.COUNT,
                new Period(10, TimeUnit.SECOND),
                5
        ));
        query.setGroup(new Group(
                GroupType.MIN,
                new Period(5, TimeUnit.SECOND),
                3
        ));

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("2.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testExplicitOrderAggregateGroup() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.AVG,
                new Period(5, TimeUnit.SECOND),
                -3
        ));
        query.setGroup(new Group(
                GroupType.COUNT,
                new Period(10, TimeUnit.SECOND),
                2
        ));

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("4.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("4.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testAggregateLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.MAX,
                new Period(5, TimeUnit.SECOND)
        ));
        query.setLimit(1);
        query.setDirection("ASC");

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries1 = createSeries(TEST_ENTITY1,
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("103.0")));

        Series expectedSeries2 = createSeries(TEST_ENTITY2,
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("204.0")));

        assertEquals(
                result,
                Arrays.asList(expectedSeries1, expectedSeries2),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testGroupLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setGroup(new Group(
                GroupType.COUNT,
                new Period(10, TimeUnit.SECOND)
        ));
        query.setLimit(1);
        query.setDirection("ASC");

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("10.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testAggregateSeriesLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setAggregate(new Aggregate(
                AggregationType.MIN,
                new Period(10, TimeUnit.SECOND)
        ));
        query.setSeriesLimit(1);

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries(TEST_ENTITY1,
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("101.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("111.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    @Issue("4729")
    @Test(description = "test query result with explicit equals Group/Aggregate order")
    public void testGroupSeriesLimit() throws Exception {
        SeriesQuery query = new SeriesQuery(
                "*",
                TEST_METRIC,
                "2017-01-01T00:00:00Z",
                "2017-01-01T00:00:20Z");
        query.setGroup(new Group(
                GroupType.COUNT,
                new Period(5, TimeUnit.SECOND)
        ));
        query.setSeriesLimit(1);

        List<Series> result = querySeriesAsList(query);

        Series expectedSeries = createSeries("*",
                Sample.ofDateDecimal("2017-01-01T00:00:00.000Z", new BigDecimal("5.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:05.000Z", new BigDecimal("5.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:10.000Z", new BigDecimal("5.0")),
                Sample.ofDateDecimal("2017-01-01T00:00:15.000Z", new BigDecimal("5.0")));

        assertEquals(
                result,
                Collections.singletonList(expectedSeries),
                "Incorrect query result with default Group/Aggregate order");
    }

    private Series createSeries(String entity, Sample... samples) {
        Series series = new Series();
        series.setEntity(entity);
        series.setMetric(TEST_METRIC);
        series.addSamples(samples);
        return series;
    }
}
