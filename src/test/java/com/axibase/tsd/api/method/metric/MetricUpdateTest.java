package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.junit.Test;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricUpdateTest extends MetricMethod {

    @Test //#1278
    public void testURLEncodeNameWhiteSpace() throws Exception {
        final Metric metric = new Metric("update metric-1");
        assertEquals(BAD_REQUEST.getStatusCode(), updateMetric(metric).getStatus());
    }

    @Test //#1278
    public void testURLEncodeNameSlash() throws Exception {
        final Metric metric = new Metric("update/metric-2");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertEquals(OK.getStatusCode(), updateMetric(metric).getStatus());
        assertTrue(metricExist(metric));
    }

    @Test //#1278
    public void testURLEncodeNameCyrillic() throws Exception {
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
