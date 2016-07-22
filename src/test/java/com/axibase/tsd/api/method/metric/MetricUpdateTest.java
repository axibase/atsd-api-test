package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.testng.annotations.Test;


import static javax.ws.rs.core.Response.Status.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MetricUpdateTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final Metric metric = new Metric("update metric-1");
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), updateMetric(metric).getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("update/metric-2");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertEquals("Fail to execute updateMetric query", OK.getStatusCode(), updateMetric(metric).getStatus());
        assertTrue("Can not find required metric", metricExist(metric));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("updateйёmetric-3");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertEquals("Fail to execute updateMetric query", OK.getStatusCode(), updateMetric(metric).getStatus());
        assertTrue("Can not find required metric", metricExist(metric));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("updatemetric-4");
        assertEquals("Unknown metric should return NotFound", NOT_FOUND.getStatusCode(), updateMetric(metric).getStatus());
    }


}
