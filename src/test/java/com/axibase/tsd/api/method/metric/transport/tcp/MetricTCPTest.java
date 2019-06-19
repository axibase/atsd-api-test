package com.axibase.tsd.api.method.metric.transport.tcp;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.metric.MetricTest;
import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class MetricTCPTest extends MetricTest {

    @Issue("6319")
    @Test
    public void testRequired() throws Exception {
        MetricCommand command = new MetricCommand((String) null);
        String expectedResult = "Missing metric in command \'metric\'";

        assertEquals("Command without metric Name sholdn't be inserted", expectedResult, TCPSender.send(command, true));
    }

    @Issue("6319")
    @Test
    public void testLabel() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setLabel(Mocks.LABEL);
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with label: %s",
                metric.getLabel()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @Issue("6319")
    @Test
    public void testDescription() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setDescription(Mocks.DESCRIPTION);
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with description: %s",
                metric.getDescription()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @Issue("6319")
    @Test
    public void testVersioning() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setVersioned(true);
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with versioned: %s",
                metric.getVersioned()
        );

        assertMetricExisting(assertMessage, metric);
    }


    @Issue("6319")
    @Test
    public void testTimezone() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setFilter("GMT0");
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with filter expression: %s",
                metric.getTimeZoneID()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @Issue("6319")
    @Test
    public void testFilterExpression() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setFilter("expression");
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with filter expression: %s",
                metric.getFilter()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @Issue("6319")
    @Test
    public void testTags() throws Exception {
        Metric metric = new Metric(Mocks.metric(), Mocks.TAGS);
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with tags: %s",
                metric.getTags()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @Issue("6319")
    @Test
    public void testInterpolate() throws Exception {
        Metric metric = new Metric(Mocks.metric());
        metric.setInterpolate(InterpolationMode.LINEAR);
        MetricCommand command = new MetricCommand(metric);
        TCPSender.send(command, true);
        String assertMessage = String.format(
                "Failed to insert metric with interpolate mode: %s",
                metric.getInterpolate()
        );

        assertMetricExisting(assertMessage, metric);
    }

    @DataProvider(name = "incorrectTimeZoneProvider")
    public Object[][] provideIncorrectTimeZoneData() {
        return new Object[][]{
                {"a"},
                {"abc"},
                {"GMT13"}
        };
    }

    @Issue("6319")
    @Test(dataProvider = "incorrectTimeZoneProvider")
    public void testIncorrectTimeZone(String incorrectTimeZone) throws Exception {
        String metricName = Mocks.metric();
        Metric metric = new Metric()
                .setName(metricName)
                .setTimeZoneID(incorrectTimeZone);
        PlainCommand incorrectCommand = new MetricCommand(metric);
        //String incorrectCommand = String.format("metric m:%s z:%s", metricName, incorrectTimeZone);
        String expectedResult = "Cannot match timezone: \'" + incorrectTimeZone + "\'";
        String assertMessage = String.format(
                "Metric with incorrect versioning field (%s) shouldn't be inserted",
                incorrectCommand
        );

        assertEquals(assertMessage, expectedResult, TCPSender.send(incorrectCommand, true));
    }

    @Issue("6319")
    @Test
    public void testEnabled() throws Exception {
        String metricName = Mocks.metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metric);
        command.setEnabled(true);
        TCPSender.send(command, true);
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertTrue("Failed to set enabled", actualMetric.getEnabled());
    }

    @Issue("6319")
    @Test
    public void testDisabled() throws Exception {
        String metricName = Mocks.metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metric);
        command.setEnabled(false);
        TCPSender.send(command, true);
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertFalse("Failed to set disabled", actualMetric.getEnabled());
    }

    @Issue("6319")
    @Test
    public void testNullEnabled() throws Exception {
        String metricName = Mocks.metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metricName);
        command.setEnabled(null);
        TCPSender.send(command, true);
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertTrue("Failed to omit enabled", actualMetric.getEnabled());
    }

}
