package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.model.metric.Metric;
import org.testng.annotations.Test;


import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MetricSeriesTest extends MetricMethod {

    /* #1278 */
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {

        final String name = "series metric-1";
        Registry.Metric.register(name);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), queryMetricSeries(name).getStatus());
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("series/metric-2");
        createOrReplaceMetricCheck(metric);

        assertTrue("series array should be empty", compareJsonString("[]", queryMetricSeries(metric.getName()).readEntity(String.class)));
    }

    /* #1278 */
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("seriesйёmetric-3");
        createOrReplaceMetricCheck(metric);

        assertTrue("series array should be empty", compareJsonString("[]", queryMetricSeries(metric.getName()).readEntity(String.class)));
    }

    @Test
    public void testUnknownMetric() throws Exception {
        final Metric metric = new Metric("seriesmetric-4");
        assertEquals("Unknown metric should return NotFound", NOT_FOUND.getStatusCode(), queryMetricSeries(metric.getName()).getStatus());
    }


}
