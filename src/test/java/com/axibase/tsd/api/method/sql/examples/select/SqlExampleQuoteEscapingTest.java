package com.axibase.tsd.api.method.sql.examples.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlExampleQuoteEscapingTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-select-ecsape-quote-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(
                TEST_ENTITY_NAME,
                TEST_METRIC_NAME,
                "double\"quote", "tv1",
                "single'quote", "tv2",
                "both'quo\"tes", "tv3");
        series.addSamples(new Sample("2016-07-27T22:41:50.407Z", new BigDecimal("12.4")));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3125
     * Test for query all tags documentation example.
     *
     * @see <a href="Select: Escape Quotes in Column Names">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/select-escape-quote</a>
     */
    @Test
    public void testExample1() {
        String sqlQuery = String.format("SELECT tags.*  %nFROM '%s'  %nWHERE datetime > '2016-07-27T22:40:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "tags.double\"quote", "tags.single'quote", "tags.both'quo\"tes"
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("tv3", "tv1", "tv2")
        );
        assertTableColumnsNames(expectedColumnNames, resultTable);
        assertTableRowsExist(expectedRows, resultTable);

    }

    /**
     * #3125
     */
    @Test
    public void testExample2() {
        String sqlQuery = String.format(
                "SELECT tags.\"double\"\"quote\", tags.'double\"quote', %n" +
                        "tags.\"single'quote\", tags.'single''quote', %n" +
                        "tags.\"both'quo\"\"tes\", tags.'both''quo\"tes' %n" +
                        "FROM '%s' %nWHERE datetime > '2016-07-27T22:40:00.000Z'",
                TEST_METRIC_NAME

        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "tags.double\"quote", "tags.double\"quote", "tags.single'quote",
                "tags.single'quote", "tags.both'quo\"tes", "tags.both'quo\"tes"
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("tv1", "tv1", "tv2", "tv2", "tv3", "tv3")
        );
        assertTableColumnsNames(expectedColumnNames, resultTable);
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3125
     */
    @Test
    public void testExample3() {
        String sqlQuery = String.format(
                "SELECT tags.\"double\"\"quote\", tags.'double\"quote', %n" +
                        "tags.\"single'quote\", tags.'single''quote', %n" +
                        "tags.\"both'quo\"\"tes\", tags.'both''quo\"tes' %n" +
                        "FROM '%s' %nWHERE tags.\"double\"\"quote\" = 'tv1' %n" +
                        "AND tags.'both''quo\"tes' IS NOT NULL %nAND tags.'single''quote' LIKE '*2' %n" +
                        "ORDER BY tags.\"single'quote\"",
                TEST_METRIC_NAME

        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "tags.double\"quote", "tags.double\"quote", "tags.single'quote",
                "tags.single'quote", "tags.both'quo\"tes", "tags.both'quo\"tes"
        );

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("tv1", "tv1", "tv2", "tv2", "tv3", "tv3")
        );
        assertTableColumnsNames(expectedColumnNames, resultTable);
        assertTableRowsExist(expectedRows, resultTable);
    }
}
