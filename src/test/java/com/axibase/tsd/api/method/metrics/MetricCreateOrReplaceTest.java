package com.axibase.tsd.api.method.metrics;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import junit.framework.Assert;
import org.junit.Test;

public class MetricCreateOrReplaceTest extends MetricMethod {

    @Test
    public void testCreateOrReplaceMetric() throws Exception {
        Metric metric = new Metric("m-create-or-replace");
        metric.setDataType(DataType.DECIMAL);

        Assert.assertTrue("Failed to insert create or replace metric", createOrReplaceMetric(metric));
        Assert.assertTrue(getMetric(metric));
        Assert.assertEquals(metric.getDataType().toString(), getMetricField("dataType"));
    }
}
