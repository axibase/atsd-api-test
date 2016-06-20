package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.SeriesQuery;
import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CSVInsertTest extends CSVInsertMethod {
    /* #2009 */
    @Test
    public void testISOFormatZNoMS() throws Exception {
        String entity = "e-iso-5";
        String metric = "m-iso-5";
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date,m-iso-5\n" +
                "2016-05-21T00:00:00Z, 12.45\n" +
                "2016-05-21T00:00:15Z, 10.8";
        csvInsert(entity, csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00Z", "2016-05-21T00:00:01Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15Z", "2016-05-21T00:00:16Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v"));
    }

    /* #2009 */
    @Test
    public void testISOFormatZMS() throws Exception {
        String entity = "e-iso-6";
        String metric = "m-iso-6";
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date,m-iso-6\n" +
                "2016-05-21T00:00:00.001Z, 12.45\n" +
                "2016-05-21T00:00:15.001Z, 10.8";
        csvInsert(entity, csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v"));
    }

    /* #2009 */
    @Test
    public void testISOFormatPlusHourNoMS() throws Exception {
        String entity = "e-iso-7";
        String metric = "m-iso-7";
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date,m-iso-7\n" +
                "2016-05-21T00:00:00+00:00, 12.45\n" +
                "2016-05-21T00:00:15+00:00, 10.8";
        csvInsert(entity, csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00Z", "2016-05-21T00:00:10Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15Z", "2016-05-21T00:00:20Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v"));
    }

    /* #2009 */
    @Test
    public void testISOFormatPlusHourMS() throws Exception {
        String entity = "e-iso-8";
        String metric = "m-iso-8";
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date,m-iso-8\n" +
                "2016-05-21T00:00:00.001+00:00, 12.45\n" +
                "2016-05-21T00:00:15.001+00:00, 10.8";
        csvInsert(entity, csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v"));
    }

    /* #2009 */
    @Test
    public void testMultipleISOFormat() throws Exception {
        String entity = "e-iso-9";
        String metric = "m-iso-9";
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date,m-iso-9\n" +
                "2016-05-21T00:00:00Z,      12.45\n" +
                "2016-05-21T00:00:00.001Z,      12\n" +
                "2016-05-21T00:00:15+00:00, 10.8\n" +
                "2016-05-21T00:00:15.001+00:00, 10";
        csvInsert(entity, csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00Z", "2016-05-21T00:00:10Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "12", getDataField(0, "v"));


        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15Z", "2016-05-21T00:00:20Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v"));

        seriesQuery = new SeriesQuery(entity, metric, "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d"));
        Assert.assertEquals("Stored value incorrect", "10", getDataField(0, "v"));
    }
}
