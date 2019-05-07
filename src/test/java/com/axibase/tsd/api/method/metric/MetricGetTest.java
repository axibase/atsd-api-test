package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;

public class MetricGetTest extends MetricMethod {

    @Issue("1278")
    @Test
    public void testURLEncodeNameWhiteSpace() {
        final String name = "get metric-1";
        Response response = queryMetric(name);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).contains("Invalid metric name"));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("get/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = queryMetric(metric.getName());
        assertSame("Fail to execute queryMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Metrics should be equal", compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("getйёmetric-3");
        createOrReplaceMetricCheck(metric);

        Response response = queryMetric(metric.getName());
        assertSame("Fail to execute queryMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Metrics should be equal", compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    @Test
    public void testUnknownMetric() {
        final Metric metric = new Metric("getmetric-4");
        assertEquals("Unknown metric should return NotFound", NOT_FOUND.getStatusCode(), queryMetric(metric.getName()).getStatus());
    }


}
