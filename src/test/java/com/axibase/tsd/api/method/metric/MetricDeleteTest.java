package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;

public class MetricDeleteTest extends MetricMethod {

    @Issue("1278")
    @Test
    public void testMetricNameContainsWhiteSpace() {
        final String name = "delete metric-1";
        Response response = deleteMetric(name);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("delete/metric-2");
        createOrReplaceMetricCheck(metric);

        Response response = deleteMetric(metric.getName());
        assertSame("Fail to execute deleteMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertFalse("Metric should be deleted", metricExist(metric));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("deleteйёmetric-3");
        createOrReplaceMetricCheck(metric);

        assertSame("Fail to execute deleteMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(deleteMetric(metric.getName())));
        assertFalse("Metric should be deleted", metricExist(metric));
    }

    /* #NoTicket */
    @Test
    public void testUnknownMetric() {
        final Metric metric = new Metric("deletemetric-4");
        assertEquals("Wrong response on unknown metric", NOT_FOUND.getStatusCode(), deleteMetric(metric.getName()).getStatus());
    }


}
