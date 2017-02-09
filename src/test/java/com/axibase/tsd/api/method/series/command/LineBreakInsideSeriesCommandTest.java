package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class LineBreakInsideSeriesCommandTest extends CommandMethod {
    private static String entityName, metricName;
    private static SeriesCommand ordinarySeriesCommand;

    @BeforeClass
    public static void initializeFields() {
        entityName = Util.TestNames.entity();
        Registry.Entity.register(entityName);

        metricName = Util.TestNames.metric();
        Registry.Metric.register(metricName);

        ordinarySeriesCommand = new SeriesCommand();
        ordinarySeriesCommand.setEntityName(entityName);
        ordinarySeriesCommand.setValues(Collections.singletonMap(metricName, "1234"));
    }

    enum TestType {
        NETWORK_API, DATA_API
    }

    @DataProvider(name = "testType")
    public static Object[][] provideTestType() {
        return new Object[][]{
                {TestType.NETWORK_API},
                {TestType.DATA_API}
        };
    }

    /**
     * #3878
     */
    @Test
    public void testTagLineBreak() throws Exception {
        SeriesCommand firstCommand = new SeriesCommand();
        firstCommand.setEntityName(entityName);
        firstCommand.setValues(Collections.singletonMap(metricName, "1234"));
        firstCommand.setTags(Collections.singletonMap("test-tag", "test\ntest\ntest"));

        List<PlainCommand> commandList = new ArrayList<>();
        commandList.add(firstCommand);
        commandList.add(ordinarySeriesCommand);
        CommandSendingResult res = send(commandList);

        assertEquals(0, (int) res.getFail());
        assertEquals(2, (int) res.getSuccess());
        assertEquals(2, (int) res.getTotal());
    }

    /**
     * #3878
     */
    @Test
    public void testMetricTextLineBreak() throws Exception {
        SeriesCommand firstCommand = new SeriesCommand();
        firstCommand.setEntityName(entityName);
        firstCommand.setTexts(Collections.singletonMap(metricName, "test\ntest\ntest"));

        List<PlainCommand> commandList = new ArrayList<>();
        commandList.add(firstCommand);
        commandList.add(ordinarySeriesCommand);
        CommandSendingResult res = send(commandList);

        assertEquals(0, (int) res.getFail());
        assertEquals(2, (int) res.getSuccess());
        assertEquals(2, (int) res.getTotal());
    }

    private List<PlainCommand> seriesToCommands(Series series) {
        List<PlainCommand> seriesList = new ArrayList<>();

        String entity = series.getEntity();
        String metric = series.getMetric();
        List<Sample> data = series.getData();

        for (Sample sample : data) {
            SeriesCommand command = new SeriesCommand();

            command.setEntityName(entity);
            command.setTags(new HashMap<>(series.getTags()));
            command.setValues(Collections.singletonMap(metric, sample.getV().toString()));
            command.setTexts(Collections.singletonMap(metric, sample.getText()));
            command.setTimeISO(sample.getD());
            command.setTimeMills(sample.getT());

            seriesList.add(command);
        }

        return seriesList;
    }
}
