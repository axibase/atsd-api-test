package com.axibase.tsd.api.method.metric;


import com.axibase.tsd.api.model.command.SimpleCommand;
import com.axibase.tsd.api.model.command.metric.DataType;
import com.axibase.tsd.api.model.command.metric.Interpolate;
import com.axibase.tsd.api.model.command.metric.MetricCommand;
import com.axibase.tsd.api.model.metric.Metric;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.Util.TestNames.generateMetricName;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;

public class MetricCommandTest extends MetricMethod {


    @Test
    public void testRequired() throws Exception {
        String metricName = generateMetricName();
        MetricCommand command = new MetricCommand(metricName, (String) null);
        tcpSender.send(command);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", response.getStatus(), NOT_FOUND.getStatusCode());
    }

    @Test
    public void testLabel() throws Exception {
        String metricName = generateMetricName();
        String label = "label";
        MetricCommand command = new MetricCommand(metricName, label);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up label", label, actualMetric.getLabel());
    }

    @Test
    public void testDescription() throws Exception {
        String metricName = generateMetricName();
        String description = "description";
        MetricCommand command = new MetricCommand(metricName, DataType.DECIMAL);
        command.setDescription(description);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up description", description, actualMetric.getDescription());
    }

    @Test
    public void testVersioning() throws Exception {
        String metricName = generateMetricName();
        Boolean versioning = true;
        MetricCommand command = new MetricCommand(metricName, versioning);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up versioning", versioning, actualMetric.getVersioned());
    }

    @Test
    public void testFilterExpression() throws Exception {
        String metricName = generateMetricName();
        MetricCommand command = new MetricCommand(metricName, "label");
        String filterExpression = "expression";
        command.setFilterExpression(filterExpression);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up filterExpression", filterExpression, actualMetric.getFilter());
    }

    @Test
    public void testTags() throws Exception {
        String metricName = generateMetricName();
        Map<String, String> tags = new HashMap<>();
        tags.put("a", "b");
        tags.put("c", "d");
        MetricCommand command = new MetricCommand(metricName, tags);
        String filterExpression = "expression";
        command.setFilterExpression(filterExpression);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up filterExpression", tags, actualMetric.getTags());
    }

    @Test
    public void testInterpolate() throws Exception {
        String metricName = generateMetricName();
        Interpolate interpolate = Interpolate.LINEAR;
        MetricCommand command = new MetricCommand(metricName, DataType.DECIMAL);
        command.setInterpolate(interpolate);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to insert metric with label", metricName, actualMetric.getName());
        assertEquals("Failed to set up filterExpression", interpolate, actualMetric.getInterpolate());
    }

    //It's disabled for a while related with (#)
    @Test(enabled = false)
    public void testIncorrectVersioningValue() throws Exception {
        String metricName = generateMetricName();
        SimpleCommand command = new SimpleCommand("metric");
        command.appendField("m", metricName);
        command.appendField("v", "a");
        tcpSender.send(command);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", response.getStatus(), NOT_FOUND.getStatusCode());
    }
}
