package com.axibase.tsd.api.method.metric;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.command.StringCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.*;


public class MetricCommandTest extends MetricTest {
    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public MetricCommandTest(final Transport transport) {
        this.transport = transport;
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testRequired() throws Exception {
        MetricCommand command = new MetricCommand((String) null);
        String assertMessage = "Bad metric was accepted";
        assertFalse(assertMessage, transport.send(command));
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testLabel() throws Exception {
        Metric metric = new Metric(metric());
        metric.setLabel(Mocks.LABEL);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with label: %s",
                metric.getLabel()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testDescription() throws Exception {
        Metric metric = new Metric(metric());
        metric.setDescription(Mocks.DESCRIPTION);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with description: %s",
                metric.getDescription()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testVersioning() throws Exception {
        Metric metric = new Metric(metric());
        metric.setVersioned(true);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with versioned: %s",
                metric.getVersioned()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }


    @Issue("3137")
    @Issue("6319")
    @Test
    public void testTimezone() throws Exception {
        Metric metric = new Metric(metric());
        metric.setTimeZoneID(Mocks.TIMEZONE_ID);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with timezone: %s",
                metric.getTimeZoneID()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testFilterExpression() throws Exception {
        Metric metric = new Metric(metric());
        metric.setFilter("expression");
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with filter expression: %s",
                metric.getFilter()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testTags() throws Exception {
        Metric metric = new Metric(metric(), Mocks.TAGS);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with tags: %s",
                metric.getTags()
        );
        assertTrue(assertMessage, transport.send(command));
        assertMetricExisting(assertMessage, metric);
    }

    @Issue("3137")
    @Issue("6319")
    @Test
    public void testInterpolate() throws Exception {
        Metric metric = new Metric(metric());
        metric.setInterpolate(InterpolationMode.LINEAR);
        MetricCommand command = new MetricCommand(metric);
        String assertMessage = String.format(
                "Failed to insert metric with interpolate mode: %s",
                metric.getInterpolate()
        );
        assertTrue(assertMessage, transport.send(command));
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
    @Issue("6319")
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectVersioning(String value) throws Exception {
        String metricName = metric();
        StringCommand incorrectCommand = new StringCommand(String.format("metric m:%s v:%s",
                metricName, value));
        String assertMessage = String.format(
                "Metric with incorrect versioning field (%s) shouldn't be inserted",
                value
        );
        assertFalse(assertMessage, transport.send(incorrectCommand));
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
    @Issue("6319")
    @Test(dataProvider = "incorrectInterpolationFieldProvider")
    public void testIncorrectInterpolation(String value) throws Exception {
        String metricName = metric();
        StringCommand incorrectCommand = new StringCommand(String.format("metric m:%s i:%s",
                metricName, value));
        String assertMessage = String.format(
                "Metric with incorrect interpolate field (%s) shouldn't be inserted",
                value
        );
        assertFalse(assertMessage, transport.send(incorrectCommand));
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
    @Issue("6319")
    @Test(dataProvider = "incorrectDataTypeFieldProvider")
    public void testIncorrectDataType(String value) throws Exception {
        String metricName = metric();
        StringCommand incorrectCommand = new StringCommand(String.format("metric m:%s p:%s",
                metricName, value));
        String assertMessage = String.format(
                "Metric with incorrect type field (%s) shouldn't be inserted",
                value
        );

        assertFalse(assertMessage, transport.send(incorrectCommand));
    }

    @DataProvider(name = "incorrectTimeZoneProvider")
    public Object[][] provideIncorrectTimeZoneData() {
        return new Object[][]{
                {"Incorrect"},
                {"HST"}
        };
    }

    @Issue("3137")
    @Issue("6319")
    @Test(dataProvider = "incorrectTimeZoneProvider")
    public void testIncorrectTimeZone(String incorrectTimeZone) throws Exception {
        String metricName = metric();
        StringCommand incorrectCommand = new StringCommand(String.format("metric m:%s z:%s",
                metricName, incorrectTimeZone));
        String assertMessage = String.format(
                "Metric with incorrect versioning field (%s) shouldn't be inserted",
                incorrectCommand
        );

        assertFalse(assertMessage, transport.send(incorrectCommand));
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testEnabled() throws Exception {
        String metricName = metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metric);
        command.setEnabled(true);
        assertTrue("Can not set metric enabled", transport.send(command));
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertTrue("Failed to set enabled", actualMetric.getEnabled());
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testDisabled() throws Exception {
        String metricName = metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metric);
        command.setEnabled(false);
        assertTrue("Can not set metric disabled", transport.send(command));
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertFalse("Failed to set disabled", actualMetric.getEnabled());
    }

    @Issue("3550")
    @Issue("6319")
    @Test
    public void testNullEnabled() throws Exception {
        String metricName = metric();
        Metric metric = new Metric(metricName);
        MetricCommand command = new MetricCommand(metricName);
        command.setEnabled(null);
        assertTrue("Can not set metric disabled with null", transport.send(command));
        Checker.check(new MetricCheck(metric));
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
                {"'false'"}
        };
    }

    @Issue("3550")
    @Issue("6319")
    @Test(dataProvider = "incorrectEnabledProvider")
    public void testIncorrectEnabled(String enabled) throws Exception {
        String metricName = metric();
        StringCommand command = new StringCommand(String.format("metric m:%s b:%s", metricName, enabled));
        String assertMessage = "Bad metric was accepted :: " + command;
        assertFalse(assertMessage, transport.send(command));
        Response serverResponse = MetricMethod.queryMetric(metricName);

        assertEquals(assertMessage, Util.responseFamily(serverResponse), Response.Status.Family.CLIENT_ERROR);
    }

    @DataProvider(name = "correctEnabledProvider")
    public Object[][] provideCorrectEnabledData() {
        return new Object[][]{
                {"true"},
                {"false"},
                {"\"true\""},
                {"\"false\""}
        };
    }


    @Issue("3550")
    @Issue("6319")
    @Test(dataProvider = "correctEnabledProvider")
    public void testRawEnabled(String enabled) throws Exception {
        String metricName = metric();
        Metric metric = new Metric(metricName);
        StringCommand command = new StringCommand(String.format("metric m:%s b:%s", metricName, enabled));
        String assertMessage = "Failed to set enabled (raw)";
        assertTrue(assertMessage, transport.send(command));
        Checker.check(new MetricCheck(metric));
        Metric actualMetric = MetricMethod.queryMetric(metricName).readEntity(Metric.class);

        assertEquals(assertMessage, enabled.replaceAll("[\\'\\\"]", ""), actualMetric.getEnabled().toString());
    }
}
