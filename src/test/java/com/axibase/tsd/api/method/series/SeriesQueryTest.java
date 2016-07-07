package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SeriesQueryTest extends SeriesMethod {
    private static final String sampleDate = "2016-07-01T14:23:20.000Z";
    private static final Series series;

    static {
        series = new Series("series-query-e-1", "series-query-m-1");
        series.addData(new Sample(sampleDate, "1"));
    }

    @BeforeClass
    public static void prepare() throws Exception {
        boolean isSucceed = insertSeries(series);
        assertTrue("Cannot store common dataset", isSucceed);
        Thread.sleep(1000l);
    }


    /* #2850 */
    @Test
    public void testISOTimezoneZ() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T14:23:20Z");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }

    /* #2850 */
    @Test
    public void testISOTimezonePlusHoursMinutes() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T15:46:20+01:23");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }

    /* #2850 */
    @Test
    public void testISOTimezoneMinusHoursMinutes() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T13:00:20-01:23");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }


    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01 14:23:20");

        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01 14:23:20\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T15:46:20+0123");

        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01T15:46:20+0123\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("1467383000000");

        Response response = executeQueryReturnResponse(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1467383000000\"}", response.readEntity(String.class), true);
    }


    private SeriesQuery buildQuery() {
        SeriesQuery seriesQuery = new SeriesQuery();
        seriesQuery.setEntity("series-query-e-1");
        seriesQuery.setMetric("series-query-m-1");

        seriesQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        return seriesQuery;
    }
}
