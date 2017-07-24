package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.extended.CommandMethodTest;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.*;
import static java.util.Collections.singletonMap;

public class AppendFieldTest extends CommandMethodTest {

    /**
     * #3796
     */
    @Test
    public void testAppendDuplicates() throws Exception {
        final String entityName = entity();
        final String metricAppendDuplicates = metric();
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series(entityName, metricAppendDuplicates);
        series.addSamples(new TextSample(ISO_TIME, "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendDuplicates, dataWithDuplicates[0]),
                                                        null, entityName, null, null, null, ISO_TIME, false);
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataWithDuplicates.length; i++) {
            seriesCommand.setTexts(singletonMap(metricAppendDuplicates, dataWithDuplicates[i]));
            CommandMethod.send(seriesCommand);
        }

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

    /**
     * #3796
     */
    @Test
    public void testAppendWithErase() throws Exception {
        final String entityName = entity();
        final String metricAppendWithErase = metric();
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series(entityName, metricAppendWithErase);
        series.addSamples(new TextSample(ISO_TIME, "d;\ne;\nf;\ng"));

        SeriesCommand seriesCommand = new SeriesCommand(singletonMap(metricAppendWithErase, dataEraseFirst[0]),
                                                        null, entityName, null, null, null, ISO_TIME, false);
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataEraseFirst.length; i++) {
            seriesCommand.setTexts(singletonMap(metricAppendWithErase, dataEraseFirst[i]));
            CommandMethod.send(seriesCommand);
        }

        seriesCommand.setAppend(false);
        seriesCommand.setTexts(singletonMap(metricAppendWithErase, dataEraseSecond[0]));
        CommandMethod.send(seriesCommand);
        seriesCommand.setAppend(true);

        for(int i = 1; i < dataEraseSecond.length; i++) {
            seriesCommand.setTexts(singletonMap(metricAppendWithErase, dataEraseSecond[i]));
            CommandMethod.send(seriesCommand);
        }

        assertTextDataEquals(series, "Append with erase doesn't work");
    }

    /**
     * #3874
     */
    @Test
    public void testDecimalFieldToTextField() throws Exception {
        final String entityName = entity();
        final String metricDecimalToText = metric();
        Series series = new Series(entityName, metricDecimalToText);
        series.addSamples(new Sample(ISO_TIME, DECIMAL_VALUE, TEXT_VALUE));

        List<PlainCommand> seriesCommandList = Arrays.asList(
                new SeriesCommand(singletonMap(metricDecimalToText, TEXT_VALUE), null,
                        entityName, null, null, null, ISO_TIME, true),
                new SeriesCommand(null, singletonMap(metricDecimalToText, DECIMAL_VALUE.toString()),
                        entityName, null, null, null, ISO_TIME, false)
        );

        CommandMethod.send(seriesCommandList);

        assertTextDataEquals(series, "Addition decimal field to text field failed");
    }

    /**
     * #3885
     */
    @Test
    public void testAppendTextViaBatchOfCommands() throws Exception {
        final String entityName = entity();
        final String metricAppendTextViaBatch = metric();
        Series series = new Series(entityName, metricAppendTextViaBatch);
        series.addSamples(new TextSample(ISO_TIME, "text1;\ntext2"));

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

    /**
     * #3902
     */
    @Test
    public void testTextFieldAfterAdditionOfDecimalValue() throws Exception {
        final String entityName = entity();
        final String metricTextAfterDecimalAddition = metric();
        Series series = new Series(entityName, metricTextAfterDecimalAddition);
        series.addSamples(new TextSample(ISO_TIME, TEXT_VALUE));

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
