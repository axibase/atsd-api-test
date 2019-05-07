package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Mocks.metric;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.*;

public class MetricCreateOrReplaceTest extends MetricMethod {

    @Test
    public void testCreateOrReplace() throws Exception {
        final Metric metric = new Metric("m-create-or-replace");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric.getName(), metric);
        assertSame("Fail to execute createOrReplaceEntityGroup method", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsWhiteSpace() {
        final Metric metric = new Metric("createreplace metric-1");

        Response response = createOrReplaceMetric(metric);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("createreplace/metric-2");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertSame("Fail to execute createOrReplaceEntityGroup method", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    @Issue("1278")
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("createreplacйёmetric-3");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertSame("Fail to execute createOrReplaceEntityGroup method", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    @Issue("3141")
    @Test
    public void testMetricTagNameIsLowerCased() {
        final String TAG_NAME = "SoMeTaG";
        final String TAG_VALUE = "value";

        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_NAME, TAG_VALUE);
        Metric metric = new Metric("create-metric-with-tag", tags);

        Response response1 = createOrReplaceMetric(metric);
        assertSame("Failed to create metric", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response1));

        Response response2 = queryMetric(metric.getName());
        Metric createdMetric = response2.readEntity(Metric.class);

        assertEquals("Wrong metric name", metric.getName(), createdMetric.getName());

        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put(TAG_NAME.toLowerCase(), TAG_VALUE);

        assertEquals("Wrong metric tags", expectedTags, createdMetric.getTags());
    }

    @Test
    public void testTimeZone() throws Exception {
        Metric metric = new Metric(metric());
        metric.setTimeZoneID("GMT0");
        createOrReplaceMetricCheck(metric);
        Metric actualMetric = queryMetric(metric.getName()).readEntity(Metric.class);
        assertEquals(String.format("Failed to create metric with the %s timezone", metric.getTimeZoneID()), actualMetric.getTimeZoneID(), metric.getTimeZoneID());
    }
}
