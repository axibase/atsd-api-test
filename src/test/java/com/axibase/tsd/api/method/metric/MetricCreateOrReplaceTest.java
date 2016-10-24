package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.Util.TestNames.generateMetricName;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MetricCreateOrReplaceTest extends MetricMethod {

    @Test
    public void testCreateOrReplace() throws Exception {
        final Metric metric = new Metric("m-create-or-replace");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric.getName(), metric);
        assertEquals("Fail to execute createOrReplaceEntityGroup method", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    /**
     * #1278
     **/
    @Test
    public void testMetricNameContainsWhiteSpace() throws Exception {
        final Metric metric = new Metric("createreplace metric-1");

        Response response = createOrReplaceMetric(metric);
        assertEquals("Method should fail if metricName contains whitespace", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * #1278
     **/
    @Test
    public void testMetricNameContainsSlash() throws Exception {
        final Metric metric = new Metric("createreplace/metric-2");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertEquals("Fail to execute createOrReplaceEntityGroup method", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    /**
     * #1278
     **/
    @Test
    public void testMetricNameContainsCyrillic() throws Exception {
        final Metric metric = new Metric("createreplacйёmetric-3");
        metric.setDataType(DataType.DECIMAL);

        Response response = createOrReplaceMetric(metric);
        assertEquals("Fail to execute createOrReplaceEntityGroup method", OK.getStatusCode(), response.getStatus());
        assertTrue("Fail to check metric inserted", metricExist(metric));
    }

    /**
     * #3141
     **/
    @Test
    public void testMetricTagNameIsLowerCased() throws Exception {
        final String TAG_NAME = "SoMeTaG";
        final String TAG_VALUE = "value";

        Metric metric = new Metric("create-metric-with-tag");
        Map<String, String> tags = new HashMap<>();
        tags.put(TAG_NAME, TAG_VALUE);
        metric.setTags(tags);
        Response response1 = createOrReplaceMetric(metric);
        assertEquals("Failed to create metric", OK.getStatusCode(), response1.getStatus());

        Response response2 = queryMetric(metric.getName());
        Metric createdMetric = response2.readEntity(Metric.class);

        assertEquals("Wrong metric name", metric.getName(), createdMetric.getName());

        Map<String, String> expectedTags = new HashMap<>();
        expectedTags.put(TAG_NAME.toLowerCase(), TAG_VALUE);

        assertEquals("Wrong metric tags", expectedTags, createdMetric.getTags());
    }

    @Test
    public void testTimeZone() throws Exception {
        Metric metric = new Metric(generateMetricName());
        metric.setTimeZoneID("GMT0");
        createOrReplaceMetricCheck(metric);
        Metric actualMetric = queryMetric(metric.getName()).readEntity(Metric.class);
        assertEquals(String.format("Failed to create metric with the %s timezone", metric.getTimeZoneID()), actualMetric.getTimeZoneID(), metric.getTimeZoneID());
    }
}
