package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static java.util.Collections.singletonMap;

public class AppendFieldTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_DUPLICATES = metric();
    private static final String METRIC_ERASING = metric();

    @BeforeClass
    public void prepareDataForDuplicateTest() throws Exception {
        String[] dataWithDuplicates = {"a", "a", "b", "a", "b", "c", "b", "0.1", "word1 word2", "0", "word1", "0.1"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_DUPLICATES);
        series.addData(new TextSample("2016-06-03T09:20:00.000Z", "a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO("2016-06-03T09:20:00.000Z");
        seriesCommand.setTexts(singletonMap(METRIC_DUPLICATES, dataWithDuplicates[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataWithDuplicates.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_DUPLICATES, dataWithDuplicates[i]));
            CommandMethod.send(seriesCommand);
        }

        Checker.check(new SeriesCheck(Collections.singletonList(series)));
    }

    /**
     * #3796
     */
    @Test
    public void testAppendDuplicates() throws Exception {
        String sqlQuery = String.format(
                "SELECT text FROM '%s'",
                METRIC_DUPLICATES
        );

        String[][] expectedRows = {
                {"a;\nb;\nc;\n0.1;\nword1 word2;\n0;\nword1"}
        };

        assertSqlQueryRows("Append text doesn't work with duplicated values", expectedRows, sqlQuery);
    }

    @BeforeClass
    public void prepareDataForEraseTest() throws Exception {
        String[] dataEraseFirst = {"a", "b", "c"};
        String[] dataEraseSecond = {"d", "e", "f", "g"};

        Series series = new Series();
        series.setEntity(ENTITY_NAME);
        series.setMetric(METRIC_ERASING);
        series.addData(new TextSample("2016-06-03T09:20:00.000Z", "d;\ne;\nf;\ng"));

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setEntityName(ENTITY_NAME);
        seriesCommand.setTags(null);
        seriesCommand.setAppend(false);
        seriesCommand.setTimeISO("2016-06-03T09:20:00.000Z");
        seriesCommand.setTexts(singletonMap(METRIC_ERASING, dataEraseFirst[0]));
        CommandMethod.send(seriesCommand);

        seriesCommand.setAppend(true);
        for(int i = 1; i < dataEraseFirst.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_ERASING, dataEraseFirst[i]));
            CommandMethod.send(seriesCommand);
        }

        seriesCommand.setAppend(false);
        seriesCommand.setTexts(singletonMap(METRIC_ERASING, dataEraseSecond[0]));
        CommandMethod.send(seriesCommand);
        seriesCommand.setAppend(true);

        for(int i = 1; i < dataEraseSecond.length; i++) {
            seriesCommand.setTexts(singletonMap(METRIC_ERASING, dataEraseSecond[i]));
            CommandMethod.send(seriesCommand);
        }

        Checker.check(new SeriesCheck(Collections.singletonList(series)));
    }

    /**
     * #3796
     */
    @Test
    public void testAppendWithErase() throws Exception {
        String sqlQuery = String.format(
                "SELECT text FROM '%s'",
                METRIC_ERASING
        );

        String[][] expectedRows = {
                {"d;\ne;\nf;\ng"}
        };

        assertSqlQueryRows("Append text with erasing doesn't work", expectedRows, sqlQuery);
    }
}
