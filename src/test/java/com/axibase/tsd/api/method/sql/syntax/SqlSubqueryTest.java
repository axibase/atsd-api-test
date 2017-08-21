package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlSubqueryTest extends SqlTest {
    public static final String ENTITY_NAME = Mocks.entity();
    public static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(ENTITY_NAME, METRIC_NAME, "t1", "Tag 1", "t2", "Tag 2");
        series.addSamples(new Sample("2017-07-21T12:00:00.000Z", 1, "Text value"));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4133
     */
    @Test
    public void testSelectConst() {
        String nonExistentEntityName = Mocks.entity();

        String sqlQuery = String.format(
                "SELECT *\n" +
                        "FROM (\n" +
                        "    SELECT 1\n" +
                        ")",
                nonExistentEntityName
        );

        assertBadSqlRequest("Invalid subquery", sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testNonExistentEntity() {
        String nonExistentEntityName = Mocks.entity();

        String sqlQuery = String.format(
                "SELECT value, entity, datetime\n" +
                        "FROM (\n" +
                        "    SELECT value, '%s' as entity, datetime\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                nonExistentEntityName,
                METRIC_NAME
        );

        assertBadSqlRequest("Invalid expression for entity column", sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testColumnDuplicates() {
        String sqlQuery = String.format(
                "SELECT value, entity, datetime\n" +
                        "FROM (\n" +
                        "    SELECT value, entity, datetime, value as \"value\"\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        assertBadSqlRequest("Duplicate column name: value", sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testIncorrectCreatedTags() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT entity, value, time, 'x' AS \"tags\"\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        assertBadSqlRequest("Invalid expression for tags column", sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testJoin() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT *\n" +
                        "    FROM \"%s\"\n" +
                        "    JOIN \"%s\"\n" +
                        ")",
                METRIC_NAME,
                METRIC_NAME
        );

        String errorMessage = String.format("Self join is not supported (metric: %s)", METRIC_NAME);
        assertBadSqlRequest(errorMessage, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testCreatedTags() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM (\n" +
                        "    SELECT entity, value, time, 'x' AS \"tags\"\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        assertBadSqlRequest("Invalid expression for tags column", sqlQuery);
    }

    /**
     * #4133, #4377
     */
    @Test
    public void testSelectAsteriskTwice() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT * FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );


        String[][] expectedRows = {
                {"1500638400000", "2017-07-21T12:00:00.000Z", "1", "Text value",
                        METRIC_NAME, ENTITY_NAME, "t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Select from subquery with * doesn't work as expected", expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test(enabled = false)
    public void testSelectAsteriskNested() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT * FROM (\n" +
                        "        SELECT *\n" +
                        "        FROM \"%s\"\n" +
                        "    )\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1500638400000", "2017-07-21T12:00:00.000Z", "1", "Text value",
                        METRIC_NAME, ENTITY_NAME, "t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Nested subqueries do not work", expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testSelectText() {
        String sqlQuery = String.format(
                "SELECT text from (\n" +
                        "    SELECT entity, value, text, time\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );


        String[][] expectedRows = {
                {"Text value"}
        };

        assertSqlQueryRows("Wrong result when selecting text in subquery", expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testTagsToTags() {
        String sqlQuery = String.format(
                "SELECT tags FROM (\n" +
                        "    SELECT entity, value, time, tags\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Wrong result when selecting tags in subquery", expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testTagsToTagsExpansion() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM (\n" +
                        "    SELECT entity, value, time, tags\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"Tag 1", "Tag 2"}
        };

        assertSqlQueryRows("Wrong result when selecting tags in subquery and tags.* in main query",
                expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testTagsExpansionToTags() {
        String sqlQuery = String.format(
                "SELECT tags FROM (\n" +
                        "    SELECT entity, value, time, tags.*\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Wrong result when selecting tags.* in subquery and tags in main query",
                expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testTagsExpansionToTagsExpansion() {
        String sqlQuery = String.format(
                "SELECT tags.* FROM (\n" +
                        "    SELECT entity, value, time, tags.*\n" +
                        "    FROM \"%s\"\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"Tag 1", "Tag 2"}
        };

        assertSqlQueryRows("Wrong result when selecting tags.* in subquery and tags.* in main query",
                expectedRows, sqlQuery);
    }

    /**
     * #4133
     */
    @Test
    public void testOption() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT *\n" +
                        "    FROM \"%s\"\n" +
                        "    ORDER BY value\n" +
                        "    OPTION (ROW_MEMORY_THRESHOLD 0)\n" +
                        ")\n" +
                        "ORDER BY value\n" +
                        "OPTION (ROW_MEMORY_THRESHOLD 0)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1500638400000", "2017-07-21T12:00:00.000Z", "1", "Text value",
                        METRIC_NAME, ENTITY_NAME, "t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Wrong result with ROW_MEMORY_THRESHOLD option in subquery", expectedRows, sqlQuery);
    }

    @Test
    public void testDeepNested() {
        String sqlQuery = String.format(
                "SELECT * FROM (\n" +
                        "    SELECT * FROM (\n" +
                        "        SELECT * FROM \"%s\"\n" +
                        "    )\n" +
                        ")",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1500638400000", "2017-07-21T12:00:00.000Z", "1", "Text value",
                        METRIC_NAME, ENTITY_NAME, "t1=Tag 1;t2=Tag 2"}
        };

        assertSqlQueryRows("Wrong result with nested subqueries", expectedRows, sqlQuery);
    }

    @Test
    public void testValueExpressionsAndGroupBy() {
        String sqlQuery = String.format(
                "SELECT datetime, tags.t1, tags.t2, \n" +
                        "  sum(value)/count(value)\n" +
                        "FROM (\n" +
                        "    SELECT datetime, tags.t1, tags.t2,\n" +
                        "      CASE WHEN sum(value) >= 0.3 THEN 1 ELSE 0 END AS \"value\"\n" +
                        "    FROM \"method-sql-syntax-sql-subquery-test-metric-0\" \n" +
                        "    WHERE datetime >= '2017-07-21T11:00:00.000Z' AND datetime < '2017-07-21T13:00:00.000Z'\n" +
                        "      WITH INTERPOLATE (5 MINUTE)\n" +
                        "      GROUP BY datetime, tags.t1, tags.t2\n" +
                        ") \n" +
                        "GROUP BY tags.t1, tags.t2, PERIOD(1 hour, 'UTC')",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-07-21T12:00:00.000Z", "Tag 1", "Tag 2", "1"}
        };

        assertSqlQueryRows("Wrong result with value expressions and group by in subquery", expectedRows, sqlQuery);
    }
}
