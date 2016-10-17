package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SeriesQueryExactMatch extends SeriesMethod {
    private static final String exactMatchEntityName = "series-query-exactmatch-entity-1";
    private static final String exactMatchMetricName = "series-query-exactmatch-metric-1";
    private static final Series seriesA = new Series(exactMatchEntityName, exactMatchMetricName);
    private static final Series seriesB = new Series();
    private static final Series seriesC = new Series();
    private static final Series seriesD = new Series();


    @BeforeClass
    public void prepareDataset() throws Exception {
        seriesA.addTag("tag-1", "val-1");
        seriesA.addTag("tag-2", "val-2");
        seriesA.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));

        seriesB.setEntity(exactMatchEntityName);
        seriesB.setMetric(exactMatchMetricName);
        seriesB.addTag("tag-1", "val-1");
        seriesB.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));

        seriesC.setEntity(exactMatchEntityName);
        seriesC.setMetric(exactMatchMetricName);
        seriesC.addTag("tag-2", "val-2");
        seriesC.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));

        seriesD.setEntity(exactMatchEntityName);
        seriesD.setMetric(exactMatchMetricName);
        seriesD.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));

        insertSeriesCheck(Arrays.asList(seriesA, seriesB, seriesC, seriesD));
    }

    /**
     * #3002
     * strict no tags => only seriesD should be received
     */
    @Test
    public void testExactTrueNoKey() throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE, new HashMap<String, String>());
        seriesQuery.setExactMatch(true);
        Response response = querySeries(seriesQuery);

        final String given = response.readEntity(String.class);
        Assert.assertEquals(calculateJsonArraySize(given), 1, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(seriesD));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     * soft no tags => all series will be received
     */
    @Test
    public void testExactFalseNoKey() throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE, new HashMap<String, String>());
        seriesQuery.setExactMatch(false);
        Response response = querySeries(seriesQuery);

        final String given = response.readEntity(String.class);
        Assert.assertEquals(calculateJsonArraySize(given), 4, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB, seriesC, seriesD));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     * strict match tags => only series with specified tag (tag-1=val-1) will be received
     */
    @Test
    public void testExactTrueTagMatch() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag-1", "val-1");
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE, tags);
        seriesQuery.setExactMatch(true);
        Response response = querySeries(seriesQuery);

        final String given = response.readEntity(String.class);
        Assert.assertEquals(calculateJsonArraySize(given), 1, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesB));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     * soft match tags => series which has the specified tag (tag-1=val-1) will be received
     */
    @Test
    public void testExactFalseTagMatch() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag-1", "val-1");
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE, tags);
        seriesQuery.setExactMatch(false);
        Response response = querySeries(seriesQuery);

        final String given = response.readEntity(String.class);
        Assert.assertEquals(calculateJsonArraySize(given), 2, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /*
    * #3371 test that wildcard for existing entity unfolded correctly
    */
    @Test
    public void testWildcardInEntityName() throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery("series-query-exactmatch-entity*", exactMatchMetricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        seriesQuery.setExactMatch(true);
        Response response = querySeries(seriesQuery);

        final String given = response.readEntity(String.class);
        Assert.assertEquals(calculateJsonArraySize(given), 1, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesD));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");

    }
}
