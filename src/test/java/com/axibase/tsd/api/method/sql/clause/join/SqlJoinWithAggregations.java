package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class SqlJoinWithAggregations extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();
        Series series2 = new Series();

        series1.setMetric(TEST_METRIC1_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1"),
                new Sample("2016-06-03T09:21:00.000Z", "2"),
                new Sample("2016-06-03T09:22:00.000Z", "3")
                )
        );

        series2.setMetric(TEST_METRIC2_NAME);
        series2.setEntity(TEST_ENTITY_NAME);
        series2.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "3"),
                new Sample("2016-06-03T09:21:00.000Z", "4"),
                new Sample("2016-06-03T09:22:00.000Z", "5")
                )
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    /**
     * #3695
     */
    @Test
    public void testJoinWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(t1.value), sum(t2.value) FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"6.0", "12.0"}
        };

        assertSqlQueryRows("Join without Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testJoinWithGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(t1.value), sum(t2.value) FROM '%s' t1 JOIN '%s' t2 GROUP BY t2.period(2 minute)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );


        String[][] expectedRows = {
                {"3", "7"},
                {"3", "5"}
        };

        assertSqlQueryRows("Join with Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testCountJoinWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT count(t1.value), count(t2.value) FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"3", "3"}
        };

        assertSqlQueryRows("Count Join without Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testCountJoinWithGroupBy() {
        String sqlQuery = String.format(
                "SELECT count(t1.value), count(t2.value) FROM '%s' t1 JOIN '%s' t2 GROUP BY t2.period(2 minute)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"2", "2"},
                {"1", "1"}
        };

        assertSqlQueryRows("Count Join with Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testDeltaAndStddevJoinWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT delta(t1.value), stddev(t2.value) FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"2", "1"}
        };

        assertSqlQueryRows("Delta and Stddev with Join without Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testDeltaAndStddevJoinWithGroupBy() {
        String sqlQuery = String.format(
                "SELECT delta(t1.value), stddev(t2.value) FROM '%s' t1 JOIN '%s' t2 GROUP BY t2.period(1 year)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"2", "1"}
        };

        assertSqlQueryRows("Delta and Stddev with Join with Group by gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testJoinWithGroupByAndHaving() {
        String sqlQuery = String.format(
                "SELECT sum(t1.value), sum(t2.value) FROM '%s' t1 JOIN '%s' t2 GROUP BY t2.period(1 minute) HAVING (sum(t1.value) > 2)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"3", "5"}
        };

        assertSqlQueryRows("Join with Group by and Having gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testJoinWithGroupByAndWhere() {
        String sqlQuery = String.format(
                "SELECT sum(t1.value), sum(t2.value) FROM '%s' t1 JOIN '%s' t2 WHERE t1.value > 2 GROUP BY t2.period(1 year)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"3.0", "5.0"}
        };

        assertSqlQueryRows("Join with Group by and Where gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3695
     */
    @Test
    public void testJoinWithWhere() {
        String sqlQuery = String.format(
                "SELECT sum(t1.value), sum(t2.value) FROM '%s' t1 JOIN '%s' t2 WHERE t1.value > 2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"3.0", "5.0"}
        };

        assertSqlQueryRows("Join with Where gives wrong result", expectedRows, sqlQuery);
    }
}
