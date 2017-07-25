package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryExactMatch extends SeriesMethod {
    private static final String exactMatchEntityName = "series-query-exactmatch-entity-1";
    private static final String exactMatchMetricName = "series-query-exactmatch-metric-1";
    private static Series seriesA;
    private static Series seriesB;
    private static Series seriesC;
    private static Series seriesD;


    @BeforeClass
    public void prepareDataset() throws Exception {
        seriesA = new Series(exactMatchEntityName, exactMatchMetricName, "tag-1", "val-1", "tag-2", "val-2");
        seriesA.addSamples(new Sample("1970-01-01T00:00:00.000Z", 0));

        seriesB = new Series(exactMatchEntityName, exactMatchMetricName, "tag-1", "val-1");
        seriesB.addSamples(new Sample("1970-01-01T00:00:00.000Z", 0));

        seriesC = new Series(exactMatchEntityName, exactMatchMetricName, "tag-2", "val-2");
        seriesC.addSamples(new Sample("1970-01-01T00:00:00.000Z", 0));

        seriesD = new Series(exactMatchEntityName, exactMatchMetricName);
        seriesD.addSamples(new Sample("1970-01-01T00:00:00.000Z", 0));

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
        assertEquals("Response array contains wrong elements count", 1, calculateJsonArraySize(given));

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(seriesD));
        assertTrue("Received series mismatch", compareJsonString(expected, given));
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
        assertEquals("Response array contains wrong elements count", 4, calculateJsonArraySize(given));

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB, seriesC, seriesD));
        assertTrue("Received series mismatch", compareJsonString(expected, given));
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
        assertEquals("Response array contains wrong elements count", 1, calculateJsonArraySize(given));

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesB));
        assertTrue("Received series mismatch", compareJsonString(expected, given));
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
        assertEquals("Response array contains wrong elements count", 2, calculateJsonArraySize(given));

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB));
        assertTrue("Received series mismatch", compareJsonString(expected, given));
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
        assertEquals("Response array contains wrong elements count", calculateJsonArraySize(given), 1);

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesD));
        assertTrue("Recieved series missmatch", compareJsonString(expected, given));

    }
}
