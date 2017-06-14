package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.NotCheckedException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.axibase.tsd.api.util.Mocks.*;
import static java.util.Collections.singletonMap;
import static org.testng.AssertJUnit.assertTrue;

public class AppendFieldTest {
    private static final Long AWAIT_TIME = 500L;

    private static boolean checkResult(Series series) {
        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }
        try {
            Thread.sleep(AWAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return checked;
    }

    private static List<String> getActualData(Series series) throws Exception {
        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        return actualSeriesList.stream()
                .flatMap(actualSeries -> actualSeries.getData().stream())
                .map(Sample::getText)
                .sorted()
                .collect(Collectors.toList());
    }

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

        boolean checked = checkResult(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Append with erase doesn't work, expected result was%n%s%nbut actual result is:%n%s",
                expected,  actualData.toString()), checked);
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

        boolean checked = checkResult(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Append with erase doesn't work, expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
    }

    /**
     * #3874
     */
    @Test
    public void testDecimalFieldToTextField() throws Exception {
        final String entityName = entity();
        final String metricDecimalToText = metric();
        Series series = new Series(entityName, metricDecimalToText);
        series.addSamples(new Sample(ISO_TIME, DECIMAL_VALUE));

        List<PlainCommand> seriesCommandList = new ArrayList<>();

        seriesCommandList.add(new SeriesCommand(singletonMap(metricDecimalToText, TEXT_VALUE), null,
                                                entityName, null, null, null, ISO_TIME, true));
        seriesCommandList.add(new SeriesCommand(null, singletonMap(metricDecimalToText, DECIMAL_VALUE.toString()),
                                                entityName, null, null, null, ISO_TIME, false));

        CommandMethod.send(seriesCommandList);

        boolean checked = checkResult(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Addition decimal field to text field failed, " +
                        "expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
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

        List<PlainCommand> seriesCommandList = new ArrayList<>();
        seriesCommandList.add(new SeriesCommand(singletonMap(metricAppendTextViaBatch, "text2"), null,
                                                entityName, null, null, null, ISO_TIME, true));
        seriesCommandList.add(new SeriesCommand(null, singletonMap(metricAppendTextViaBatch, DECIMAL_VALUE.toString()),
                                                entityName, null, null, null, ISO_TIME, null));

        CommandMethod.send(seriesCommandList);

        boolean checked = checkResult(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Addition text field to text field failed, " +
                        "expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
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

        List<PlainCommand> seriesCommandList = new ArrayList<>();

        seriesCommandList.add(new SeriesCommand(singletonMap(metricTextAfterDecimalAddition, TEXT_VALUE), null,
                                                entityName, null, null, null, ISO_TIME, true));
        seriesCommandList.add(new SeriesCommand(null, singletonMap(metricTextAfterDecimalAddition, DECIMAL_VALUE.toString()),
                                                entityName, null, null, null, ISO_TIME, null));

        CommandMethod.send(seriesCommandList);

        boolean checked = checkResult(series);

        List<String> actualData = getActualData(series);
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Addition of decimal value corrupted text field, " +
                        "expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
    }
}
