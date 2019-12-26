package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.compaction.CompactionMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.CommonAssertions;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.CommonAssertions.assertDecimals;
import static com.axibase.tsd.api.util.CommonAssertions.assertErrorMessageStart;
import static com.axibase.tsd.api.util.ErrorTemplate.EMPTY_TAG;
import static com.axibase.tsd.api.util.ErrorTemplate.JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER;
import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;


public class SeriesInsertTest extends SeriesTest {
    private static final String NEXT_AFTER_MAX_STORABLE_DATE = addOneMS(MAX_STORABLE_DATE);

    @Issue("2871")
    @Test
    public void testBigFloatOverflow() throws Exception {
        String entityName = "e-float-1";
        String metricName = "m-float-1";
        BigDecimal largeNumber = new BigDecimal("10.121212121212121212212121212121212121212121");
        final long t = MILLS_TIME;

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(Util.ISOFormat(t), largeNumber));
        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.FLOAT);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertSame("Failed to insert float series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));
        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertDecimals("Stored big float value rounded incorrect",
                new BigDecimal("10.121212121212121"),
                seriesList.get(0).getData().get(0).getValue()
        );
    }

    @Issue("2871")
    @Test
    public void testBigDecimalOverflow() throws Exception {
        String entityName = "e-decimal-1";
        String metricName = "m-decimal-1";
        BigDecimal largeNumber = new BigDecimal("10.121212121212121212212121212121212121212121");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(Mocks.ISO_TIME, largeNumber));

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.DECIMAL);

        MetricMethod.createOrReplaceMetricCheck(metric);
        assertEquals("Managed to insert large decimal series", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());
    }

    @Issue("2871")
    @Test
    public void testBigDecimalAggregatePrecision() throws Exception {
        String entityName = "e-decimal-2";
        String metricName = "m-decimal-2";
        BigDecimal number = new BigDecimal("0.6083333332");
        ZonedDateTime dateTime = ZonedDateTime.parse(Mocks.ISO_TIME);

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DECIMAL);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            series.addSamples(Sample.ofDateDecimal(dateTime.plusSeconds(5 * i).toString(), number));
        }
        assertSame("Failed to insert small decimal series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(
                series.getEntity(),
                series.getMetric(),
                dateTime.format(DateTimeFormatter.ISO_INSTANT),
                dateTime.plusMinutes(1).format(DateTimeFormatter.ISO_INSTANT));
        seriesQuery.setAggregate(new Aggregate(AggregationType.SUM, new Period(1, TimeUnit.MINUTE)));
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored small decimal value incorrect", new BigDecimal("7.2999999984"), seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2871")
    @Test
    public void testDoubleAggregatePrecision() throws Exception {
        String entityName = "e-double-3";
        String metricName = "m-double-3";
        BigDecimal number = new BigDecimal("0.6083333332");
        final long t = MILLS_TIME;

        Metric metric = new Metric(metricName);
        metric.setDataType(DataType.DOUBLE);

        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series(entityName, null);
        series.setMetric(metricName);
        for (int i = 0; i < 12; i++) {
            String isoDate = Util.ISOFormat(t + i * 5000);
            series.addSamples(Sample.ofDateDecimal(isoDate, number));
        }
        assertSame("Failed to insert small decimal series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1 + 11 * 5000);
        seriesQuery.setAggregate(new Aggregate(AggregationType.SUM, new Period(1, TimeUnit.MINUTE)));

        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored small double value incorrect", new BigDecimal("7.299999998400001"), seriesList.get(0).getData().get(0).getValue());
    }

    @DataProvider(name = "afterCompactionDataProvider")
    public Object[][] provideDataAfterCompaction() {
        return new Object[][] {
                // Will be removed in next release
//                {DataType.DOUBLE, new BigDecimal("90000000000000003.9")},
//                {DataType.FLOAT, new BigDecimal("900000003.9")},
                {DataType.DECIMAL, new BigDecimal("90000000000000003.93")}
        };
    }


    @Issue("2871")
    @Test(dataProvider = "afterCompactionDataProvider")
    public void testPrecisionAfterCompaction(DataType type, BigDecimal valueBefore) throws Exception {
        Metric metric = new Metric(metric());
        metric.setDataType(type);
        long time = MILLS_TIME;

        Series series = new Series(entity(), metric.getName());
        series.addSamples(Sample.ofDateDecimal(Util.ISOFormat(time), valueBefore));

        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);
        CompactionMethod.performCompaction();

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, time + 1);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        BigDecimal actualValue = seriesList.get(0).getData().get(0).getValue();
        assertDecimals("Stored value precision incorrect", valueBefore, actualValue);
    }

    @Issue("2009")
    @Test
    public void testISOFormatsZmsAbsent() throws Exception {
        String entityName = "e-iso-1";
        String metricName = "m-iso-1";
        BigDecimal value = new BigDecimal(0);

        String storedDate = "2016-06-09T17:08:09.000Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09Z";
        series.addSamples(Sample.ofDateDecimal(d, value));

        assertSame("Failed to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.001Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", value, seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2009")
    @Test
    public void testISOFormatsZms() throws Exception {
        String entityName = "e-iso-2";
        String metricName = "m-iso-2";
        BigDecimal value = new BigDecimal(0);

        String storedDate = "2016-06-09T17:08:09.100Z";
        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T17:08:09.100Z";
        series.addSamples(Sample.ofDateDecimal(d, value));

        assertSame("Failed to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));
        assertSeriesExisting(series);

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T17:08:09.101Z");
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", storedDate, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", value, seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2009")
    @Test
    public void testISOFormatsPlusHoursNoMS() throws Exception {
        String entityName = "e-iso-3";
        String metricName = "m-iso-3";
        BigDecimal value = new BigDecimal(0);

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.000Z";
        series.addSamples(Sample.ofDateDecimal("2016-06-09T17:08:09+07:00", value));

        assertSame("Failed to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));


        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:09.100Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", value, seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2009")
    @Test
    public void testISOFormatsPlusHoursMS() throws Exception {
        String entityName = "e-iso-4";
        String metricName = "m-iso-4";
        BigDecimal value = new BigDecimal(0);

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T10:08:09.999Z";
        series.addSamples(Sample.ofDateDecimal("2016-06-09T17:08:09.999+07:00", value));

        assertSame("Failed to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T10:08:10Z");
        assertSeriesQueryDataSize(seriesQuery, 1);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", value, seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2850")
    @Test
    public void testISOFormatsMinusHoursNoMS() throws Exception {
        String entityName = "e-iso-10";
        String metricName = "m-iso-10";
        BigDecimal value = new BigDecimal(0);

        Series series = new Series(entityName, metricName);
        String d = "2016-06-09T20:00:00.000Z";
        series.addSamples(Sample.ofDateDecimal("2016-06-09T17:29:00-02:31", value));
        assertSame("Fail to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T20:00:01Z");
        assertSeriesQueryDataSize(seriesQuery, 1);

        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", value, seriesList.get(0).getData().get(0).getValue());
    }


    @Issue("2913")
    @Test
    public void testUnderscoreSequence() throws Exception {
        final long t = MILLS_TIME;

        Series series = new Series("e___underscore", "m___underscore");
        series.addSamples(Sample.ofDateInteger(Util.ISOFormat(t), 0));

        assertSame("Fail to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));
        assertSeriesExisting(series);
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMinInMSSaved() throws Exception {
        long time = 0L;
        long endTime = 1L;
        Series series = new Series("e-time-range-1", "m-time-range-1");
        series.addSamples(Sample.ofDateInteger(Util.ISOFormat(time), 0));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertDecimals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMinInISOSaved() throws Exception {
        Series series = new Series("e-time-range-2", "m-time-range-2");
        series.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 0));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Empty data in returned series", 1, seriesList.get(0).getData().size());
        assertDecimals(new BigDecimal("0"), seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeInMSTimeSaved() throws Exception {
        long time = 1L;
        long endTime = 2L;
        Series series = new Series("e-time-range-3", "m-time-range-3");
        series.addSamples(Sample.ofDateInteger(Util.ISOFormat(time), 1));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), time, endTime);
        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertDecimals(new BigDecimal("1"), seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMaxInMSSaved() throws Exception {
        final long t = getUnixTime(MAX_STORABLE_DATE);
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-5", "m-time-range-5");
        series.addSamples(Sample.ofDateDecimal(Util.ISOFormat(t), v));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getValue());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMaxInISOSaved() throws Exception {
        final BigDecimal v = new BigDecimal("" + getUnixTime(MAX_STORABLE_DATE));

        Series series = new Series("e-time-range-6", "m-time-range-6");
        series.addSamples(Sample.ofDateDecimal(MAX_STORABLE_DATE, v));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                MAX_STORABLE_DATE, NEXT_AFTER_MAX_STORABLE_DATE);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertNotSame("Empty data in response", 0, data.size());
        assertEquals(v, data.get(0).getValue());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMaxInMSOverflow() throws Exception {
        final long t = getUnixTime(MAX_STORABLE_DATE) + 1;
        final BigDecimal v = new BigDecimal("" + t);

        Series series = new Series("e-time-range-7", "m-time-range-7");
        Sample sample = Sample.ofTimeDecimal(t, v);
        series.addSamples(sample);

        assertEquals("Managed to insert series with t out of range", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), t, t + 1);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Managed to insert series with t out of range", 0, data.size());
    }

    @Issue("2957")
    @Test
    public void testTimeRangeMaxInISOOverflow() throws Exception {
        final BigDecimal v = new BigDecimal("" + getUnixTime(NEXT_AFTER_MAX_STORABLE_DATE));
        Series series = new Series("e-time-range-8", "m-time-range-8");
        series.addSamples(Sample.ofDateDecimal(NEXT_AFTER_MAX_STORABLE_DATE, v));

        assertEquals("Managed to insert series with d out of range", BAD_REQUEST.getStatusCode(), insertSeries(Collections.singletonList(series)).getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(),
                NEXT_AFTER_MAX_STORABLE_DATE, addOneMS(NEXT_AFTER_MAX_STORABLE_DATE));
        List<Series> seriesList = querySeriesAsList(seriesQuery);

        assertEquals("Managed to insert series with d out of range", 0, seriesList.get(0).getData().size());
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundGetRequest0() {
        Response response = executeRootRequest(webTarget -> webTarget.path("api").path("404").request().get());
        response.bufferEntity();
        assertEquals("Nonexistent url with /api doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());

    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundGetRequest1() {
        Response response = executeApiRequest(webTarget -> webTarget.path("query").request().get());
        response.bufferEntity();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundGetRequest2() {
        Response response = executeApiRequest(webTarget -> webTarget.path("404").request().get());
        response.bufferEntity();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundGetRequest3() {
        Response response = executeApiRequest(webTarget -> webTarget.path("404").queryParam("not", "exist").request().get());
        response.bufferEntity();
        assertEquals("Nonexistent url with /api/v1 get doesn't return 404", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundOptionsRequestWithoutApiV1() {
        Response response = executeRootRequest(webTarget -> webTarget.path("api").path("404").request().options());
        response.bufferEntity();
        assertSame("Nonexistent url without /api/v1 options doesn't return 404", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundOptionsRequest0() {
        Response response = executeApiRequest(webTarget -> webTarget.path("*").request().options());
        response.bufferEntity();
        assertSame("Nonexistent url with /api/v1 options doesn't return 200", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundOptionsRequest1() {
        Response response = executeApiRequest(webTarget -> webTarget.path("query").request().options());
        response.bufferEntity();
        assertSame("Nonexistent url with /api/v1 options doesn't return 200", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundOptionsRequest2() {
        Response response = executeApiRequest(webTarget -> webTarget.path("404").request().options());
        response.bufferEntity();
        assertSame("Nonexistent url with /api/v1 options doesn't return 200", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
    }

    @Issue("2927")
    @Test
    public void testUrlNotFoundOptionsRequest3() {
        Response response = executeApiRequest(webTarget -> webTarget.path("404").queryParam("not", "exist").request().options());
        response.bufferEntity();
        assertSame("Nonexistent url with /api/v1 options doesn't return 200", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
    }

    @Issue("2850")
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "e-iso-11";
        String metricName = "m-iso-11";
        String value = "0";

        Series series = new Series(entityName, metricName);
        Sample sample = Sample.ofRawDateInteger("2016-06-09 20:00:00", 0);
        series.addSamples(sample);

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

    @Issue("2850")
    @Issue("5272")
    @Test
    public void testRfc822TimezoneOffsetSupported() throws Exception {
        String entityName = "e-iso-12";
        String metricName = "m-iso-12";

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofRawDateInteger("2016-06-09T09:50:00-1010", 42));
        String d = "2016-06-09T20:00:00.000Z";
        assertSame("Fail to insert series", Response.Status.Family.SUCCESSFUL, Util.responseFamily(insertSeries(Collections.singletonList(series))));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), d, "2016-06-09T20:00:01Z");
        assertSeriesQueryDataSize(seriesQuery, 1);

        List<Series> seriesList = querySeriesAsList(seriesQuery);
        assertEquals("Stored date incorrect", d, seriesList.get(0).getData().get(0).getRawDate());
        assertDecimals("Stored value incorrect", BigDecimal.valueOf(42), seriesList.get(0).getData().get(0).getValue());
    }

    @Issue("2850")
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "e-iso-13";
        String metricName = "m-iso-13";

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofRawDateInteger(Long.toString(Mocks.MILLS_TIME), 0));

        Response response = insertSeries(Collections.singletonList(series));

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertErrorMessageStart(
                extractErrorMessage(response),
                String.format(
                        JSON_MAPPING_EXCEPTION_UNEXPECTED_CHARACTER,
                        "-", "5"
                )
        );
    }

    @Issue("3164")
    @Test
    public void testEmptyTagValueRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-1", "m-empty-tag-1");
        series.addSamples(Sample.ofDateInteger(ISO_TIME, 1));
        String emptyTagName = "empty-tag";

        series.addTag(emptyTagName, "");

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    @Issue("3164")
    @Test
    public void testNullTagValueRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-2", "m-empty-tag-2");
        series.addSamples(Sample.ofDateInteger(ISO_TIME, 1));
        String emptyTagName = "empty-tag";

        series.addTag(emptyTagName, null);

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    @Issue("3164")
    @Test
    public void testNullTagValueWithNormalTagsRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-3", "m-empty-tag-3");
        series.addSamples(Sample.ofDateInteger(ISO_TIME, 1));
        String emptyTagName = "empty-tag";

        series.addTag("nonempty-tag", "value");
        series.addTag(emptyTagName, null);

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    @Issue("3164")
    @Test
    public void testEmptyTagValueWithNormalTagsRaisesError() throws Exception {
        Series series = new Series("e-empty-tag-4", "m-empty-tag-4");
        series.addSamples(Sample.ofDateInteger(ISO_TIME, 1));
        String emptyTagName = "empty-tag";

        series.addTag("nonempty-tag", "value");
        series.addTag(emptyTagName, "");

        Response response = insertSeries(Collections.singletonList(series));
        String errorMessage = extractErrorMessage(response);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Incorrect error message", String.format(EMPTY_TAG, emptyTagName), errorMessage);
    }

    @Issue("2416")
    @Test
    public void testTagValueNullRaiseError() throws Exception {
        Series series = new Series("nulltag-entity-1", "nulltag-metric-1");
        series.addSamples(Sample.ofDateInteger(ISO_TIME, 1));
        series.addTag("t1", null);

        Response response = insertSeries(Collections.singletonList(series));

        assertEquals("Null in tag value should fail the query", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @DataProvider(name = "dataTextProvider")
    public Object[][] provideDataText() {
        return new Object[][] {
                {"hello"},
                {"HelLo"},
                {"Hello World"},
                {"spaces      \t\t\t afeqf everywhere"},
                {"Кириллица"},
                {"猫"},
                {"Multi\nline"},
                {null},
                {"null"},
                {"\"null\""},
                {"true"},
                {"\"true\""},
                {"11"},
                {"0"},
                {"0.1"},
                {"\"0.1\""},
                {"\"+0.1\""},
                {""}
        };
    }

    @Issue("3480")
    @Test(dataProvider = "dataTextProvider")
    public void testXTextField(String text) throws Exception {
        String entityName = entity();
        String metricName = metric();

        Series series = new Series(entityName, metricName);
        Sample sample = Sample.ofDateIntegerText("2016-10-11T13:00:00.000Z", 1, text);
        series.addSamples(sample);

        insertSeriesCheck(Collections.singletonList(series));
        SeriesQuery seriesQuery = new SeriesQuery(series);

        CommonAssertions.jsonAssert("Stored series are incorrect",
                Collections.singletonList(series), querySeries(seriesQuery));
    }

    @Issue("3480")
    @Test
    public void testXTextFieldOverwritten() throws Exception {
        String entityName = "e-text-overwritten-1";
        String metricName = "m-text-overwritten-1";

        Series series = new Series(entityName, metricName);

        String[] data = new String[] {"1", "2"};
        for (String x : data) {
            Sample sample = Sample.ofDateIntegerText("2016-10-11T13:00:00.000Z", 1, x);
            series.setSamples(Collections.singleton(sample));
            insertSeriesCheck(Collections.singletonList(series));
        }

        SeriesQuery seriesQuery = new SeriesQuery(series);
        Series lastInsertedSeries = series;
        CommonAssertions.jsonAssert("Stored series are incorrect",
                Collections.singletonList(lastInsertedSeries), SeriesMethod.querySeries(seriesQuery));
    }

    @Issue("3740")
    @Test
    public void testXTextFieldVersioned() throws Exception {
        String entityName = "e-text-versioning-2";
        String metricName = "m-text-versioning-2";
        Series series = new Series(entityName, metricName);

        Metric metric = new Metric(metricName);
        metric.setVersioned(true);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String[] data = new String[] {"1", "2", "3", "4"};
        for (String x : data) {
            Sample sample = Sample.ofDateIntegerText("2016-10-11T13:00:00.000Z", 1, x);
            series.setSamples(Collections.singleton(sample));
            insertSeriesCheck(Collections.singletonList(series));
        }

        SeriesQuery seriesQueryVersioned = new SeriesQuery(series);
        seriesQueryVersioned.setVersioned(true);
        seriesQueryVersioned.setExactMatch(false);
        List<Series> seriesListVersioned = querySeriesAsList(seriesQueryVersioned);
        List<String> textValuesVersioned = new ArrayList<>();
        for (Sample s : seriesListVersioned.get(0).getData()) {
            textValuesVersioned.add(s.getText());
        }

        assertEquals("Text field versioning is corrupted", Arrays.asList(data), textValuesVersioned);
    }

    @Issue("3480")
    @Test
    public void testXTextFieldPreservedFromTagsModifications() throws Exception {
        String entityName = "e-text-modify-tags-1";
        String metricName = "m-text-modify-tags-1";

        Series series = new Series(entityName, metricName);
        String xText = "text";
        Sample sample = Sample.ofDateIntegerText("2016-10-11T13:00:00.000Z", 1, xText);
        series.addSamples(sample);
        insertSeriesCheck(Collections.singletonList(series));

        series.addTag("foo", "foo");
        SeriesQuery seriesQuery = new SeriesQuery(series);
        insertSeriesCheck(Collections.singletonList(series));

        Response response = querySeries(seriesQuery);
        List<Series> seriesList = querySeriesAsList(seriesQuery);

        CommonAssertions.jsonAssert("Stored series are incorrect", Collections.singletonList(series), response);
        assertEquals("Tag was not modified", "foo", seriesList.get(0).getTags().get("foo"));
    }

    @Issue("3480")
    @Test
    public void testXTextFieldExplicitNull() throws Exception {
        String entityName = "e-series-insert-text-null-1";
        String metricName = "m-series-insert-text-null-1";
        Series series = new Series(entityName, metricName);
        Sample sample = Sample.ofDateInteger("2016-10-11T13:00:00.000Z", 1);
        series.addSamples(sample);

        String commandJsonFormat = "[{'entity':'%s','metric':'%s','data':[{'d':'%s','v':%s,'x':null}]}]";
        commandJsonFormat = commandJsonFormat.replace('\'', '"');
        String json = String.format(commandJsonFormat, series.getEntity(), series.getMetric(),
                sample.getRawDate(), sample.getValue());
        Response response = insertSeries(json);
        assertSame("Bad insertion request status code", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        Checker.check(new SeriesCheck(Collections.singletonList(series)));

        SeriesQuery seriesQuery = new SeriesQuery(series);
        CommonAssertions.jsonAssert("Stored series are incorrect",
                Collections.singletonList(series), querySeries(seriesQuery));
    }
}
