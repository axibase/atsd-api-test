package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JoinWithAtsdSeriesTest extends SqlTest {
    private static final String METRIC_NAME1 = TestNames.metric();
    private static final String METRIC_NAME2 = TestNames.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entityName = TestNames.entity();

        Series series1 = new Series(entityName, METRIC_NAME1);
        series1.addData(
                new Sample("2017-01-01T12:00:00.000Z", "1"),
                new Sample("2017-01-02T12:00:00.000Z", "2"),
                new Sample("2017-01-04T12:00:00.000Z", "4")
        );

        Series series2 = new Series(entityName, METRIC_NAME1, "t1", "tag");
        series2.addData(new Sample("2017-01-03T12:00:00.000Z", "3"));

        Series series3 = new Series(entityName, METRIC_NAME2, "t2", "tag");
        series3.addData(new Sample("2017-01-03T12:00:00.000Z", "5"));

        Series series4 = new Series(entityName, METRIC_NAME2);
        series4.addData(
                new Sample("2017-01-04T12:00:00.000Z", "6"),
                new Sample("2017-01-05T12:00:00.000Z", "7"),
                new Sample("2017-01-06T12:00:00.000Z", "8")
        );

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);
    }

    /**
     * #4089
     */
    @Test
    public void testJoinFromAtsdSeries() {
        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "JOIN '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {"4", "6"}
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testJoinFromAtsdSeriesUsingEntity() {
        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {"3", "5"},
                {"4", "6"}
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testOuterJoinFromAtsdSeries() {
        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "OUTER JOIN '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {   "1",    "null"},
                {   "2",    "null"},
                {   "3",    "null"},
                {"null",       "5"},
                {   "4",       "6"},
                {"null",       "7"},
                {"null",       "8"},
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }



    /**
     * #4089
     */
    @Test
    public void testOuterJoinFromAtsdSeriesUsingEntity() {
        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "OUTER JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {   "1",    "null"},
                {   "2",    "null"},
                {   "3",       "5"},
                {   "4",       "6"},
                {"null",       "7"},
                {"null",       "8"},
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testSelfJoinFromAtsdSeries() {
        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "JOIN '%1$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1
        );

        String expectedMessage = String.format("Self join is not supported (metric: %s)", METRIC_NAME1);

        assertBadRequest("", expectedMessage, queryResponse(sqlQuery));
    }
}
