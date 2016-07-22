package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.metric.Metric;
import org.testng.annotations.Test;


import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MetricGetTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testURLEncodeNameWhiteSpace() throws Exception {
        final String name = "get metric-1";
        Registry.Metric.register(name);
        Response response = queryMetric(name);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.readEntity(String.class).contains("Invalid metric name"));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("get/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = queryMetric(metric.getName());
        assertEquals("Fail to execute queryMetric query", OK.getStatusCode(), response.getStatus());
        assertTrue("Metrics should be equal", compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("getйёmetric-3");
        createOrReplaceMetricCheck(metric);

        Response response = queryMetric(metric.getName());
        assertEquals("Fail to execute queryMetric query", OK.getStatusCode(), response.getStatus());
        assertTrue("Metrics should be equal", compareJsonString(jacksonMapper.writeValueAsString(metric), response.readEntity(String.class)));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("getmetric-4");
        assertEquals("Unknown metric should return NotFound", NOT_FOUND.getStatusCode(), queryMetric(metric.getName()).getStatus());
    }


}
