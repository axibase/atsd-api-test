package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
public class SeriesQueryExactMatch extends SeriesMethod {
    final String exactMatchEntityName = "series-query-exactmatch-entity-1";
    final String exactMatchMetricName = "series-query-exactmatch-metric-1";
    final Series seriesA = new Series(exactMatchEntityName, exactMatchMetricName);
    final Series seriesB = new Series();
    final Series seriesC = new Series();
    final Series seriesD = new Series();


    @BeforeClass
    public void prepareDataset() throws IOException {
        seriesA.addTag("tag-1", "val-1");
        seriesA.addTag("tag-2", "val-2");
        seriesA.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));
        insertSeriesCheck(seriesA);

        seriesB.setEntity(exactMatchEntityName);
        seriesB.setMetric(exactMatchMetricName);
        seriesB.addTag("tag-1", "val-1");
        seriesB.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));
        insertSeriesCheck(seriesB);

        seriesC.setEntity(exactMatchEntityName);
        seriesC.setMetric(exactMatchMetricName);
        seriesC.addTag("tag-2", "val-2");
        seriesC.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));
        insertSeriesCheck(seriesC);

        seriesD.setEntity(exactMatchEntityName);
        seriesD.setMetric(exactMatchMetricName);
        seriesD.addData(new Sample("1970-01-01T00:00:00.000Z", "0"));
        insertSeriesCheck(seriesD);
    }

    /**
     * #3002
     *  strict no tags => only seriesD should be received
     */
    @Test
    public void testExactTrueNoKey() throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, Util.MIN_QUERYABLE_DATE, Util.MAX_QUERYABLE_DATE, new HashMap<String, String>());
        seriesQuery.setExactMatch(true);
        Response response = querySeries(seriesQuery);

        final String given = formatToJsonString(response);
        Assert.assertEquals(calculateJsonArraySize(given), 1, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(seriesD));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     *  soft no tags => all series will be received
     */
    @Test
    public void testExactFalseNoKey() throws Exception {
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, Util.MIN_QUERYABLE_DATE, Util.MAX_QUERYABLE_DATE, new HashMap<String, String>());
        seriesQuery.setExactMatch(false);
        Response response = querySeries(seriesQuery);

        final String given = formatToJsonString(response);
        Assert.assertEquals(calculateJsonArraySize(given), 4, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB, seriesC, seriesD));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     *  strict match tags => only series with specified tag (tag-1=val-1) will be received
     */
    @Test
    public void testExactTrueTagMatch() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag-1", "val-1");
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, Util.MIN_QUERYABLE_DATE, Util.MAX_QUERYABLE_DATE, tags);
        seriesQuery.setExactMatch(true);
        Response response = querySeries(seriesQuery);

        final String given = formatToJsonString(response);
        Assert.assertEquals(calculateJsonArraySize(given), 1, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesB));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }

    /**
     * #3002
     *  soft match tags => series which has the specified tag (tag-1=val-1) will be received
     */
    @Test
    public void testExactFalseTagMatch() throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("tag-1", "val-1");
        SeriesQuery seriesQuery = new SeriesQuery(exactMatchEntityName, exactMatchMetricName, Util.MIN_QUERYABLE_DATE, Util.MAX_QUERYABLE_DATE, tags);
        seriesQuery.setExactMatch(false);
        Response response = querySeries(seriesQuery);

        final String given = formatToJsonString(response);
        Assert.assertEquals(calculateJsonArraySize(given), 2, "Response array contains wrong elements count");

        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(seriesA, seriesB));
        Assert.assertTrue(compareJsonString(expected, given), "Recieved series missmatch");
    }
}
