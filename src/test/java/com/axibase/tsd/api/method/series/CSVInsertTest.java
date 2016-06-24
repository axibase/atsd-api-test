package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.SeriesQuery;
import junit.framework.Assert;
import org.json.JSONArray;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CSVInsertTest extends CSVInsertMethod {
    /* #2009 */
    @Test
    public void testISOFormatZNoMS() throws Exception {
        Entity entity = new Entity("e-iso-5");
        Metric metric = new Metric("m-iso-5");
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date," + metric.getName() + "\n" +
                "2016-05-21T00:00:00Z, 12.45\n" +
                "2016-05-21T00:00:15Z, 10.8";
        csvInsert(entity.getName(), csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(),
                "2016-05-21T00:00:00Z", "2016-05-21T00:00:01Z", tags);
        JSONArray storedSeriesList1 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d", storedSeriesList1));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v", storedSeriesList1));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:15Z", "2016-05-21T00:00:16Z");
        JSONArray storedSeriesList2 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d", storedSeriesList2));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v", storedSeriesList2));
    }

    /* #2009 */
    @Test
    public void testISOFormatZMS() throws Exception {
        Entity entity = new Entity("e-iso-6");
        Metric metric = new Metric("m-iso-6");
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date," + metric.getName() + "\n" +
                "2016-05-21T00:00:00.001Z, 12.45\n" +
                "2016-05-21T00:00:15.001Z, 10.8";
        csvInsert(entity.getName(), csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(),
                "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        JSONArray storedSeriesList1 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d", storedSeriesList1));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v", storedSeriesList1));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(),
                "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        JSONArray storedSeriesList2 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d", storedSeriesList2));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v", storedSeriesList2));
    }

    /* #2009 */
    @Test
    public void testISOFormatPlusHourNoMS() throws Exception {
        Entity entity = new Entity("e-iso-7");
        Metric metric = new Metric("m-iso-7");
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date," + metric.getName() + "\n" +
                "2016-05-21T00:00:00+00:00, 12.45\n" +
                "2016-05-21T00:00:15+00:00, 10.8";
        csvInsert(entity.getName(), csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:00Z", "2016-05-21T00:00:10Z", tags);
        JSONArray storedSeriesList1 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d", storedSeriesList1));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v", storedSeriesList1));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:15Z", "2016-05-21T00:00:20Z");
        JSONArray storedSeriesList2 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d", storedSeriesList2));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v", storedSeriesList2));
    }

    /* #2009 */
    @Test
    public void testISOFormatPlusHourMS() throws Exception {
        Entity entity = new Entity("e-iso-8");
        Metric metric = new Metric("m-iso-8");
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date," + metric.getName() + "\n" +
                "2016-05-21T00:00:00.001+00:00, 12.45\n" +
                "2016-05-21T00:00:15.001+00:00, 10.8";
        csvInsert(entity.getName(), csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        JSONArray storedSeriesList1 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d", storedSeriesList1));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v", storedSeriesList1));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(),
                "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        JSONArray storedSeriesList2 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d", storedSeriesList2));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v", storedSeriesList2));
    }

    /* #2009 */
    @Test
    public void testMultipleISOFormat() throws Exception {
        Entity entity = new Entity("e-iso-9");
        Metric metric = new Metric("m-iso-9");
        Map tags = new HashMap<String, String>();
        tags.put("tag-1", "value-1");
        tags.put("tag-2", "value-2");

        String csvPayload = "date," + metric.getName() + "\n" +
                "2016-05-21T00:00:00Z,      12.45\n" +
                "2016-05-21T00:00:00.001Z,      12\n" +
                "2016-05-21T00:00:15+00:00, 10.8\n" +
                "2016-05-21T00:00:15.001+00:00, 10";
        csvInsert(entity.getName(), csvPayload, tags, 1000);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:00Z", "2016-05-21T00:00:10Z", tags);
        JSONArray storedSeriesList1 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.000Z", getDataField(0, "d", storedSeriesList1));
        Assert.assertEquals("Stored value incorrect", "12.45", getDataField(0, "v", storedSeriesList1));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:00.001Z", "2016-05-21T00:00:00.002Z", tags);
        JSONArray storedSeriesList2 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:00.001Z", getDataField(0, "d", storedSeriesList2));
        Assert.assertEquals("Stored value incorrect", "12", getDataField(0, "v", storedSeriesList2));


        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:15Z", "2016-05-21T00:00:20Z");
        JSONArray storedSeriesList3 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.000Z", getDataField(0, "d", storedSeriesList3));
        Assert.assertEquals("Stored value incorrect", "10.8", getDataField(0, "v", storedSeriesList3));

        seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), "2016-05-21T00:00:15.001Z", "2016-05-21T00:00:15.002Z");
        JSONArray storedSeriesList4 = executeQuery(seriesQuery);
        Assert.assertEquals("Stored date incorrect", "2016-05-21T00:00:15.001Z", getDataField(0, "d", storedSeriesList4));
        Assert.assertEquals("Stored value incorrect", "10", getDataField(0, "v", storedSeriesList4));
    }
}
