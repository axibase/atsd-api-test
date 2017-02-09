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

import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static java.util.Collections.singletonMap;
import static org.testng.AssertJUnit.assertTrue;

public class AppendFieldTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_APPEND_DUPLICATES = metric();
    private static final String METRIC_APPEND_WITH_ERASE = metric();
    private static final String METRIC_DECIMAL_TO_TEXT = metric();
    private static final String METRIC_APPEND_TEXT_VIA_BATCH = metric();
    private static final String METRIC_TEXT_AFTER_DECIMAL_ADDITION = metric();

    /**
     * #3796
     */
    @Test
    public void testAppendDuplicates() throws Exception {
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_APPEND_DUPLICATES);
        series.addData(new TextSample(ISO_TIME, "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setTexts(singletonMap(METRIC_APPEND_DUPLICATES, dataWithDuplicates[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataWithDuplicates.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_APPEND_DUPLICATES, dataWithDuplicates[i]));
            CommandMethod.send(seriesCommand);
        }

        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }

        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        List<String> actualData = new ArrayList<>();
        for (Series actualSeries : actualSeriesList) {
            for (Sample sample : actualSeries.getData()) {
                actualData.add(sample.getText());
            }
        }
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Append with erase doesn't work, expected result was%n%s%nbut actual result is:%n%s",
                expected,  actualData.toString()), checked);
    }

    /**
     * #3796
     */
    @Test
    public void testAppendWithErase() throws Exception {
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_APPEND_WITH_ERASE);
        series.addData(new TextSample(ISO_TIME, "d;\ne;\nf;\ng"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setTexts(singletonMap(METRIC_APPEND_WITH_ERASE, dataEraseFirst[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataEraseFirst.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_APPEND_WITH_ERASE, dataEraseFirst[i]));
            CommandMethod.send(seriesCommand);
        }

        seriesCommand.setAppend(false);
        seriesCommand.setTexts(singletonMap(METRIC_APPEND_WITH_ERASE, dataEraseSecond[0]));
        CommandMethod.send(seriesCommand);
        seriesCommand.setAppend(true);

        for(int i = 1; i < dataEraseSecond.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_APPEND_WITH_ERASE, dataEraseSecond[i]));
            CommandMethod.send(seriesCommand);
        }

        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }

        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        List<String> actualData = new ArrayList<>();
        for (Series actualSeries : actualSeriesList) {
            for (Sample sample : actualSeries.getData()) {
                actualData.add(sample.getText());
            }
        }
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Append with erase doesn't work, expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
    }

    /**
     * #3874
     */
    @Test
    public void testDecimalFieldToTextField() throws Exception {
        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_DECIMAL_TO_TEXT);
        series.addData(new Sample(ISO_TIME, DECIMAL_VALUE));

        List<PlainCommand> seriesCommandList = new ArrayList<>();

        SeriesCommand seriesCommandText = new SeriesCommand();
        seriesCommandText.setEntityName(ENTITY_NAME);
        seriesCommandText.setTags(null);
        seriesCommandText.setAppend(true);
        seriesCommandText.setTimeISO(ISO_TIME);
        seriesCommandText.setTexts(singletonMap(METRIC_DECIMAL_TO_TEXT, TEXT_VALUE));
        seriesCommandList.add(seriesCommandText);

        SeriesCommand seriesCommandDecimal = new SeriesCommand();
        seriesCommandDecimal.setEntityName(ENTITY_NAME);
        seriesCommandDecimal.setTags(null);
        seriesCommandDecimal.setAppend(false);
        seriesCommandDecimal.setTimeISO(ISO_TIME);
        seriesCommandDecimal.setValues(singletonMap(METRIC_DECIMAL_TO_TEXT, DECIMAL_VALUE));
        seriesCommandList.add(seriesCommandDecimal);

        CommandMethod.send(seriesCommandList);

        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }

        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        List<String> actualData = new ArrayList<>();
        for (Series actualSeries : actualSeriesList) {
            for (Sample sample : actualSeries.getData()) {
                actualData.add(sample.getText());
            }
        }
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
        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_APPEND_TEXT_VIA_BATCH);
        series.addData(new TextSample(ISO_TIME, "text1;\ntext2"));

        SeriesCommand seriesCommandInitial = new SeriesCommand();
        seriesCommandInitial.setEntityName(ENTITY_NAME);
        seriesCommandInitial.setAppend(false);
        seriesCommandInitial.setTimeISO(ISO_TIME);
        seriesCommandInitial.setTexts(singletonMap(METRIC_APPEND_TEXT_VIA_BATCH, "text1"));
        CommandMethod.send(seriesCommandInitial);

        List<PlainCommand> seriesCommandList = new ArrayList<>();

        SeriesCommand seriesCommandText = new SeriesCommand();
        seriesCommandText.setEntityName(ENTITY_NAME);
        seriesCommandText.setAppend(true);
        seriesCommandText.setTimeISO(ISO_TIME);
        seriesCommandText.setTexts(singletonMap(METRIC_APPEND_TEXT_VIA_BATCH, "text2"));
        seriesCommandList.add(seriesCommandText);

        SeriesCommand seriesCommandDecimal = new SeriesCommand();
        seriesCommandDecimal.setEntityName(ENTITY_NAME);
        seriesCommandDecimal.setTimeISO(ISO_TIME);
        seriesCommandDecimal.setValues(singletonMap(METRIC_APPEND_TEXT_VIA_BATCH, DECIMAL_VALUE));
        seriesCommandList.add(seriesCommandDecimal);

        CommandMethod.send(seriesCommandList);

        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }

        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        List<String> actualData = new ArrayList<>();
        for (Series actualSeries : actualSeriesList) {
            for (Sample sample : actualSeries.getData()) {
                actualData.add(sample.getText());
            }
        }
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
        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_TEXT_AFTER_DECIMAL_ADDITION);
        series.addData(new TextSample(ISO_TIME, TEXT_VALUE));

        List<PlainCommand> seriesCommandList = new ArrayList<>();

        SeriesCommand seriesCommandText = new SeriesCommand();
        seriesCommandText.setEntityName(ENTITY_NAME);
        seriesCommandText.setAppend(true);
        seriesCommandText.setTimeISO(ISO_TIME);
        seriesCommandText.setTexts(singletonMap(METRIC_TEXT_AFTER_DECIMAL_ADDITION, TEXT_VALUE));
        seriesCommandList.add(seriesCommandText);

        SeriesCommand seriesCommandDecimal = new SeriesCommand();
        seriesCommandDecimal.setEntityName(ENTITY_NAME);
        seriesCommandDecimal.setTimeISO(ISO_TIME);
        seriesCommandDecimal.setValues(singletonMap(METRIC_TEXT_AFTER_DECIMAL_ADDITION, DECIMAL_VALUE));
        seriesCommandList.add(seriesCommandDecimal);

        CommandMethod.send(seriesCommandList);

        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }

        List<Series> actualSeriesList = SeriesMethod.executeQueryReturnSeries(new SeriesQuery(series));
        List<String> actualData = new ArrayList<>();
        for (Series actualSeries : actualSeriesList) {
            for (Sample sample : actualSeries.getData()) {
                actualData.add(sample.getText());
            }
        }
        String expected = series.getData().get(0).getText();
        assertTrue(String.format("Addition of decimal value corrupted text field, " +
                        "expected result was%n%s%nbut actual result is:%n%s",
                expected, actualData.toString()), checked);
    }
}
