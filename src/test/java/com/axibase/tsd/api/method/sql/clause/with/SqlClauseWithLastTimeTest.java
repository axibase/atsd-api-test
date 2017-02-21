package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;


public class SqlClauseWithLastTimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-with-last-time-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";


    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        Series series1 = new Series();
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.setMetric(TEST_METRIC_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-29T08:01:00.000Z", "0"),
                new Sample("2016-06-29T08:02:00.000Z", "1"),
                new Sample("2016-06-29T08:03:00.000Z", "2")
                )
        );

        Series series2 = new Series();
        series2.setEntity(TEST_ENTITY2_NAME);
        series2.setMetric(TEST_METRIC_NAME);
        series2.setData(Arrays.asList(
                new Sample("2016-06-29T08:04:00.000Z", "3"),
                new Sample("2016-06-29T08:05:00.000Z", "4"),
                new Sample("2016-06-29T08:06:00.000Z", "5")
                )
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
        //Required for last_time computing
        Thread.sleep(TestUtil.LAST_INSERT_WRITE_PERIOD);
    }

    /**
     * #3291
     */
    @Test
    public void testGreaterOrEqualsLastTime() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time >= last_time - 1*minute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "1"},
                {TEST_ENTITY1_NAME, "2"},
                {TEST_ENTITY2_NAME, "4"},
                {TEST_ENTITY2_NAME, "5"}
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #3291
     */
    @Test
    public void testGreaterLastTime() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time > last_time - 1*minute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "2"},
                {TEST_ENTITY2_NAME, "5"}
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3291
     */
    @Test
    public void testLessOrEqualsLastTime() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time <= last_time - 1*minute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "0"},
                {TEST_ENTITY1_NAME, "1"},
                {TEST_ENTITY2_NAME, "3"},
                {TEST_ENTITY2_NAME, "4"}
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3291
     */
    @Test
    public void testLessLastTime() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time < last_time - 1*minute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "0"},
                {TEST_ENTITY2_NAME, "3"},
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3291
     */
    @Test
    public void testCaseInsensitive() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time < LASt_TiMe - 1*miNute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY1_NAME, "0"},
                {TEST_ENTITY2_NAME, "3"},
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3291
     */
    @Test
    public void testWithWhereClause() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWHERE datetime > '2016-06-29T08:03:00.000Z'" +
                        "WITH time < LAST_TIME - 1*minute",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY2_NAME, "3"},
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3291
     */
    @Test
    public void testWithOrderByClause() {
        String sqlQuery = String.format(
                "SELECT entity, value FROM '%s' %nWITH time < LAST_TIME - 1*minute%n" +
                        "ORDER BY value DESC",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        String[][] expectedRows = {
                {TEST_ENTITY2_NAME, "3"},
                {TEST_ENTITY1_NAME, "0"}
        };

        StringTable resultTable = response.readEntity(StringTable.class);

        assertTableRowsExist(expectedRows, resultTable);
    }
}
