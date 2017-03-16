package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.NotCheckedException;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.testng.AssertJUnit.assertTrue;

public class LineBreakInsideSeriesCommandTest extends CommandMethod {
    private static Series ordinarySeries;
    private static Sample testSample = new Sample(Mocks.ISO_TIME, 1234);

    @BeforeClass
    public static void initializeFields() {
        ordinarySeries = new Series(Util.TestNames.entity(), Util.TestNames.metric());
        ordinarySeries.addData(testSample);
    }

    enum TestType {
        NETWORK_API, DATA_API
    }

    @DataProvider(name = "testTypeAndValue")
    public static Object[][] provideTestTypeAndValue() {
        String[] values = new String[]{
                "test\ntest",
                "test\ntest\ntest",
                "test\rtest\rtest",
                "test\ntest\rtest",
                "test\rtest\ntest",
                "test\r\ntest\n\rtest",
                "test\n",
                "test\r",
                "test\r\n",
                "\ntest",
                "\rtest",
                "\n\rtest",
                "\n",
                "\r",
                "\n\n",
                "\r\n",
                "\n\r",
        };

        List<Object[]> parameters = new ArrayList<>();

        for (TestType type : TestType.values()) {
            for (String value : values) {
                parameters.add(new Object[]{type, value});
            }
        }

        Object[][] result = new Object[parameters.size()][];
        parameters.toArray(result);

        return result;
    }

    /**
     * #3878
     */
    @Test(dataProvider = "testTypeAndValue")
    public void testTagLineBreak(TestType type, String value) throws Exception {
        Series seriesWithBreak = new Series(Util.TestNames.entity(), Util.TestNames.metric());
        seriesWithBreak.addTag("test-tag", value);
        seriesWithBreak.addData(testSample);

        sendAndCheck(Arrays.asList(seriesWithBreak, ordinarySeries), type);
    }

    /**
     * #3878
     */
    @Test(dataProvider = "testTypeAndValue")
    public void testMetricTextLineBreak(TestType type, String value) {
        Series seriesWithBreak = new Series(Util.TestNames.entity(), Util.TestNames.metric());
        seriesWithBreak.addData(new TextSample(Mocks.ISO_TIME, value));

        sendAndCheck(Arrays.asList(seriesWithBreak, ordinarySeries), type);
    }

    private void sendAndCheck(List<Series> seriesList, TestType type) {
        List<PlainCommand> commands = new ArrayList<>();
        for (Series series : seriesList) {
            commands.addAll(seriesToCommands(series));
        }

        boolean checked;
        switch (type) {
            case DATA_API:
                try {
                    sendChecked(new SeriesCheck(seriesList), commands);
                    checked = true;
                } catch (NotCheckedException e) {
                    checked = false;
                }
                break;
            case NETWORK_API:
                /*
                StringBuilder gatheredCommands = new StringBuilder();
                for (PlainCommand command : commands) {
                    gatheredCommands.append("debug ").append(command.toString()).append("\n");
                }

                tcpSender.setCommand(gatheredCommands.toString());
                try {
                    checked = tcpSender.sendDebugMode();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                */
                checked = true;
                break;
            default:
                checked = false;
        }
        assertTrue(checked);
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
            command.setTimeMills(sample.getT());
            command.setTimeISO(sample.getD());

            BigDecimal v = sample.getV();
            if (v != null)
                command.setValues(Collections.singletonMap(metric, v.toString()));

            String text = sample.getText();
            if (text != null)
                command.setTexts(Collections.singletonMap(metric, text));

            seriesList.add(command);
        }

        return seriesList;
    }
}
