package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

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

    /* #3141 */
    @Test
    public void testMetricTagNameIsLowerCased() throws Exception {
        final String FIRST_TAG_NAME = "SoMeTaG";
        final String SECOND_TAG_NAME = "NeWtAg";
        final String TAG_VALUE = "value";

        Metric metric = new Metric("metric-with-tag");
        Map<String, String> tags = new HashMap<>();
        tags.put(FIRST_TAG_NAME, TAG_VALUE);
        metric.setTags(tags);
        Response response1 = createOrReplaceMetric(metric);
        assertEquals("Failed to create metric", OK.getStatusCode(), response1.getStatus());

        Response response2 = queryMetric(metric.getName());
        Metric createdMetric = response2.readEntity(Metric.class);

        assertEquals("Wrong metric tags count", createdMetric.getTags().size(), 1);
        assertTrue("Metric has not tag '"+FIRST_TAG_NAME.toLowerCase()+"'",
                createdMetric.getTags().containsKey(FIRST_TAG_NAME.toLowerCase()));
        assertEquals("Wrong tag '"+FIRST_TAG_NAME.toLowerCase()+"' value",
                createdMetric.getTags().get(FIRST_TAG_NAME.toLowerCase()), TAG_VALUE);

        Map<String, String> newTag = new HashMap<>();
        newTag.put("NeWtAg", "value");

        metric.setTags(newTag);

        Response response3 = updateMetric(metric);
        assertEquals("Failed to update metric", OK.getStatusCode(), response3.getStatus());

        Response response4 = queryMetric(metric.getName());
        Metric updatedMetric = response4.readEntity(Metric.class);

        assertEquals("Wrong metric tags count", updatedMetric.getTags().size(), 2);
        assertTrue("Metric has not tag '"+FIRST_TAG_NAME.toLowerCase()+"'",
                updatedMetric.getTags().containsKey(FIRST_TAG_NAME.toLowerCase()));
        assertEquals("Wrong tag 'sometag' value",
                updatedMetric.getTags().get(FIRST_TAG_NAME.toLowerCase()), TAG_VALUE);
        assertTrue("Metric has not tag '" + SECOND_TAG_NAME.toLowerCase()+"'",
                updatedMetric.getTags().containsKey(SECOND_TAG_NAME.toLowerCase()));
        assertEquals("Wrong tag '"+SECOND_TAG_NAME.toLowerCase()+"' value",
                updatedMetric.getTags().get(SECOND_TAG_NAME.toLowerCase()), TAG_VALUE);
    }
}
