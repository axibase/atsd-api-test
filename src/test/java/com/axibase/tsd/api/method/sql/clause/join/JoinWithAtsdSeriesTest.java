package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JoinWithAtsdSeriesTest extends SqlTest {
    private static final String METRIC_NAME1 = TestNames.metric();
    private static final String METRIC_NAME2 = TestNames.metric();
    private static final String METRIC_NAME3 = TestNames.metric();
    private static final String METRIC_NAME4 = TestNames.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entityName = TestNames.entity();

        Series series1 = new Series(entityName, METRIC_NAME1);
        series1.addSamples(
                new Sample("2017-01-01T12:00:00.000Z", "1"),
                new Sample("2017-01-02T12:00:00.000Z", "2"),
                new Sample("2017-01-04T12:00:00.000Z", "4")
        );

        Series series2 = new Series(entityName, METRIC_NAME1, "t1", "tag");
        series2.addSamples(new Sample("2017-01-03T12:00:00.000Z", "3"));

        Series series3 = new Series(entityName, METRIC_NAME2, "t2", "tag");
        series3.addSamples(new Sample("2017-01-03T12:00:00.000Z", "5"));

        Series series4 = new Series(entityName, METRIC_NAME2);
        series4.addSamples(
                new Sample("2017-01-04T12:00:00.000Z", "6"),
                new Sample("2017-01-05T12:00:00.000Z", "7"),
                new Sample("2017-01-06T12:00:00.000Z", "8")
        );

        Series series5 = series4.copy();
        Registry.Metric.register(METRIC_NAME3);
        series5.setMetric(METRIC_NAME3);

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4, series5);

        Registry.Metric.register(METRIC_NAME4);
        MetricMethod.createOrReplaceMetricCheck(new Metric(METRIC_NAME4));
    }

    /**
     * #4089
     */
    @Test
    public void testJoinFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 JOIN m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 4        | 6        |
         */

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

        assertSqlQueryRows("Wrong result for join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testJoinEmptyFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 JOIN m3 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 4        | 6        |
         */

        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "JOIN '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME4
        );

        String[][] expectedRows = {};

        assertSqlQueryRows("Wrong result for empty join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testMultipleJoinFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value, t3.value FROM atsd_series t1 JOIN m2 t2 JOIN m3 t3 WHERE t1.metric = m1

        | t1.value | t2.value | t3.value |
        |----------|----------|----------|
        | 4        | 6        | 6        |
         */

        String sqlQuery= String.format(
                "SELECT t1.value, t2.value, t3.value " +
                        "FROM atsd_series t1 " +
                        "JOIN '%2$s' t2 " +
                        "JOIN '%3$s' t3 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2,
                METRIC_NAME3
        );

        String[][] expectedRows = {
                {"4", "6", "6"}
        };

        assertSqlQueryRows("Wrong result for multiple join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testJoinFromAtsdSeriesUsingEntity() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 JOIN USING ENTITY m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 3        | 5        |
        | 4        | 6        |
         */

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

        assertSqlQueryRows("Wrong result for join using entity from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testOuterJoinFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 OUTER JOIN m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 1        | null     |
        | 2        | null     |
        | 3        | null     |
        | null     | 5        |
        | 4        | 6        |
        | null     | 7        |
        | null     | 8        |
         */

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

        assertSqlQueryRows("Wrong result for outer join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testOuterJoinEmptyFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 OUTER JOIN m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 1        | null     |
        | 2        | null     |
        | 3        | null     |
        | 4        | null     |
         */

        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "OUTER JOIN '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME3
        );

        String[][] expectedRows = {
                {"1",    "null"},
                {"2",    "null"},
                {"3",    "null"},
                {"4",    "null"},
        };

        assertSqlQueryRows("Wrong result for empty outer join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testMultipleOuterJoinFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 OUTER JOIN m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value | t3.value |
        |----------|----------|----------|
        | 1        | null     | null     |
        | 2        | null     | null     |
        | 3        | null     | null     |
        | null     | 5        | null     |
        | 4        | 6        | 6        |
        | null     | 7        | 7        |
        | null     | 8        | 8        |
         */

        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "OUTER JOIN '%2$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1,
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {   "1",    "null",    "null"},
                {   "2",    "null",    "null"},
                {   "3",    "null",    "null"},
                {"null",       "5",    "null"},
                {   "4",       "6",       "6"},
                {"null",       "7",       "7"},
                {"null",       "8",       "8"},
        };

        assertSqlQueryRows("Wrong result for multiple outer join from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testOuterJoinFromAtsdSeriesUsingEntity() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 OUTER JOIN USING ENTITY m2 t2 WHERE t1.metric = m1

        | t1.value | t2.value |
        |----------|----------|
        | 1        | null     |
        | 2        | null     |
        | 3        | 5        |
        | 4        | 6        |
        | null     | 7        |
        | null     | 8        |
         */

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

        assertSqlQueryRows("Wrong result for outer join using entity from atsd_series", expectedRows, sqlQuery);
    }

    /**
     * #4089
     */
    @Test
    public void testSelfJoinFromAtsdSeries() {
        /*
        SELECT t1.value, t2.value FROM atsd_series t1 OUTER JOIN m1 t2 WHERE t1.metric = m1

        Self-join, error expected
         */

        String sqlQuery= String.format(
                "SELECT t1.value, t2.value " +
                        "FROM atsd_series t1 " +
                        "JOIN '%1$s' t2 " +
                        "WHERE t1.metric = '%1$s'",
                METRIC_NAME1
        );

        String expectedMessage = String.format("Self join is not supported (metric: %s)", METRIC_NAME1);

        assertBadRequest("Wrong result for self join with atsd_series", expectedMessage, queryResponse(sqlQuery));
    }
}
