package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.junit.Test;

import static javax.ws.rs.core.Response.Status.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricUpdateTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final Metric metric = new Metric("update metric-1");
        assertEquals(BAD_REQUEST.getStatusCode(), updateMetric(metric).getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("update/metric-2");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertEquals(OK.getStatusCode(), updateMetric(metric).getStatus());
        assertTrue(metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("updateйёmetric-3");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertEquals(OK.getStatusCode(), updateMetric(metric).getStatus());
        assertTrue(metricExist(metric));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("updatemetric-4");
        assertEquals(NOT_FOUND.getStatusCode(), updateMetric(metric).getStatus());
    }


}
