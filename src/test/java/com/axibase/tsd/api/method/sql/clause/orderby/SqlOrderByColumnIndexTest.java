package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlOrderByColumnIndexTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-order-by-column-index-";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public void prepareData() throws IOException {
        Registry.Metric.register(TEST_METRIC_NAME);
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);

        Series series1 = new Series(),
                series2 = new Series();

        series1.setMetric(TEST_METRIC_NAME);
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:00.000Z", "6"),
                new Sample("2016-06-19T11:00:01.000Z", "2"),
                new Sample("2016-06-19T11:00:02.000Z", "4")
        ));

        series2.setMetric(TEST_METRIC_NAME);
        series2.setEntity(TEST_ENTITY2_NAME);
        series2.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:04.000Z", "3"),
                new Sample("2016-06-19T11:00:05.000Z", "1"),
                new Sample("2016-06-19T11:00:06.000Z", "5")
        ));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    /**
     * Issue #3191
     */
    @Test
    public void test123Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 1,2,3",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3191
     */
    @Test
    public void test132Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 1,3,2",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3191
     */
    @Test
    public void test213Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 2,1,3",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3191
     */
    @Test
    public void test231Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 2,3,1",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3191
     */
    @Test
    public void test312Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 3,1,2",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3191
     */
    @Test
    public void test321Order() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 3,2,1",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3191
     */
    @Test
    public void test1Decs2Decs3DecsOrder() {
        String sqlQuery = String.format(
                "SELECT entity, value, datetime FROM '%s'\nORDER BY 1 DESC,2 DESC,3 DESC",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "5.0", "2016-06-19T11:00:06.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0", "2016-06-19T11:00:04.000Z"),
                Arrays.asList(TEST_ENTITY2_NAME, "1.0", "2016-06-19T11:00:05.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "6.0", "2016-06-19T11:00:00.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "4.0", "2016-06-19T11:00:02.000Z"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0", "2016-06-19T11:00:01.000Z")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
