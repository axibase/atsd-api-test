package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class GroupByTextTest extends SqlTest {
    private final String DEFAULT_ENTITY = entity();
    private final String DEFAULT_METRIC = metric();

    @BeforeClass
    public void insertTextSampleToDefaultSeries() throws Exception {
        Series series = new Series(DEFAULT_ENTITY, DEFAULT_METRIC);
        series.addSamples(
                Sample.ofDateText("2016-06-03T09:00:00.000Z", "sample text"),
                Sample.ofDateText("2016-06-03T09:05:00.000Z", "text"),
                Sample.ofDateText("2016-06-03T09:10:00.000Z", "TEXT"),
                Sample.ofDateText("2016-06-03T09:15:00.000Z", "12"),
                Sample.ofDateInteger("2016-06-03T09:20:00.000Z", 1), // text is null
                Sample.ofDateText("2016-06-03T09:25:00.000Z", "")
        );
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4002
     */
    @Test
    public void testGroupByText() {
        String query = String.format(
                "SELECT text %n" +
                "FROM \"%s\" %n" +
                "WHERE entity = '%s' %n" +
                "GROUP BY text",
                DEFAULT_METRIC, DEFAULT_ENTITY
        );
        String[][] expected = {
                // Sorted inserted text values
                {"null"},
                {""},
                {"12"},
                {"TEXT"},
                {"sample text"},
                {"text"},
        };
        assertSqlQueryRows("Unexpected grouping by text", expected, query);
    }

    /**
     * #4002
     */
    @Test
    public void testGroupByFunctionOfText() {
        String query = String.format(
                "SELECT COUNT(entity) %n" +
                "FROM \"%s\" %n" +
                "WHERE entity = '%s' %n" +
                "GROUP BY UPPER(text)",
                DEFAULT_METRIC, DEFAULT_ENTITY
        );
        String[][] expected = {
                {"1"}, // null
                {"1"}, // ""
                {"1"}, // "12"
                {"1"}, // "SAMPLE TEXT"
                {"2"}, // "TEXT"
        };
        assertSqlQueryRows("Unexpected grouping by text function", expected, query);
    }

    /**
     * #4002
     */
    @Test
    public void testGroupByIsNullText() {
        String query = String.format(
                "SELECT COUNT(entity) %n" +
                "FROM \"%s\" %n" +
                "WHERE entity = '%s' %n" +
                "GROUP BY (text IS NULL)",
                DEFAULT_METRIC, DEFAULT_ENTITY
        );
        String[][] expected = {
                {"5"}, // not null
                {"1"}, // null
        };
        assertSqlQueryRows("Unexpected grouping by text nullity", expected, query);
    }

    /**
     * #4002
     */
    @Test
    public void testGroupByTextAsNumber() {
        String query = String.format(
                "SELECT COUNT(entity) %n" +
                "FROM \"%s\" %n" +
                "WHERE entity = '%s' %n" +
                "GROUP BY CAST(text as number)",
                DEFAULT_METRIC, DEFAULT_ENTITY
        );
        String[][] expected = {
                {"1"}, // "12"
                {"5"}, // other
        };
        assertSqlQueryRows("Unexpected grouping by text to number conversion", expected, query);
    }
}
