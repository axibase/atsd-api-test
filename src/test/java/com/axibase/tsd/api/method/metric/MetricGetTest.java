package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricGetTest extends MetricMethod {

    @Test //#1278
    public void testURLEncodeNameWhiteSpace() throws Exception {
        final Metric metric = new Metric("get metric-1");
        Response response = getMetric(metric.getName());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).contains("Invalid metric name"));
    }

    @Test //#1278
    public void testURLEncodeNameSlash() throws Exception {
        final Metric metric = new Metric("get/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = getMetric(metric.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    @Test //#1278
    public void testURLEncodeNameCyrillic() throws Exception {
        final Metric metric = new Metric("getйёmetric-3");
        createOrReplaceMetricCheck(metric);

        Response response = getMetric(metric.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("getmetric-4");
        assertEquals(NOT_FOUND.getStatusCode(), getMetric(metric.getName()).getStatus());
    }


}
