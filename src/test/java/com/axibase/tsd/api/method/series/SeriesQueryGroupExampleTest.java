package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.*;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryGroupExampleTest extends SeriesMethod {
    private final static String FIRST_ENTITY = "series-group-example-1";
    private final static String SECOND_ENTITY = "series-group-example-2";
    private final static String GROUPED_METRIC = "metric-group-example-1";

    /**
     * dataset details:
     * https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#detailed-data-by-series
     */
    @BeforeClass
    public void insertSeriesSet() throws Exception {
        Series firstSeries = new Series(FIRST_ENTITY, GROUPED_METRIC);
        Series secondSeries = new Series(SECOND_ENTITY, GROUPED_METRIC);

        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 1));
        secondSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 11));
        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 3));
        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 5));
        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 8));
        secondSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 8));
        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 3));
        secondSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 13));
        firstSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 5));
        secondSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 15));
        secondSeries.addSamples(Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 19));
        insertSeriesCheck(Arrays.asList(firstSeries, secondSeries));
    }

    @Issue("2995")
    public void testExampleSum() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");
        query.setGroup(new Group(GroupType.SUM));

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 12),
                Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 3),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 5),
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#interpolation-1")
    public void testExampleSumInterpolation() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Group group = new Group(GroupType.SUM);
        group.setInterpolate(new Interpolate(InterpolateType.PREVIOUS));
        query.setGroup(group);

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 12),
                Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 14),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group-aggregation")
    public void testExampleSumAggregation() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");
        query.setGroup(new Group(GroupType.SUM, new Interval(10, TimeUnit.SECOND)));

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 15),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 21),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:40.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:50.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group-aggregation")
    public void testExampleSumGroupAggregation() throws Exception {
        final Interval period = new Interval(10, TimeUnit.SECOND);

        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");
        query.setGroup(new Group(GroupType.SUM, period));
        query.setAggregate(new Aggregate(AggregationType.SUM, period));

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 15),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 21),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:40.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:50.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#aggregation---group")
    public void testExampleSumAggregationToGroup() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");
        query.setGroup(new Group(GroupType.SUM, null, 1));
        query.setAggregate(new Aggregate(AggregationType.COUNT, new Interval(10, TimeUnit.SECOND)));

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 3),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 3),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 2),
                Sample.ofDateInteger("2016-06-25T08:00:40.000Z", 2),
                Sample.ofDateInteger("2016-06-25T08:00:50.000Z", 1)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group---aggregation")
    public void testExampleSumGroupToAggregation() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");
        query.setGroup(new Group(GroupType.SUM, new Interval(1, TimeUnit.MILLISECOND), 0));
        query.setAggregate(new Aggregate(AggregationType.COUNT, new Interval(10, TimeUnit.SECOND), 1));

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 2),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 2),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 1),
                Sample.ofDateInteger("2016-06-25T08:00:40.000Z", 1),
                Sample.ofDateInteger("2016-06-25T08:00:50.000Z", 1)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#truncation")
    public void testExampleSumTruncate() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:01Z", "2016-06-25T08:01:00Z");

        Group group = new Group(GroupType.SUM);
        group.setTruncate(true);

        query.setGroup(group);

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2995")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#extend")
    public void testExampleSumExtendTrue() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:01Z", "2016-06-25T08:01:00Z");

        Group group = new Group(GroupType.SUM);
        final Interpolate interpolate = new Interpolate(InterpolateType.NONE);
        interpolate.setExtend(true);
        group.setInterpolate(interpolate);

        query.setGroup(group);

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 11),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 13),
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 24)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2997")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/exampleData/series/group.md#no-aggregation")
    public void testExampleSumExtendFalse() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Group group = new Group(GroupType.SUM);
        final Interpolate interpolate = new Interpolate(InterpolateType.NONE);
        interpolate.setExtend(false);
        group.setInterpolate(interpolate);

        query.setGroup(group);

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 12),
                Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 3),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 5),
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    @Issue("2997")
    @Test(description = "https://github.com/axibase/atsd-docs/blob/master/api/exampleData/series/group.md#no-aggregation")
    public void testExampleSumExtendNull() throws Exception {
        SeriesQuery query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Group group = new Group(GroupType.SUM);
        final Interpolate interpolate = new Interpolate(InterpolateType.NONE);
        group.setInterpolate(interpolate);

        query.setGroup(group);

        List<Sample> expectedSamples = Arrays.asList(
                Sample.ofDateInteger("2016-06-25T08:00:00.000Z", 12),
                Sample.ofDateInteger("2016-06-25T08:00:05.000Z", 3),
                Sample.ofDateInteger("2016-06-25T08:00:10.000Z", 5),
                Sample.ofDateInteger("2016-06-25T08:00:15.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:30.000Z", 16),
                Sample.ofDateInteger("2016-06-25T08:00:45.000Z", 20),
                Sample.ofDateInteger("2016-06-25T08:00:59.000Z", 19)
        );

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertTrue("Grouped series do not match to expected", compareJsonString(expected, actual));
    }

    private SeriesQuery prepareDefaultQuery(String startDate, String endDate) {
        SeriesQuery seriesQuery = new SeriesQuery();
        seriesQuery.setMetric(GROUPED_METRIC);
        seriesQuery.setEntities(Arrays.asList(FIRST_ENTITY, SECOND_ENTITY));
        seriesQuery.setStartDate(startDate);
        seriesQuery.setEndDate(endDate);
        return seriesQuery;
    }
}
