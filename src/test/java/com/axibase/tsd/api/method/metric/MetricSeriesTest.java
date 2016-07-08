package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricSeriesTest extends MetricMethod {

    @Test //#1278
    public void testURLEncodeNameWhiteSpace() throws Exception {

        final Metric metric = new Metric("series metric-1");
        assertEquals(BAD_REQUEST.getStatusCode(), getMetricSeries(metric.getName()).getStatus());
    }

    @Test //#1278
    public void testURLEncodeNameSlash() throws Exception {
        final Metric metric = new Metric("series/metric-2");
        createOrReplaceMetricCheck(metric);

        assertTrue(compareJsonString("[]", getMetricSeries(metric.getName()).readEntity(String.class)));
    }

    @Test //#1278
    public void testURLEncodeNameCyrillic() throws Exception {
        final Metric metric = new Metric("seriesйёmetric-3");
        createOrReplaceMetricCheck(metric);

        assertTrue(compareJsonString("[]", getMetricSeries(metric.getName()).readEntity(String.class)));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("seriesmetric-4");
        assertEquals(NOT_FOUND.getStatusCode(), getMetricSeries(metric.getName()).getStatus());
    }


}
