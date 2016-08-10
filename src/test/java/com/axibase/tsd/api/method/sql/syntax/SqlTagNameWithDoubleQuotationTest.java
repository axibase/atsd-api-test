package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTagNameWithDoubleQuotationTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-tag-name-with-double-quotation-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public static void prepareData() throws Exception {

        final Map<String, String> tags = Collections.unmodifiableMap(new HashMap<String, String>() {{
            put("tag", "0");
            put("tag\"quotation", "1");
            put("tag+plus", "2");
            put("tag-minus", "3");
            put("tag*multiple", "4");
            put("tag/division", "5");
            put("tag\"quotation'", "6");
        }});

        SeriesMethod.insertSeriesCheck(
                new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
                    addData(new Sample("2016-06-19T11:00:00.500Z", "0"));
                    setTags(tags);
                }}
        );

        MetricMethod.updateMetric(TEST_METRIC_NAME, new Metric() {{
            setTags(tags);
        }});
        EntityMethod.updateEntity(TEST_ENTITY_NAME, new Entity() {{
            setTags(tags);
        }});
    }
    /*
    Following tests related to #3085 issue
    Tag name With Special Characters
     */

    /**
     * Issue #3085
     */
    @Test
    public void testPlainName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag\", metric.tags.\"tag\", entity.tags.\"tag\" FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("0", "0", "0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testDoubleQuotationName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag\"\"quotation\", metric.tags.\"tag\"\"quotation\", entity.tags.\"tag\"\"quotation\" FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("1", "1", "1")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testPlusName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag+plus\", metric.tags.\"tag+plus\", entity.tags.\"tag+plus\" FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2", "2", "2")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testMinusName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag-minus\", metric.tags.\"tag-minus\", entity.tags.\"tag-minus\" FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("3", "3", "3")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testMultipleName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag*multiple\", metric.tags.\"tag*multiple\", entity.tags.\"tag*multiple\" FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("4", "4", "4")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testDivisionName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag/division\", metric.tags.\"tag/division\", entity.tags.\"tag/division\" FROM '%s'\n" +
                        "WHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("5", "5", "5")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3085
     */
    @Test
    public void testDifferentQuotationName() {
        String sqlQuery = String.format(
                "SELECT tags.\"tag\"\"quotation'\", metric.tags.\"tag\"\"quotation'\", entity.tags.\"tag\"\"quotation'\" " +
                        "FROM '%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("6", "6", "6")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
