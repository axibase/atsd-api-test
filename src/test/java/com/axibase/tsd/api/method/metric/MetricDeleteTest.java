package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MetricDeleteTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final Metric metric = new Metric("delete metric-1");

        Response response = deleteMetric(metric.getName());
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("delete/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = deleteMetric(metric.getName());
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertFalse(metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("deleteйёmetric-3");
        createOrReplaceMetricCheck(metric);

        assertEquals(OK.getStatusCode(), deleteMetric(metric.getName()).getStatus());
        assertFalse(metricExist(metric));
    }

    /* #NoTicket */
    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("deletemetric-4");
        assertEquals(NOT_FOUND.getStatusCode(), deleteMetric(metric.getName()).getStatus());
    }


}
