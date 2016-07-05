package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SeriesQueryTest extends SeriesMethod {
    private static final long sampleMillis = 1467383000000L;

    @BeforeClass
    public static void prepare() throws Exception {
        Series series = new Series("series-query-e-1", "series-query-m-1");
        series.addData(new Sample(sampleMillis, "1"));
        boolean success = insertSeries(series);
        assertTrue("Cannot store common dataset", success);
        Thread.sleep(1000l);
    }

    @Test
    public void testISOTimezoneZ() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        String startDate = dateFormat.format(new Date(sampleMillis));
        String endDate = dateFormat.format(new Date(sampleMillis+1000));
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-1", "series-query-m-1", startDate, endDate);
        seriesQuery.setTimeFormat("milliseconds");
        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals(sampleMillis, storedSeries.get(0).getData().get(0).getT().longValue());
    }
    @Test
    public void testISOTimezonePlusHoursMinutes() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+01:23"));

        String startDate = dateFormat.format(new Date(sampleMillis));
        String endDate = dateFormat.format(new Date(sampleMillis+1000));
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-1", "series-query-m-1", startDate, endDate);
        seriesQuery.setTimeFormat("milliseconds");
        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals(sampleMillis, storedSeries.get(0).getData().get(0).getT().longValue());
    }
    @Test
    public void testISOTimezoneMinusHoursMinutes() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-01:23"));

        String startDate = dateFormat.format(new Date(sampleMillis));
        String endDate = dateFormat.format(new Date(sampleMillis+1000));
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-1", "series-query-m-1", startDate, endDate);
        seriesQuery.setTimeFormat("milliseconds");
        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals(sampleMillis, storedSeries.get(0).getData().get(0).getT().longValue());
    }


    @Test
    public void testLocalTimeUnsupported() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startDate = dateFormat.format(new Date(sampleMillis));
        String endDate = dateFormat.format(new Date(sampleMillis+1000));
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-4", "series-query-m-4", startDate, endDate);
        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01 14:23:20\"}", response.readEntity(String.class), true);

    }
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXX");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+01:23"));

        String startDate = dateFormat.format(new Date(sampleMillis));
        String endDate = dateFormat.format(new Date(sampleMillis+1000));
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-5", "series-query-m-5", startDate, endDate);
        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01T15:46:20+0123\"}", response.readEntity(String.class), true);

    }
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String startDate = ""+sampleMillis;
        String endDate = ""+(sampleMillis+1000);
        SeriesQuery seriesQuery = new SeriesQuery("series-query-e-1", "series-query-m-1", startDate, endDate);
        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1467383000000\"}", response.readEntity(String.class), true);
    }

}
