package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.SampleTriplet;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryGroupExampleTest extends SeriesMethod {
    private final static String FIRST_ENTITY = "series-group-example-1";
    private final static String SECOND_ENTITY = "series-group-example-2";
    private final static String GROUPED_METRIC = "metric-group-example-1";
    private static List<SampleTriplet> exampleData = new ArrayList<>();

    static {
        Registry.Entity.register(FIRST_ENTITY);
        Registry.Entity.register(SECOND_ENTITY);
        Registry.Metric.register(GROUPED_METRIC);
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:00.000Z", 1));
        exampleData.add(new SampleTriplet(SECOND_ENTITY, "2016-06-25T08:00:00.000Z", 11));
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:05.000Z", 3));
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:10.000Z", 5));
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:15.000Z", 8));
        exampleData.add(new SampleTriplet(SECOND_ENTITY, "2016-06-25T08:00:15.000Z", 8));
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:30.000Z", 3));
        exampleData.add(new SampleTriplet(SECOND_ENTITY, "2016-06-25T08:00:30.000Z", 13));
        exampleData.add(new SampleTriplet(FIRST_ENTITY, "2016-06-25T08:00:45.000Z", 5));
        exampleData.add(new SampleTriplet(SECOND_ENTITY, "2016-06-25T08:00:45.000Z", 15));
        exampleData.add(new SampleTriplet(SECOND_ENTITY, "2016-06-25T08:00:59.000Z", 19));
    }


    @BeforeClass
    public void insertSeriesSet() throws Exception {
        Series firstSeries = new Series();
        firstSeries.setEntity(FIRST_ENTITY);
        firstSeries.setMetric(GROUPED_METRIC);

        Series secondSeries = new Series();
        secondSeries.setEntity(SECOND_ENTITY);
        secondSeries.setMetric(GROUPED_METRIC);

        String currentEntity;
        for (SampleTriplet sampleTriplet : exampleData) {
            currentEntity = sampleTriplet.getLeft();
            switch (currentEntity) {
                case FIRST_ENTITY:
                    firstSeries.addData(new Sample(sampleTriplet.getMiddle(), sampleTriplet.getRight()));
                    break;
                case SECOND_ENTITY:
                    secondSeries.addData(new Sample(sampleTriplet.getMiddle(), sampleTriplet.getRight()));
                    break;
                default:
                    throw new IllegalStateException("Unexpected entity in exampleData");
            }
        }
        insertSeriesCheck(Arrays.asList(firstSeries, secondSeries));
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/exampleData/series/group.md#no-aggregation
     */
    @Test
    public void testExampleSum() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = new HashMap<>();
        group.put("type", "SUM");
        query.put("group", group);

        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "12.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:05.000Z", "3.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "5.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:15.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:45.000Z", "20.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:59.000Z", "19.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#truncation
     */
    @Test
    public void testExampleSumTruncate() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:01Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = new HashMap<>();
        group.put("type", "SUM");
        group.put("truncate", true);
        query.put("group", group);


        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:15.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:45.000Z", "20.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#extend
     */
    @Test
    public void testExampleSumExtend() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:01Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = new HashMap<>();
        group.put("type", "SUM");
        Map<String, Object> interpolate = new HashMap<>();
        interpolate.put("extend", true);
        group.put("interpolate", interpolate);
        query.put("group", group);

        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:05.000Z", "11.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "13.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:15.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:45.000Z", "20.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:59.000Z", "24.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#interpolation-1
     */
    @Test
    public void testExampleSumInterpolation() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = new HashMap<>();
        group.put("type", "SUM");
        Map<String, Object> interpolate = new HashMap<>();
        interpolate.put("type", "PREVIOUS");
        group.put("interpolate", interpolate);
        query.put("group", group);

        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "12.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:05.000Z", "14.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:15.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:45.000Z", "20.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:59.000Z", "19.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group-aggregation
     */
    @Test
    public void testExampleSumAggregation() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = prepareGroupParameters("SUM", null, 10, TimeUnit.SECOND);
        query.put("group", group);


        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "15.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "21.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:40.000Z", "20.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:50.000Z", "19.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group-aggregation
     */
    @Test
    public void testExampleSumGroupAggregation() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = prepareGroupParameters("SUM", null, 10, TimeUnit.SECOND);
        query.put("group", group);
        query.put("aggregate", group); //aggregate parameter is equal to group parameter


        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "15.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "21.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "16.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:40.000Z", "20.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:50.000Z", "19.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#aggregation---group
     */
    @Test
    public void testExampleSumAggregationToGroup() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = prepareGroupParameters("SUM", 1, null, null);
        query.put("group", group);

        Map<String, Object> aggregate = prepareGroupParameters("COUNT", 0, 10, TimeUnit.SECOND);
        query.put("aggregate", aggregate);

        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "3.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "3.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "2.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:40.000Z", "2.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:50.000Z", "1.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }

    /**
     * #2995
     *
     * @see https://github.com/axibase/atsd-docs/blob/master/api/data/series/group.md#group---aggregation
     */
    @Test
    public void testExampleSumGroupToAggregation() throws Exception {
        Map<String, Object> query = prepareDefaultQuery("2016-06-25T08:00:00Z", "2016-06-25T08:01:00Z");

        Map<String, Object> group = prepareGroupParameters("SUM", 0, 1, TimeUnit.MILLISECOND);
        query.put("group", group);

        Map<String, Object> aggregate = prepareGroupParameters("COUNT", 1, 10, TimeUnit.SECOND);
        query.put("aggregate", aggregate);

        List<Sample> expectedSamples = new ArrayList<>();
        expectedSamples.add(new Sample("2016-06-25T08:00:00.000Z", "2.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:10.000Z", "2.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:30.000Z", "1.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:40.000Z", "1.0"));
        expectedSamples.add(new Sample("2016-06-25T08:00:50.000Z", "1.0"));

        List<Series> groupedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, groupedSeries.size());
        List<Sample> givenSamples = groupedSeries.get(0).getData();

        final String actual = jacksonMapper.writeValueAsString(givenSamples);
        final String expected = jacksonMapper.writeValueAsString(expectedSamples);
        assertEquals("Grouped series do not match to expected", expected, actual);
    }



    private Map<String, Object> prepareDefaultQuery(String startDate, String endDate) {
        Map<String, Object> defaultQuery = new HashMap<>();
        defaultQuery.put("startDate", startDate);
        defaultQuery.put("endDate", endDate);
        defaultQuery.put("entities", Arrays.asList(FIRST_ENTITY, SECOND_ENTITY));
        defaultQuery.put("metric", GROUPED_METRIC);
        return defaultQuery;
    }

    private Map<String, Object> prepareGroupParameters(String type, Integer order, Integer periodCount, TimeUnit periodUnit) {
        Map<String, Object> groupParameter = new HashMap<>();
        if(type != null)
            groupParameter.put("type", type);
        if(order != null)
            groupParameter.put("order", order);
        if(periodCount != null && periodUnit != null) {
            Map<String, Object> period = new HashMap<>();
            period.put("count", periodCount);
            period.put("unit", periodUnit);
            groupParameter.put("period", period);
        }
        return groupParameter;
    }

}
