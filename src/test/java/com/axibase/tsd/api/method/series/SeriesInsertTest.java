package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.compaction.CompactionMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;

import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

import static com.axibase.tsd.api.Util.*;
import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.*;


public class SeriesInsertTest extends SeriesMethod {
    final String NEXT_AFTER_MAX_STORABLE_DATE = addOneMS(MAX_STORABLE_DATE);

    /* #2871 */
    @Test
    public void testBigFloatOverflow() throws Exception {
        String entityName = "e-float-1";
        String metricName = "m-float-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = 1465485524888L;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(t, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored big float value rounded incorrect", new BigDecimal("10.12121212121212121"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testBigDecimalOverflow() throws Exception {
        String entityName = "e-decimal-1";
        String metricName = "m-decimal-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = 1465485524888L;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(t, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertFalse("Managed to insert large decimal series", insertSeries(series, 1000));
    }

    /* #2871 */
    @Test
    public void testBigDecimalAggregatePrecision() throws Exception {
        String entityName = "e-decimal-2";
        String metricName = "m-decimal-2";
        String number = "0.6083333332";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            series.addData(new Sample(t + i * 5000, number));
        }
        assertTrue("Failed to insert small decimal series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.addAggregateType("SUM");
        seriesQuery.setAggregatePeriod(1, "MINUTE");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small decimal value incorrect", new BigDecimal("7.2999999984"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDoubleAggregatePrecision() throws Exception {
        String entityName = "e-double-3";
        String metricName = "m-double-3";
        String number = "0.6083333332";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            series.addData(new Sample(t + i * 5000, number));
        }
        assertTrue("Failed to insert small decimal series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.addAggregateType("SUM");
        seriesQuery.setAggregatePeriod(1, "MINUTE");

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small double value incorrect", new BigDecimal("7.299999998400001"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDoublePrecisionAfterCompaction() throws Exception {
        String entityName = "e-double-4";
        String metricName = "m-double-4";
        String number = "90000000000000003.9";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert double series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored double value precision incorrect", new BigDecimal("9.0E16"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testFloatPrecisionAfterCompaction() throws Exception {
        String entityName = "e-float-4";
        String metricName = "m-float-4";
        String number = "900000003.9";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored float value precision incorrect", new BigDecimal("9.0E8"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2871 */
    @Test
    public void testDecimalPrecisionAfterCompaction() throws Exception {
        String entityName = "e-decimal-4";
        String metricName = "m-decimal-4";
        String number = "90000000000000003.93";
        final long t = 1465984800000L;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        series.addData(new Sample(t, number));
        assertTrue("Failed to insert decimal series", insertSeries(series, 1000));

        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored decimal value precision incorrect", new BigDecimal("90000000000000003.93"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsZmsAbsent() throws Exception {
        String entityName = "e-iso-1";
        String metricName = "m-iso-1";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.000Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09Z";
        series.addData(new Sample(d, value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.001Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsZms() throws Exception {
        String entityName = "e-iso-2";
        String metricName = "m-iso-2";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.100Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09.100Z";
        series.addData(new Sample(d, value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.101Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsPlusHoursNoMS() throws Exception {
        String entityName = "e-iso-3";
        String metricName = "m-iso-3";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.000Z";
        series.addData(new Sample("2016-06-09T17:08:09+07:00", value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:09.100Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2009 */
    @Test
    public void testISOFormatsPlusHoursMS() throws Exception {
        String entityName = "e-iso-4";
        String metricName = "m-iso-4";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.999Z";
        series.addData(new Sample("2016-06-09T17:08:09.999+07:00", value));

        assertTrue("Failed to insert series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:10Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /* #2850 */
    @Test
    public void testISOFormatsMinusHoursNoMS() throws Exception {
        String entityName = "e-iso-10";
        String metricName = "m-iso-10";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T20:00:00.000Z";
        series.addData(new Sample("2016-06-09T17:29:00-02:31", value));

        //TODO upgrade insertSeriesCheck method to avoid this
        if(insertSeries(series).getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute insert series query");
        }
        Thread.sleep(Util.EXPECTED_PROCESSING_TIME); //wait to handle series by ATSD

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T20:00:01Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }


    /* #2913 */
    @Test
    public void testUnderscoreSequence() throws Exception {
        final long t = 1465485524888L;

        Series series = new Series("e___underscore", "m___underscore");
        series.addData(new Sample(t, "0"));

        assertTrue("Failed to insert float series", insertSeries(series, 1000));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored big float value rounded incorrect", new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
        assertEquals("Returned incorrect entity", series.getEntity(), seriesList.get(0).getEntity());
        assertEquals("Returned incorrect metric", series.getMetric(), seriesList.get(0).getMetric());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinInMSSsaved() throws Exception {
        Long time = 0L;
        Long endTime = 1L;
        Series series = new Series("e-time-range-1", "m-time-range-1");
        series.addData(new Sample(0, "0"));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinInISOSaved() throws Exception {
        Series series = new Series("e-time-range-2", "m-time-range-2");
        series.addData(new Sample(MIN_STORABLE_DATE, "0"));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Empty data in returned series", 1, seriesList.get(0).getData().size());
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeInMSTimeSaved() throws Exception {
        Long time = 1L;
        Long endTime = 2L;
        Series series = new Series("e-time-range-3", "m-time-range-3");
        series.addData(new Sample(time, "1"));

        boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("1"), seriesList.get(0).getData().get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInMSSaved() throws Exception {
        final long t = getMillis(MAX_STORABLE_DATE);
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-5", "m-time-range-5");
        series.addData(new Sample(t, v));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInISOSaved() throws Exception {
        final BigDecimal v = new BigDecimal("" + getMillis(MAX_STORABLE_DATE));

        Series series = new Series("e-time-range-6", "m-time-range-6");
        series.addData(new Sample(MAX_STORABLE_DATE, v));

        Boolean success = insertSeries(series, 700);
        if (!success)
            fail("Failed to insert series");
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                MAX_STORABLE_DATE, NEXT_AFTER_MAX_STORABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getV());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInMSOverflow() throws Exception {
        final long t = getMillis(MAX_STORABLE_DATE) + 1;
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-7", "m-time-range-7");
        series.addData(new Sample(t, v));

        assertFalse("Managed to insert series with t out of range", insertSeries(series, 700));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Managed to insert series with t out of range", 0, data.size());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxInISOOverflow() throws Exception {
        final BigDecimal v = new BigDecimal("" + getMillis(NEXT_AFTER_MAX_STORABLE_DATE));
        Series series = new Series("e-time-range-8", "m-time-range-8");
        series.addData(new Sample(NEXT_AFTER_MAX_STORABLE_DATE, v));

        assertFalse("Managed to insert series with d out of range", insertSeries(series, 700));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                NEXT_AFTER_MAX_STORABLE_DATE, addOneMS(NEXT_AFTER_MAX_STORABLE_DATE));
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals("Managed to insert series with d out of range", 0, seriesList.get(0).getData().size());
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundGetRequest0() throws Exception {
        Response response = httpRootResource.path("api").path("404").request().get();
        assertEquals("Nonexistent url with /api doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /* #2927 */
    @Test
    public void testUrlNotFoundGetRequest1() throws Exception {
        Response response = httpApiResource.path("query").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /* #2927 */
    @Test
    public void testUrlNotFoundGetRequest2() throws Exception {
        Response response = httpApiResource.path("404").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /* #2927 */
    @Test
    public void testUrlNotFoundGetRequest3() throws Exception {
        Response response = httpApiResource.path("404").queryParam("not", "exist").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundOptionsRequestWithoutApiV1() throws Exception {
        Response response = httpRootResource.path("api").path("404").request().options();
        assertEquals("Nonexistent url without /api/v1 options doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundOptionsRequest0() throws Exception {
        Response response = httpApiResource.path("*").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundOptionsRequest1() throws Exception {
        Response response = httpApiResource.path("query").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundOptionsRequest2() throws Exception {
        Response response = httpApiResource.path("404").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2927 */
    @Test
    public void testUrlNotFoundOptionsRequest3() throws Exception {
        Response response = httpApiResource.path("404").queryParam("not", "exist").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "e-iso-11";
        String metricName = "m-iso-11";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample("2016-06-09 20:00:00", value));

        Response response = insertSeriesReturnResponse(series);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"org.codehaus.jackson.map.JsonMappingException: Expected 'T' character but found ' ' (through reference chain: com.axibase.tsd.model.api.ApiTimeSeriesModel[\\\"data\\\"]->com.axibase.tsd.model.api.ApiTimeSeriesValue[\\\"d\\\"])\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "e-iso-12";
        String metricName = "m-iso-12";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample("2016-06-09T09:50:00-1010", value));

        Response response = insertSeriesReturnResponse(series);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"org.codehaus.jackson.map.JsonMappingException: N/A (through reference chain: com.axibase.tsd.model.api.ApiTimeSeriesModel[\\\"data\\\"]->com.axibase.tsd.model.api.ApiTimeSeriesValue[\\\"d\\\"])\"}", response.readEntity(String.class), true);
    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "e-iso-13";
        String metricName = "m-iso-13";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample("1465502400000", value));

        Response response = insertSeriesReturnResponse(series);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"org.codehaus.jackson.map.JsonMappingException: Expected '-' character but found '5' (through reference chain: com.axibase.tsd.model.api.ApiTimeSeriesModel[\\\"data\\\"]->com.axibase.tsd.model.api.ApiTimeSeriesValue[\\\"d\\\"])\"}", response.readEntity(String.class), true);
    }
}
