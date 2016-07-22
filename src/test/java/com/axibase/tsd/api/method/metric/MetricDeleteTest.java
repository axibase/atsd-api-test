package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.metric.Metric;
import org.testng.annotations.Test;


import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class MetricDeleteTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final String name = "delete metric-1";
        Registry.Metric.register(name);
        Response response = deleteMetric(name);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("delete/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = deleteMetric(metric.getName());
        assertEquals("Fail to execute deleteMetric query", OK.getStatusCode(), response.getStatus());
        assertFalse("Metric should be deleted", metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("deleteйёmetric-3");
        createOrReplaceMetricCheck(metric);

        assertEquals("Fail to execute deleteMetric query", OK.getStatusCode(), deleteMetric(metric.getName()).getStatus());
        assertFalse("Metric should be deleted", metricExist(metric));
    }

    /* #NoTicket */
    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("deletemetric-4");
        assertEquals("Wrong response on unknown metric", NOT_FOUND.getStatusCode(), deleteMetric(metric.getName()).getStatus());
    }


}
