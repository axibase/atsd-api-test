package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.*;

public class MetricUpdateTest extends MetricMethod {

    @Issue("1278")
    @Test
    public void testMetricNameContainsWhiteSpace() {
        final Metric metric = new Metric("update metric-1");
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), updateMetric(metric).getStatus());
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("update/metric-2");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertSame("Fail to execute updateMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(updateMetric(metric)));
        assertTrue("Can not find required metric", metricExist(metric));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("updateйёmetric-3");
        metric.setDataType(DataType.DECIMAL);
        createOrReplaceMetricCheck(metric);

        metric.setDataType(DataType.DOUBLE);
        assertSame("Fail to execute updateMetric query", Response.Status.Family.SUCCESSFUL, Util.responseFamily(updateMetric(metric)));
        assertTrue("Can not find required metric", metricExist(metric));
    }

    @Test
    public void testUnknownMetric() {
        final Metric metric = new Metric("updatemetric-4");
        assertEquals("Unknown metric should return NotFound", NOT_FOUND.getStatusCode(), updateMetric(metric).getStatus());
    }

    @Issue("3141")
    @Test
    public void testMetricTagNameIsLowerCased() throws Exception {
        final String TAG_NAME = "NeWtAg";
        final String TAG_VALUE = "value";

        Metric metric = new Metric("update-metric-with-tag");
        Response createResponse = createOrReplaceMetric(metric);
        assertSame("Failed to create metric", Response.Status.Family.SUCCESSFUL, Util.responseFamily(createResponse));

        createOrReplaceMetricCheck(metric);

        Map<String, String> tags = new HashMap<>();
        tags.put("NeWtAg", "value");
        metric.setTags(tags);

        Response updateResponse = updateMetric(metric);
        assertSame("Failed to update metric", Response.Status.Family.SUCCESSFUL, Util.responseFamily(updateResponse));

        Response queryResponse = queryMetric(metric.getName());
        assertSame("Failed to query metric", Response.Status.Family.SUCCESSFUL, Util.responseFamily(queryResponse));
        Metric updatedMetric = queryResponse.readEntity(Metric.class);

        assertEquals("Wrong metric name", metric.getName(), updatedMetric.getName());

        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put(TAG_NAME.toLowerCase(), TAG_VALUE);

        assertEquals("Wrong metric tags", expectedTags, updatedMetric.getTags());
    }
}
