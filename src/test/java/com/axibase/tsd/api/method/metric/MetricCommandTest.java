package com.axibase.tsd.api.method.metric;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.*;

public class MetricCommandTest extends MetricTest {

    @Issue("3137")
    @Test
    public void testRequired() throws Exception {
        MetricCommand command = new MetricCommand((String) null);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        assertEquals("Command without metric Name sholdn't be inserted", expectedResult, CommandMethod.send(command));
    }

    @Issue("3137")
    @Test
    public void testLabel() throws Exception {
        Metric metric = new Metric(metric());
        metric.setLabel(Mocks.LABEL);
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with label: %s",
                metric.getLabel()
        );
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Test
    public void testDescription() throws Exception {
        Metric metric = new Metric(metric());
        metric.setDescription(Mocks.DESCRIPTION);
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with description: %s",
                metric.getDescription()
        );
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Test
    public void testVersioning() throws Exception {
        Metric metric = new Metric(metric());
        metric.setVersioned(true);
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with versioned: %s",
                metric.getVersioned()
        );
        assertMetricExisting(assertMessage, metric);
    }


    @Issue("3137")
    @Test
    public void testTimezone() throws Exception {
        Metric metric = new Metric(metric());
        metric.setFilter("GMT0");
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with filter expression: %s",
                metric.getTimeZoneID()
        );
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Test
    public void testFilterExpression() throws Exception {
        Metric metric = new Metric(metric());
        metric.setFilter("expression");
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with filter expression: %s",
                metric.getFilter()
        );
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Test
    public void testTags() throws Exception {
        Metric metric = new Metric(metric(), Mocks.TAGS);
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with tags: %s",
                metric.getTags()
        );
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Test
    public void testInterpolate() throws Exception {
        Metric metric = new Metric(metric());
        metric.setInterpolate(InterpolationMode.LINEAR);
        MetricCommand command = new MetricCommand(metric);
        CommandMethod.send(command);
        String assertMessage = String.format(
                "Failed to insert metric with interpolate mode: %s",
                metric.getInterpolate()
        );
        assertMetricExisting(assertMessage, metric);
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

    @Issue("3137")
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectVersioning(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s v:%s",
                metricName, value);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        String assertMessage = String.format(
                "Metric with incorrect versioning field (%s) shouldn't be inserted",
                value
        );
        assertEquals(assertMessage, expectedResult, CommandMethod.send(incorrectCommand));
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

    @Issue("3137")
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectInterpolation(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s i:%s",
                metricName, value);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        String assertMessage = String.format(
                "Metric with incorrect interpolate field (%s) shouldn't be inserted",
                value
        );
        assertEquals(assertMessage, expectedResult, CommandMethod.send(incorrectCommand));
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

    @Issue("3137")
    @Test(dataProvider = "incorrectDataTypeFieldProvider")
    public void testIncorrectDataType(String value) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s p:%s",
                metricName, value);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        String assertMessage = String.format(
                "Metric with incorrect type field (%s) shouldn't be inserted",
                value
        );
        assertEquals(assertMessage, expectedResult, CommandMethod.send(incorrectCommand));
    }

    @DataProvider(name = "incorrectTimeZoneProvider")
    public Object[][] provideIncorrectTimeZoneData() {
        return new Object[][]{
                {"a"},
                {"abc"},
                {"GMT13"}
        };
    }

    @Issue("3137")
    @Test(dataProvider = "incorrectTimeZoneProvider")
    public void testIncorrectTimeZone(String incorrectTimeZone) throws Exception {
        String metricName = metric();
        String incorrectCommand = String.format("metric m:%s z:%s",
                metricName, incorrectTimeZone);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        String assertMessage = String.format(
                "Metric with incorrect versioning field (%s) shouldn't be inserted",
                incorrectCommand
        );
        assertEquals(assertMessage, expectedResult, CommandMethod.send(incorrectCommand));
    }

    /**
     * #3550
     */
    @Test
    public void testEnabled() throws Exception {
        String metricName = metric();
        Registry.Metric.register(metricName);
        MetricCommand command = new MetricCommand(metricName);
        command.setEnabled(true);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertTrue("Failed to set enabled", actualMetric.getEnabled());
    }

    /**
     * #3550
     */
    @Test
    public void testDisabled() throws Exception {
        String metricName = metric();
        Registry.Metric.register(metricName);
        MetricCommand command = new MetricCommand(metricName);
        command.setEnabled(false);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertFalse("Failed to set disabled", actualMetric.getEnabled());
    }

    /**
     * #3550
     */
    @Test
    public void testNullEnabled() throws Exception {
        String metricName = metric();
        Registry.Metric.register(metricName);
        MetricCommand command = new MetricCommand(metricName);
        command.setEnabled(null);
        tcpSender.send(command);
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertTrue("Failed to omit enabled", actualMetric.getEnabled());
    }

    @DataProvider(name = "incorrectEnabledProvider")
    public Object[][] provideIncorrectEnabledData() {
        return new Object[][]{
                {"y"},
                {"Y"},
                {"yes"},
                {"да"},
                {"non"},
                {"1"},
                {"+"},
                {"azazaz"},
                {"'true'"},
                {"'false'"},
                {"\"true\""},
                {"\"false\""}
        };
    }

    /**
     * #3550
     */
    @Test(dataProvider = "incorrectEnabledProvider")
    public void testIncorrectEnabled(String enabled) throws Exception {
        String metricName = metric();
        Registry.Metric.register(metricName);
        String command = String.format("metric m:%s b:%s", metricName, enabled);
        tcpSender.send(command);
        Response serverResponse = MetricMethod.queryMetric(metricName);
        assertTrue("Bad metric was accepted", serverResponse.getStatus() >= 400);
    }

    @DataProvider(name = "correctEnabledProvider")
    public Object[][] provideCorrectEnabledData() {
        return new Object[][]{
                {"true"},
                {"false"}
        };
    }


    /**
     * #3550
     */
    @Test(dataProvider = "correctEnabledProvider")
    public void testRawEnabled(String enabled) throws Exception {
        String metricName = "m-metric-command-raw-enabled-" + enabled;
        Metric metric = new Metric(metricName);
        String command = String.format("metric m:%s b:%s", metricName, enabled);
        tcpSender.send(command);
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);
        assertEquals("Failed to set enabled (raw)", enabled, actualMetric.getEnabled().toString());
    }
}
