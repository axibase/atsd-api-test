package com.axibase.tsd.api.method.metric;


import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class MetricCommandTest extends MetricTest {

    /**
     * #3137
     */
    @Test
    public void testRequired() throws Exception {
        MetricCommand command = new MetricCommand((String) null);
        CommandSendingResult expectedResult = new CommandSendingResult(1, 0);
        assertEquals("Command without metric Name sholdn't be inserted", expectedResult, CommandMethod.send(command));
    }

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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


    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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

    /**
     * #3137
     */
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
}
