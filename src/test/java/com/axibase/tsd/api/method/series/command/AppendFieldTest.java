package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.extended.CommandMethodTest;
import com.axibase.tsd.api.model.command.ListCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.Transport;
import io.qameta.allure.Flaky;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import com.axibase.tsd.api.util.Mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonMap;

public class AppendFieldTest extends CommandMethodTest {
    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public AppendFieldTest(Transport transport) {
        this.transport = transport;
    }

    /*
     * Test is unstable when TCP transport is used
     */
    @Issue("3796")
    @Issue("6319")
    @Test
    @Flaky
    public void testAppendDuplicates() throws Exception {
        final String entityName = Mocks.entity();
        final String metricName = Mocks.metric();
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateText(Mocks.ISO_TIME, "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        List<PlainCommand> commandList = new ArrayList<>();
        for (int i = 0; i < dataWithDuplicates.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricName, dataWithDuplicates[i]),
                    null, entityName, null, null, null, Mocks.ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

//        transport.send(new ListCommand(commandList));
        Transport.HTTP.send(new ListCommand(commandList));

        assertTextDataEquals(series, "Append with erase doesn't work");
    }

//        Append with erase doesn't work, expected result was
//        a;
//        b;
//        c;
//        0.1;
//        word1 word2;
//        0;
//        word1
//        but actual result is:
//        [a;
//        b;
//        c;
//        word1 word2;
//        0;
//        word1;
//        0.1]

/*
 * Test is unstable when TCP transport is used
 */
    @Flaky
    @Issue("3796")
    @Test
    public void testAppendWithErase() throws Exception {
        final String entityName = Mocks.entity();
        final String metricAppendWithErase = Mocks.metric();
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series(entityName, metricAppendWithErase);
        series.addSamples(Sample.ofDateText(Mocks.ISO_TIME, "d;\ne;\nf;\ng"));

        List<PlainCommand> commandList = new ArrayList<>();

        for (int i = 0; i < dataEraseFirst.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendWithErase, dataEraseFirst[i]),
                    null, entityName, null, null, null, Mocks.ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

        for (int i = 0; i < dataEraseSecond.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendWithErase, dataEraseSecond[i]),
                    null, entityName, null, null, null, Mocks.ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

//        transport.send(new ListCommand(commandList));
        Transport.HTTP.send(new ListCommand(commandList)); // TODO Fix when TCP errors details are ready
        assertTextDataEquals(series, "Append with erase doesn't work");
    }

    @Issue("3874")
    @Issue("6319")
    @Test
    public void testDecimalFieldToTextField() throws Exception {
        final String entityName = Mocks.entity();
        final String metricDecimalToText = Mocks.metric();
        Series series = new Series(entityName, metricDecimalToText);
        series.addSamples(Sample.ofDateDecimalText(Mocks.ISO_TIME, Mocks.DECIMAL_VALUE, Mocks.TEXT_VALUE));

        List<PlainCommand> seriesCommandList = Arrays.asList(
                new SeriesCommand(singletonMap(metricDecimalToText, Mocks.TEXT_VALUE), null,
                        entityName, null, null, null, Mocks.ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricDecimalToText, Mocks.DECIMAL_VALUE.toString()),
                        entityName, null, null, null, Mocks.ISO_TIME, false)
        );

        transport.send(new ListCommand(seriesCommandList));

        assertTextDataEquals(series, "Addition decimal field to text field failed");
    }

    @Issue("3885")
    @Issue("6319")
    @Test
    public void testAppendTextViaBatchOfCommands() throws Exception {
        final String entityName = Mocks.entity();
        final String metricAppendTextViaBatch = Mocks.metric();
        Series series = new Series(entityName, metricAppendTextViaBatch);
        series.addSamples(Sample.ofDateText(Mocks.ISO_TIME, "text1;\ntext2"));

        List<PlainCommand> seriesCommandList = new ArrayList<>(Arrays.asList(
                new SeriesCommand(singletonMap(metricAppendTextViaBatch, "text1"), null,
                        entityName, null, null, null, Mocks.ISO_TIME, false),
                new SeriesCommand(singletonMap(metricAppendTextViaBatch, "text2"), null,
                        entityName, null, null, null, Mocks.ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricAppendTextViaBatch, Mocks.DECIMAL_VALUE.toString()),
                        entityName, null, null, null, Mocks.ISO_TIME, null))
        );

        transport.send(new ListCommand(seriesCommandList));

        assertTextDataEquals(series, "Addition text field to text field failed");
    }

    @Issue("3902")
    @Issue("6319")
    @Test
    public void testTextFieldAfterAdditionOfDecimalValue() throws Exception {
        final String entityName = Mocks.entity();
        final String metricTextAfterDecimalAddition = Mocks.metric();
        Series series = new Series(entityName, metricTextAfterDecimalAddition);
        series.addSamples(Sample.ofDateText(Mocks.ISO_TIME, Mocks.TEXT_VALUE));

        List<PlainCommand> seriesCommandList = Arrays.asList(
                new SeriesCommand(singletonMap(metricTextAfterDecimalAddition, Mocks.TEXT_VALUE), null,
                        entityName, null, null, null, Mocks.ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricTextAfterDecimalAddition, Mocks.DECIMAL_VALUE.toString()),
                        entityName, null, null, null, Mocks.ISO_TIME, null)
        );

        transport.send(new ListCommand(seriesCommandList));

        assertTextDataEquals(series, "Addition of decimal value corrupted text field");
    }
}
