package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class JoinWithTagOrEntityFilter extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC3_NAME};
        String[] tags = {"123", "123", "abc4"};

        Registry.Entity.register(TEST_ENTITY_NAME);

        for (int i = 0; i < metricNames.length; i++) {
            String metricName = metricNames[i];
            Registry.Metric.register(metricName);

            Series series = new Series();
            series.setEntity(TEST_ENTITY_NAME);
            series.setMetric(metricName);

            series.addData(new Sample("2016-06-03T09:20:00.000Z", i + 1));

            String tag = tags[i];
            series.addTag("tag", tag);

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /**
     * #3756
     */
    @Test
    public void testJoinWithEntityFilter() {
        String sqlQuery = String.format(
                    "SELECT t1.value, t2.value " +
                    "FROM '%1$s' t1 JOIN '%2$s' t2 " +
                    "WHERE t1.entity = '%3$s' AND t2.entity = '%3$s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Entity filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithEntityNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN '%2$s' t2 " +
                        "WHERE t1.entity IS NOT NULL AND t2.entity IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Entity NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithEntityFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.entity = '%3$s' AND t2.entity = '%3$s'",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME, TEST_ENTITY_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Entity filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithEntityNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.entity IS NOT NULL AND t2.entity IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Entity NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithTagFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN '%2$s' t2 " +
                        "WHERE t1.tags.tag = '123' AND t2.tags.tag = '123'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Tag filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinWithTagNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN '%2$s' t2 " +
                        "WHERE t1.tags.tag IS NOT NULL AND t2.tags.tag IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows("JOIN with Tag NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithTagFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.tags.tag = '123' AND t2.tags.tag = 'abc4'",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Tag filter gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3756
     */
    @Test
    public void testJoinUsingEntityWithTagNotNullFilter() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value " +
                        "FROM '%1$s' t1 JOIN USING ENTITY '%2$s' t2 " +
                        "WHERE t1.tags.tag IS NOT NULL AND t2.tags.tag IS NOT NULL",
                TEST_METRIC1_NAME, TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "3"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with Tag NOT NULL filter gives wrong result", expectedRows, sqlQuery);
    }
}
