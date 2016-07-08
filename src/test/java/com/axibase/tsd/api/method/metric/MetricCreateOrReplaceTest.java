package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricCreateOrReplaceTest extends MetricMethod {

    @Test
    public void testCreateOrReplace() throws Exception {
        final Metric metric = new Metric("m-create-or-replace");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric.getName(), metric);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final Metric metric = new Metric("createreplace metric-1");

        Response response = createOrReplaceMetric(metric);
        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("createreplace/metric-2");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertEquals(OK.getStatusCode(), response.getStatus());
        assertTrue(metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("createreplacйёmetric-3");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertEquals("Fail to execute createOrReplace metric query", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }
}
