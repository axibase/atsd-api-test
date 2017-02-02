package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.NotCheckedException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.ISO_TIME;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static java.util.Collections.singletonMap;

public class AppendFieldTest {
    private static final String ENTITY_NAME = entity();

    /**
     * #3796
     */
    @Test
    public void testAppendDuplicates() throws Exception {
        String metricDuplicates = metric();
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(metricDuplicates);
        series.addData(new TextSample(ISO_TIME, "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setTexts(singletonMap(metricDuplicates, dataWithDuplicates[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataWithDuplicates.length; i++) {
            seriesCommand.setTexts(singletonMap(metricDuplicates, dataWithDuplicates[i]));
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
        Assert.assertTrue(checked, "Append with erase doesn't work, expected result was\n" + expected +
                "\nbut actual result is:\n" + actualData.toString());
    }

    /**
     * #3796
     */
    @Test
    public void testAppendWithErase() throws Exception {
        String metricErasing = metric();
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(metricErasing);
        series.addData(new TextSample(ISO_TIME, "d;\ne;\nf;\ng"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO(ISO_TIME);
        seriesCommand.setTexts(singletonMap(metricErasing, dataEraseFirst[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataEraseFirst.length; i++) {
            seriesCommand.setTexts(singletonMap(metricErasing, dataEraseFirst[i]));
            CommandMethod.send(seriesCommand);
        }

        seriesCommand.setAppend(false);
        seriesCommand.setTexts(singletonMap(metricErasing, dataEraseSecond[0]));
        CommandMethod.send(seriesCommand);
        seriesCommand.setAppend(true);

        for(int i = 1; i < dataEraseSecond.length; i++) {
            seriesCommand.setTexts(singletonMap(metricErasing, dataEraseSecond[i]));
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
        Assert.assertTrue(checked, "Append with erase doesn't work, expected result was\n" + expected +
                "\nbut actual result is:\n" + actualData.toString());
    }
}
