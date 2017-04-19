package com.axibase.tsd.api.method.sql.examples;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlAllTagsExampleTest extends SqlTest {
    private final static String TEST_PREFIX = "sql-example-all-tags-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity-1", TEST_PREFIX + "metric") {{
            setTags(Collections.unmodifiableMap(new HashMap<String, String>() {{
                put("tag1", "val1");
            }}));
        }};
        sendSamplesToSeries(series, new Sample("2016-06-19T11:00:00.000Z", 1));
        series.setEntity(TEST_PREFIX + "entity-2");
        series.setTags(Collections.
                unmodifiableMap(new HashMap<String, String>() {{
                                    put("tag1", "val2");
                                    put("tag2", "val2");
                                }}
                ));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:00:00.000Z", 2));
        series.setEntity(TEST_PREFIX + "entity-3");
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("tag2", "val3");
            }
        }));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:00:00.000Z", 3));
        series.setEntity(TEST_PREFIX + "entity-4");
        series.setTags(Collections.unmodifiableMap(new HashMap<String, String>() {
            {
                put("tag4", "val4");
            }
        }));
        sendSamplesToSeries(series, new Sample("2016-06-19T11:00:00.000Z", 4));
    }

    /**
     * Issue #3047
     * Test for query all tags documentation example.
     *
     * @see <a href="Query All Tags">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/all-tags.md</a>
     */
    @Test
    public void testAllTagsExample() {


        String sqlQuery =
                "SELECT entity, datetime, value, tags.*\n" +
                        "FROM 'sql-example-all-tags-metric'\n" +
                        "WHERE datetime >= '2016-06-19T11:00:00.000Z' and datetime < '2016-06-19T12:00:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("entity", "datetime", "value", "tags.tag4", "tags.tag2", "tags.tag1");

        assertTableColumnsNames(expectedColumnNames, resultTable);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-example-all-tags-entity-1", "2016-06-19T11:07:06.018Z", "1.0", "null", "null", "val1"),
                Arrays.asList("sql-example-all-tags-entity-2", "2016-06-19T11:07:06.018Z2", "2.0", "null", "val2", "val2"),
                Arrays.asList("sql-example-all-tags-entity-3", "2016-06-19T11:07:06.018Z2", "3.0", "null", "val3", "null"),
                Arrays.asList("sql-example-all-tags-entity-4", "2016-06-19T11:07:06.018Z2", "4.0", "val4", "null", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
