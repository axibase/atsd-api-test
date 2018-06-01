package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static com.axibase.tsd.api.util.ErrorTemplate.DATE_FILTER_COMBINATION_REQUIRED;
import static com.axibase.tsd.api.util.ErrorTemplate.DATE_FILTER_END_GREATER_START_REQUIRED;
import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

// TODO: Disabled while #5314 will not be solved.
public class SeriesQueryDateFilterTest extends SeriesMethod {
    private final Sample DATE_FILTER_DEFAULT_SAMPLE = Sample.ofDateInteger("2014-06-06T00:00:00.000Z", 1);

    @Issue("3030")
    @Test
    public void testIntervalOnly() throws Exception {
        Series series = new Series("datefilter-e-1", "datefilter-m-1");
        series.addSamples(DATE_FILTER_DEFAULT_SAMPLE);
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery(series.getEntity(), series.getMetric());
        query.setInterval(new Interval(40, TimeUnit.YEAR));

        Response response = querySeries(query);
        assertEquals("Response code mismatch", OK.getStatusCode(), response.getStatus());
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = response.readEntity(String.class);
        assertTrue("Stored series mismatch", compareJsonString(expected, given));
    }

    @Issue("3030")
    @Test
    public void testIntervalAndEnd() throws Exception {
        Series series = new Series("datefilter-e-2", "datefilter-m-2");
        series.addSamples(DATE_FILTER_DEFAULT_SAMPLE);
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery(series.getEntity(), series.getMetric());
        query.setInterval(new Interval(300, TimeUnit.YEAR));
        query.setEndDate(MAX_STORABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", OK.getStatusCode(), response.getStatus());
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = response.readEntity(String.class);
        assertTrue("Stored series mismatch", compareJsonString(expected, given));
    }

    @Issue("3030")
    @Test
    public void testIntervalAndStart() throws Exception {
        Series series = new Series("datefilter-e-3", "datefilter-m-3");
        series.addSamples(DATE_FILTER_DEFAULT_SAMPLE);
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery(series.getEntity(), series.getMetric());
        query.setInterval(new Interval(300, TimeUnit.YEAR));
        query.setStartDate(MIN_STORABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", OK.getStatusCode(), response.getStatus());
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = response.readEntity(String.class);
        assertTrue("Stored series mismatch", compareJsonString(expected, given));
    }

    @Issue("3030")
    @Test
    public void testStartOnlyRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setStartDate(MIN_STORABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_COMBINATION_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3030")
    @Test
    public void testEndOnlyRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setEndDate(MAX_QUERYABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_COMBINATION_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3030")
    @Test(enabled = false)
    public void testStartGreaterEndRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setEndDate(MIN_QUERYABLE_DATE);
        query.setStartDate(Util.addOneMS(MIN_QUERYABLE_DATE));

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_END_GREATER_START_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3030")
    @Test(enabled = false)
    public void testStartEqualEndRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setEndDate(MIN_QUERYABLE_DATE);
        query.setStartDate(MIN_QUERYABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_END_GREATER_START_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3030")
    @Test(enabled = false)
    public void testIntervalZeroAndStartRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setInterval(new Interval(0, TimeUnit.HOUR));
        query.setStartDate(MIN_QUERYABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_END_GREATER_START_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3030")
    @Test(enabled = false)
    public void testIntervalZeroAndEndRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mockEntity", "mockMetric");
        query.setInterval(new Interval(0, TimeUnit.HOUR));
        query.setEndDate(MIN_QUERYABLE_DATE);

        Response response = querySeries(query);
        assertEquals("Response code mismatch", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", DATE_FILTER_END_GREATER_START_REQUIRED, extractErrorMessage(response));
    }

}
