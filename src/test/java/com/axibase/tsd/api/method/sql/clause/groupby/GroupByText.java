package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

public class GroupByText extends SqlTest {
    private final Series DEFAULT_SERIES = Mocks.series();
    private final String DEFAULT_METRIC = DEFAULT_SERIES.getMetric();
    private final String DEFAULT_ENTITY = DEFAULT_SERIES.getEntity();

    private final String GROUP_TEXT_BY = String.format(
            "SELECT text %n" +
            "FROM '%s' %n" +
            "WHERE entity = '%s' %n" +
            "GROUP BY %%s",
            DEFAULT_METRIC, DEFAULT_ENTITY
    );

    private final String GROUP_ENTITY_COUNT_BY = String.format(
            "SELECT COUNT(entity) %n" +
            "FROM '%s' %n" +
            "WHERE entity = '%s' %n" +
            "GROUP BY %%s",
            DEFAULT_METRIC, DEFAULT_ENTITY
    );

    private final String[] INSERTED_TEXT_VALUES = {
            "sample text",
            "text",
            "TEXT",
            "12",
            null,
            "",
    };


    @BeforeClass
    public void insertTextSampleToDefaultSeries() throws Exception {
        DEFAULT_SERIES.setData(new ArrayList<Sample>());
        int minutes = 0;
        final String ISO_PATTERN = "2016-06-03T09:%02d:00.000Z";
        for (String text: INSERTED_TEXT_VALUES) {
            final Sample sample;
            if (text != null) {
                sample = new TextSample(String.format(ISO_PATTERN, minutes), text);
            } else {
                sample = new Sample(String.format(ISO_PATTERN, minutes), new BigDecimal(1), text);
            }
            DEFAULT_SERIES.addData(sample);
            minutes += 5;
        }
        SeriesMethod.insertSeriesCheck(DEFAULT_SERIES);
    }

    @Test
    public void testGroupByText() throws Exception {
        String query = String.format(GROUP_TEXT_BY, "text");
        String[][] expected = {
                // Sorted INSERTED_TEXT_VALUES
                {"null"},
                {""},
                {"12"},
                {"TEXT"},
                {"sample text"},
                {"text"},
        };
        assertSqlQueryRows(expected, query);
    }

    @Test
    public void testGroupByFunctionOfText() throws Exception {
        String query = String.format(GROUP_ENTITY_COUNT_BY, "UPPER(text)");
        String[][] expected = {
                {"1"}, // null
                {"1"}, // ""
                {"1"}, // "12"
                {"1"}, // "SAMPLE TEXT"
                {"2"}, // "TEXT"
        };
        assertSqlQueryRows(expected, query);
    }

    @Test
    public void testGroupByIsNullText() throws Exception {
        String query = String.format(GROUP_ENTITY_COUNT_BY, "(text IS NULL)");
        String[][] expected = {
                {"5"}, // not null
                {"1"}, // null
        };
        assertSqlQueryRows(expected, query);
    }

    @Test
    public void testGroupByTextAsNumber() throws Exception {
        String query = String.format(GROUP_ENTITY_COUNT_BY, "CAST(text as number)");
        String[][] expected = {
                {"1"}, // "12"
                {"5"}, // other
        };
        assertSqlQueryRows(expected, query);
    }
}
