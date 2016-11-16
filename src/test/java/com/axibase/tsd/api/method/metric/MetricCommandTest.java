package com.axibase.tsd.api.method.metric;


import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Interpolate;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.testng.AssertJUnit.assertEquals;

public class MetricCommandTest extends MetricMethod {

    /**
     * #3137
     */
    @Test
    public void testRequired() throws Exception {
        String metricName = metric();
        MetricCommand command = new MetricCommand((String) null);
        tcpSender.send(command);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    /**
     * #3137
     */
    @Test
    public void testLabel() throws Exception {
        String metricName = metric();
        String label = "label";
        MetricCommand command = new MetricCommand(metricName);
        command.setLabel(label);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up label", label, actualMetric.getLabel());
    }

    /**
     * #3137
     */
    @Test
    public void testDescription() throws Exception {
        String metricName = metric();
        String description = "description";
        MetricCommand command = new MetricCommand(metricName);
        command.setDataType(DataType.DECIMAL);
        command.setDescription(description);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up description", description, actualMetric.getDescription());
    }

    /**
     * #3137
     */
    @Test
    public void testVersioning() throws Exception {
        String metricName = metric();
        Boolean versioning = true;
        MetricCommand command = new MetricCommand(metricName);
        command.setVersioning(versioning);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up versioning", versioning, actualMetric.getVersioned());
    }


    /**
     * #3137
     */
    @Test
    public void testTimezone() throws Exception {
        String metricName = metric();
        String timeZoneId = "GMT0";
        MetricCommand command = new MetricCommand(metricName);
        command.setTimeZoneId(timeZoneId);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up timezone", timeZoneId, actualMetric.getTimeZoneID());
    }

    /**
     * #3137
     */
    @Test
    public void testFilterExpression() throws Exception {
        String metricName = metric();
        MetricCommand command = new MetricCommand(metricName);
        String filterExpression = "expression";
        command.setFilterExpression(filterExpression);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up filterExpression", filterExpression, actualMetric.getFilter());
    }

    /**
     * #3137
     */
    @Test
    public void testTags() throws Exception {
        String metricName = metric();
        Map<String, String> tags = new HashMap<>();
        tags.put("a", "b");
        tags.put("c", "d");
        MetricCommand command = new MetricCommand(metricName);
        command.setTags(tags);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals("Failed to set up tags", tags, actualMetric.getTags());
    }

    /**
     * #3137
     */
    @Test
    public void testInterpolate() throws Exception {
        String metricName = metric();
        InterpolationMode interpolate = InterpolationMode.LINEAR;
        MetricCommand command = new MetricCommand(metricName);
        command.setInterpolate(interpolate);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to set up interpolation mode", interpolate, actualMetric.getInterpolate());
    }

    @DataProvider(name = "incorrectVersioningFiledProvider")
    public Object[][] provideVersioningFieldData() {
        return new Object[][]{
                {"a"},
                {"тrue"},
                {"tru"},
                {"falsed"},
                {"trueee"},
                {"incorrect"},
                {"кириллица"}
        };
    }

    /**
     * #3137
     */
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectVersioning(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s v:%s",
                metricName, value);
        tcpSender.send(incorrectCommand);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @DataProvider(name = "incorrectInterpolationFieldProvider")
    public Object[][] provideInterpolationFieldData() {
        return new Object[][]{
                {"PREVIOU"},
                {"bla"},
                {"sport"},
                {"lineаr"}
        };
    }

    /**
     * #3137
     */
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectInterpolation(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s i:%s",
                metricName, value);
        tcpSender.send(incorrectCommand);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @DataProvider(name = "incorrectDataTypeFieldProvider")
    public Object[][] provideDataTypeFieldData() {
        return new Object[][]{
                {"int"},
                {"lon"},
                {"sss"},
                {"уу"},
                {"кириллица"}
        };
    }

    /**
     * #3137
     */
    @Test(dataProvider = "incorrectDataTypeFieldProvider")
    public void testIncorrectDataType(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s p:%s",
                metricName, value);
        tcpSender.send(incorrectCommand);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", NOT_FOUND.getStatusCode(), response.getStatus());
    }


    @DataProvider(name = "incorrectTimeZoneProvider")
    public Object[][] provideIncorrectTimeZoneData() {
        return new Object[][]{
                {"a"},
                {"abc"},
                {"GMT13"}
        };
    }

    /**
     * #3137
     */
    @Test(dataProvider = "incorrectTimeZoneProvider")
    public void testIncorrectTimeZone(String incorrectTimeZone) throws Exception {
        String metricName = metric();
        MetricCommand command = new MetricCommand(metricName);
        command.setTimeZoneId("aaa");
        tcpSender.send(command);
        Response response = MetricMethod.queryMetric(metricName);
        assertEquals("Metric shouldn't be inserted", NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
