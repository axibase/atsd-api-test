package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.extended.CommandMethodTest;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.*;
import static java.util.Collections.singletonMap;

public class AppendFieldTest extends CommandMethodTest {

    @Issue("3796")
    @Test
    public void testAppendDuplicates() throws Exception {
        final String entityName = entity();
        final String metricName = metric();
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateText(ISO_TIME, "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        List<PlainCommand> commandList = new ArrayList<>();
        for(int i = 0; i < dataWithDuplicates.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricName, dataWithDuplicates[i]),
                    null, entityName, null, null, null, ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

        CommandMethod.send(commandList);

        assertTextDataEquals(series, "Append with erase doesn't work");

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
    }

    @Issue("3796")
    @Test
    public void testAppendWithErase() throws Exception {
        final String entityName = entity();
        final String metricAppendWithErase = metric();
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series(entityName, metricAppendWithErase);
        series.addSamples(Sample.ofDateText(ISO_TIME, "d;\ne;\nf;\ng"));

        List<PlainCommand> commandList = new ArrayList<>();

        for(int i = 0; i < dataEraseFirst.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendWithErase, dataEraseFirst[i]),
                    null, entityName, null, null, null, ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

        for(int i = 0; i < dataEraseSecond.length; i++) {
            SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendWithErase, dataEraseSecond[i]),
                    null, entityName, null, null, null, ISO_TIME, true);
            if (i == 0) {
                seriesCommand.setAppend(false);
            }
            commandList.add(seriesCommand);
        }

        CommandMethod.send(commandList);

        assertTextDataEquals(series, "Append with erase doesn't work");
    }

    @Issue("3874")
    @Test
    public void testDecimalFieldToTextField() throws Exception {
        final String entityName = entity();
        final String metricDecimalToText = metric();
        Series series = new Series(entityName, metricDecimalToText);
        series.addSamples(Sample.ofDateDecimalText(ISO_TIME, DECIMAL_VALUE, TEXT_VALUE));

        List<PlainCommand> seriesCommandList = Arrays.asList(
                new SeriesCommand(singletonMap(metricDecimalToText, TEXT_VALUE), null,
                        entityName, null, null, null, ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricDecimalToText, DECIMAL_VALUE.toString()),
                        entityName, null, null, null, ISO_TIME, false)
        );

        CommandMethod.send(seriesCommandList);

        assertTextDataEquals(series, "Addition decimal field to text field failed");
    }

    @Issue("3885")
    @Test
    public void testAppendTextViaBatchOfCommands() throws Exception {
        final String entityName = entity();
        final String metricAppendTextViaBatch = metric();
        Series series = new Series(entityName, metricAppendTextViaBatch);
        series.addSamples(Sample.ofDateText(ISO_TIME, "text1;\ntext2"));

        CommandMethod.send(new SeriesCommand(singletonMap(metricAppendTextViaBatch, "text1"), null,
                                            entityName, null, null, null, ISO_TIME, false));

        List<PlainCommand> seriesCommandList = new ArrayList<>(Arrays.asList(
                new SeriesCommand(singletonMap(metricAppendTextViaBatch, "text2"), null,
                        entityName, null, null, null, ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricAppendTextViaBatch, DECIMAL_VALUE.toString()),
                        entityName, null, null, null, ISO_TIME, null))
        );

        CommandMethod.send(seriesCommandList);

        assertTextDataEquals(series, "Addition text field to text field failed");
    }

    @Issue("3902")
    @Test
    public void testTextFieldAfterAdditionOfDecimalValue() throws Exception {
        final String entityName = entity();
        final String metricTextAfterDecimalAddition = metric();
        Series series = new Series(entityName, metricTextAfterDecimalAddition);
        series.addSamples(Sample.ofDateText(ISO_TIME, TEXT_VALUE));

        List<PlainCommand> seriesCommandList = Arrays.asList(
                new SeriesCommand(singletonMap(metricTextAfterDecimalAddition, TEXT_VALUE), null,
                        entityName, null, null, null, ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricTextAfterDecimalAddition, DECIMAL_VALUE.toString()),
                        entityName, null, null, null, ISO_TIME, null)
        );

        CommandMethod.send(seriesCommandList);

        assertTextDataEquals(series, "Addition of decimal value corrupted text field");
    }
}
