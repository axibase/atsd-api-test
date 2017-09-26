package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.axibase.tsd.api.transport.tcp.TCPSender.sendChecked;
import static org.testng.AssertJUnit.assertTrue;

@Slf4j
public class LineBreakInsideSeriesCommandTest extends CommandMethod {
    private static Sample testSample = new Sample(Mocks.ISO_TIME, 1234);

    enum TestType {
        NETWORK_API, DATA_API
    }

    @AllArgsConstructor
    @ToString
    private static class TestData {
        TestType testType;
        String insertData;
        String responseData;
    }

    @DataProvider(name = "testTypeAndValue")
    public static Object[][] provideTestTypeAndValue() {
        String[][] valueResults = {
                {"test\ntest",                    "test\ntest"},
                {"test\ntest\ntest",              "test\ntest\ntest"},
                {"test\rtest\rtest",              "test\rtest\rtest"},
                {"test\r\ntest\n\rtest",          "test\ntest\n\rtest"},
                {"test\n\ntest\n\ntest",          "test\ntest\ntest"},
                {"test\n\r",                      "test\n\r"},
                {"  \r\r\n\rtest\n\rtest  \n\r ", "  \n\rtest\n\rtest  \n\r "},
        };

        List<Object[]> parameters = new ArrayList<>();

        for (TestType type : TestType.values()) {
            for (String[] valueResult : valueResults) {
                parameters.add(new Object[]{new TestData(type, valueResult[0], valueResult[1])});
            }
        }

        Object[][] result = new Object[parameters.size()][];
        parameters.toArray(result);

        return result;
    }

    /**
     * #3878, #3906
     */
    @Test(dataProvider = "testTypeAndValue")
    public void testTagLineBreak(TestData data) throws Exception {
        log.info("Data", data);
        Series seriesWithBreak = new Series(Mocks.entity(), Mocks.metric());
        seriesWithBreak.addTag("test-tag", data.insertData);
        seriesWithBreak.addSamples(testSample);

        Series responseSeries = seriesWithBreak.copy();
        responseSeries.addTag("test-tag", data.responseData);

        sendAndCheck(seriesWithBreak, responseSeries, data.testType);
    }

    /**
     * #3878, #3906
     */
    @Test(dataProvider = "testTypeAndValue")
    public void testMetricTextLineBreak(TestData data) {
        Sample sampleWithBreak = new TextSample(Mocks.ISO_TIME, data.insertData);
        Sample responseSample = new TextSample(Mocks.ISO_TIME, data.responseData);

        Series seriesWithBreak = new Series(Mocks.entity(), Mocks.metric());
        Series responseSeries = seriesWithBreak.copy();

        seriesWithBreak.addSamples(sampleWithBreak);
        responseSeries.addSamples(responseSample);

        sendAndCheck(seriesWithBreak, responseSeries, data.testType);
    }

    private void sendAndCheck(Series insert, Series response, TestType type) {
        List<PlainCommand> commands = new ArrayList<>();
        commands.addAll(seriesToCommands(insert));

        boolean checked;
        switch (type) {
            case DATA_API:
                try {
                    sendChecked(new SeriesCheck(Collections.singletonList(response)), commands);
                    checked = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    checked = false;
                }
                break;
            case NETWORK_API:
                try {
                    TCPSender.sendChecked(new SeriesCheck(Collections.singletonList(response)), commands);
                    checked = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    checked = false;
                }
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
