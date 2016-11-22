package com.axibase.tsd.api.method.series;


import com.axibase.tsd.api.method.compaction.CompactionMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.Util.TestNames;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.CommonAssertions.assertErrorMessageStart;
import static com.axibase.tsd.api.util.ErrorTemplate.*;
import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.addOneMS;
import static com.axibase.tsd.api.util.Util.getMillis;
import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.*;


public class SeriesInsertTest extends SeriesTest {
    final String NEXT_AFTER_MAX_STORABLE_DATE = addOneMS(MAX_STORABLE_DATE);

    /**
     * #2871
     **/
    @Test
    public void testBigFloatOverflow() throws Exception {
        String entityName = "e-float-1";
        String metricName = "m-float-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = MILLS_TIME;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(Util.ISOFormat(t), largeNumber));
        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertEquals("Failed to insert float series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored big float value rounded incorrect", new BigDecimal("10.12121212121212121"), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2871
     **/
    @Test
    public void testBigDecimalOverflow() throws Exception {
        String entityName = "e-decimal-1";
        String metricName = "m-decimal-1";
        String largeNumber = "10.121212121212121212212121212121212121212121";
        final long t = MILLS_TIME;

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(t, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertEquals("Managed to insert large decimal series", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
    }

    /**
     * #2871
     **/
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
            String isoDate = Util.ISOFormat(t + i * 5000);
            series.addData(new Sample(isoDate, number));
        }
        assertEquals("Failed to insert small decimal series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.setAggregate(new Aggregate(AggregationType.SUM, new Interval(1, TimeUnit.MINUTE)));
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small decimal value incorrect", new BigDecimal("7.2999999984"), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2871
     **/
    @Test
    public void testDoubleAggregatePrecision() throws Exception {
        String entityName = "e-double-3";
        String metricName = "m-double-3";
        String number = "0.6083333332";
        final long t = MILLS_TIME;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            String isoDate = Util.ISOFormat(t + i * 5000);
            series.addData(new Sample(isoDate, number));
        }
        assertEquals("Failed to insert small decimal series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.setAggregate(new Aggregate(AggregationType.SUM, new Interval(1, TimeUnit.MINUTE)));

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored small double value incorrect", new BigDecimal("7.299999998400001"), seriesList.get(0).getData().get(0).getV());
    }

    @DataProvider(name = "afterCompactionDataProvider")
    public Object[][] provideDataAfterCompaction() {
        return new Object[][]{
                {DataType.DOUBLE, "90000000000000003.9", "9.0E16"},
                {DataType.FLOAT, "900000003.9", "9.0E8"},
                {DataType.DECIMAL, "90000000000000003.93", "90000000000000003.93"}
        };
    }


    /**
     * #2871
     **/
    @Test(dataProvider = "afterCompactionDataProvider")
    public void testPrecisionAfterCompaction(DataType type, String valueBefore, String valueAfter) throws Exception {
        Metric metric = new Metric(TestNames.metric());
        metric.setDataType(type);
        Long time = MILLS_TIME;
        MetricMethod.createOrReplaceMetricCheck(metric);
        Series series = new Series();
        series.setEntity(TestNames.entity());
        series.setMetric(metric.getName());
        series.addData(new Sample(Util.ISOFormat(time), valueBefore));
        SeriesMethod.insertSeriesCheck(series);
        CompactionMethod.performCompaction("2016-06-15", true);
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, time + 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        BigDecimal expectedVale = new BigDecimal(valueBefore);
        BigDecimal actualValue = seriesList.get(0).getData().get(0).getV();
        String assertMessage = String.format(
                "Stored value precision incorrect.%n Expected: %s%nActual: %s%n",
                expectedVale, actualValue
        );
        assertTrue(assertMessage, expectedVale.compareTo(actualValue) == 0);
    }

    /**
     * #2009
     **/
    @Test
    public void testISOFormatsZmsAbsent() throws Exception {
        String entityName = "e-iso-1";
        String metricName = "m-iso-1";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.000Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09Z";
        series.addData(new Sample(d, value));

        assertEquals("Failed to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.001Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2009
     **/
    @Test
    public void testISOFormatsZms() throws Exception {
        String entityName = "e-iso-2";
        String metricName = "m-iso-2";
        String value = "0";

        String storedDate = "2016-06-09T17:08:09.100Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09.100Z";
        series.addData(new Sample(d, value));

        assertEquals("Failed to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.101Z");
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2009
     **/
    @Test
    public void testISOFormatsPlusHoursNoMS() throws Exception {
        String entityName = "e-iso-3";
        String metricName = "m-iso-3";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.000Z";
        series.addData(new Sample("2016-06-09T17:08:09+07:00", value));

        assertEquals("Failed to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());


        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:09.100Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2009
     **/
    @Test
    public void testISOFormatsPlusHoursMS() throws Exception {
        String entityName = "e-iso-4";
        String metricName = "m-iso-4";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.999Z";
        series.addData(new Sample("2016-06-09T17:08:09.999+07:00", value));

        assertEquals("Failed to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:10Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2850
     **/
    @Test
    public void testISOFormatsMinusHoursNoMS() throws Exception {
        String entityName = "e-iso-10";
        String metricName = "m-iso-10";
        String value = "0";

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T20:00:00.000Z";
        series.addData(new Sample("2016-06-09T17:29:00-02:31", value));
        assertEquals("Fail to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T20:00:01Z");
        assertSeriesQueryDataSize(seriesQuery, 1);

        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getD());
        assertEquals("Stored value incorrect", new BigDecimal(value), seriesList.get(0).getData().get(0).getV());
    }


    /**
     * #2913
     **/
    @Test
    public void testUnderscoreSequence() throws Exception {
        final long t = MILLS_TIME;

        Series series = new Series("e___underscore", "m___underscore");
        series.addData(new Sample(Util.ISOFormat(t), "0"));

        assertEquals("Fail to insert series", OK.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
        assertSeriesExisting(series);
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMinInMSSaved() throws Exception {
        Long time = 0L;
        Long endTime = 1L;
        Series series = new Series("e-time-range-1", "m-time-range-1");
        series.addData(new Sample(Util.ISOFormat(time), "0"));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMinInISOSaved() throws Exception {
        Series series = new Series("e-time-range-2", "m-time-range-2");
        series.addData(new Sample(MIN_STORABLE_DATE, "0"));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals("Empty data in returned series", 1, seriesList.get(0).getData().size());
        assertEquals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeInMSTimeSaved() throws Exception {
        Long time = 1L;
        Long endTime = 2L;
        Series series = new Series("e-time-range-3", "m-time-range-3");
        series.addData(new Sample(Util.ISOFormat(time), "1"));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);
        assertEquals(new BigDecimal("1"), seriesList.get(0).getData().get(0).getV());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMaxInMSSaved() throws Exception {
        final long t = getMillis(MAX_STORABLE_DATE);
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-5", "m-time-range-5");
        series.addData(new Sample(Util.ISOFormat(t), v));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getV());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMaxInISOSaved() throws Exception {
        final BigDecimal v = new BigDecimal("" + getMillis(MAX_STORABLE_DATE));

        Series series = new Series("e-time-range-6", "m-time-range-6");
        series.addData(new Sample(MAX_STORABLE_DATE, v));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                MAX_STORABLE_DATE, NEXT_AFTER_MAX_STORABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getV());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMaxInMSOverflow() throws Exception {
        final long t = getMillis(MAX_STORABLE_DATE) + 1;
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-7", "m-time-range-7");
        series.addData(new Sample(t, v));

        assertEquals("Managed to insert series with t out of range", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Managed to insert series with t out of range", 0, data.size());
    }

    /**
     * #2957
     **/
    @Test
    public void testTimeRangeMaxInISOOverflow() throws Exception {
        final BigDecimal v = new BigDecimal("" + getMillis(NEXT_AFTER_MAX_STORABLE_DATE));
        Series series = new Series("e-time-range-8", "m-time-range-8");
        series.addData(new Sample(NEXT_AFTER_MAX_STORABLE_DATE, v));

        assertEquals("Managed to insert series with d out of range", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                NEXT_AFTER_MAX_STORABLE_DATE, addOneMS(NEXT_AFTER_MAX_STORABLE_DATE));
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals("Managed to insert series with d out of range", 0, seriesList.get(0).getData().size());
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundGetRequest0() throws Exception {
        Response response = httpRootResource.path("api").path("404").request().get();
        assertEquals("Nonexistent url with /api doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundGetRequest1() throws Exception {
        Response response = httpApiResource.path("query").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundGetRequest2() throws Exception {
        Response response = httpApiResource.path("404").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();

    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundGetRequest3() throws Exception {
        Response response = httpApiResource.path("404").queryParam("not", "exist").request().get();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundOptionsRequestWithoutApiV1() throws Exception {
        Response response = httpRootResource.path("api").path("404").request().options();
        assertEquals("Nonexistent url without /api/v1 options doesn't return 404", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundOptionsRequest0() throws Exception {
        Response response = httpApiResource.path("*").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundOptionsRequest1() throws Exception {
        Response response = httpApiResource.path("query").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundOptionsRequest2() throws Exception {
        Response response = httpApiResource.path("404").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2927
     **/
    @Test
    public void testUrlNotFoundOptionsRequest3() throws Exception {
        Response response = httpApiResource.path("404").queryParam("not", "exist").request().options();
        assertEquals("Nonexistent url with /api/v1 options doesn't return 200", OK.getStatusCode(), response.getStatus());
        response.close();
    }

    /**
     * #2850
     **/
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "e-iso-11";
        String metricName = "m-iso-11";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample("2016-06-09 20:00:00", value));

        Response response = insertSeries(Collections.singletonList(series));

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorMessageStart(
                extractErrorMessage(response),
                String.format(
                        JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER,
                        "T", " "
                )
        );
    }

    /**
     * #2850
     **/
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "e-iso-12";
        String metricName = "m-iso-12";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample("2016-06-09T09:50:00-1010", value));

        Response response = insertSeries(Collections.singletonList(series));

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorMessageStart(
                extractErrorMessage(response),
                JSON_MAPPING_EXCEPTION_NA
        );
    }

    /**
     * #2850
     **/
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "e-iso-13";
        String metricName = "m-iso-13";
        String value = "0";

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(Mocks.MILLS_TIME.toString(), value));

        Response response = insertSeries(Collections.singletonList(series));

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorMessageStart(
                extractErrorMessage(response),
                String.format(
                        JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER,
                        "-", "9"
                )
        );
    }

    /**
     * #3164
     */
    @Test
    public void testEmptyTagValueRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-1", "m-empty-tag-1");
        series.addData(new Sample(Mocks.MILLS_TIME, "1"));
        String emptyTagName = "empty-tag";

        series.addTag(emptyTagName, "");

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    /**
     * #3164
     */
    @Test
    public void testNullTagValueRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-2", "m-empty-tag-2");
        series.addData(new Sample(Mocks.MILLS_TIME, "1"));
        String emptyTagName = "empty-tag";

        series.addTag(emptyTagName, null);

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    /**
     * #3164
     **/
    @Test
    public void testNullTagValueWithNormalTagsRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-3", "m-empty-tag-3");
        series.addData(new Sample(Mocks.MILLS_TIME, "1"));
        String emptyTagName = "empty-tag";

        series.addTag("nonempty-tag", "value");
        series.addTag(emptyTagName, null);

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    /**
     * #3164
     **/
    @Test
    public void testEmptyTagValueWithNormalTagsRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-4", "m-empty-tag-4");
        series.addData(new Sample(Mocks.MILLS_TIME, "1"));
        String emptyTagName = "empty-tag";

        series.addTag("nonempty-tag", "value");
        series.addTag(emptyTagName, "");

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    /**
     * 2416
     */
    @Test
    public void testTagValueNullRaiseError() throws Exception {
        Series series = new Series("nulltag-entity-1", "nulltag-metric-1");
        series.addData(new Sample(1, "1"));
        series.addTag("t1", null);
        assertEquals("Null in tag value should fail the query", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
    }
}
