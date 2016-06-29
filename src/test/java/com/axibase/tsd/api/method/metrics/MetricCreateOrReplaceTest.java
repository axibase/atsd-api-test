package com.axibase.tsd.api.method.metrics;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MetricCreateOrReplaceTest extends MetricMethod {

    @Test
    public void testCreateOrReplace() throws Exception {
        Metric metric = new Metric("m-create-or-replace");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertEquals("Fail to execute createOrReplace metric query", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }
}
