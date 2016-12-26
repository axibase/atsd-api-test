package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlClauseJoinUsingEntity extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_METRIC4_NAME = metric();
    private static final String TEST_METRIC5_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();
        Series series2 = new Series();
        Series series3 = new Series();
        Series series4 = new Series();
        Series series5 = new Series();

        series1.setMetric(TEST_METRIC1_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );
        series1.addTag("tag1", "4");

        series2.setMetric(TEST_METRIC2_NAME);
        series2.setEntity(TEST_ENTITY_NAME);
        series2.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "2")
                )
        );
        series2.addTag("tag1", "123");

        series3.setMetric(TEST_METRIC3_NAME);
        series3.setEntity(TEST_ENTITY_NAME);
        series3.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "3")
                )
        );
        series3.addTag("tag1", "123");

        series4.setMetric(TEST_METRIC4_NAME);
        series4.setEntity(TEST_ENTITY_NAME);
        series4.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "4")
                )
        );

        series5.setMetric(TEST_METRIC5_NAME);
        series5.setEntity(TEST_ENTITY_NAME);
        series5.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "5")
                )
        );
        series5.addTag("tag2", "123");

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2, series3, series4, series5));
    }

    /**
     * #3741
     */
    @Test
    public void testJoin() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Query gives some result, but should give none");
    }

    /**
     * #3741
     */
    @Test
    public void testJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1", "2"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join Using Entity gives wrong result");
    }

    /**
     * #3741
     */
    @Test
    public void testJoinUsingEntitySameTags() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"2", "3"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join Using Entity with same tags gives wrong result");
    }

    /**
     * #3741
     */
    @Test
    public void testJoinUsingEntityOneWithoutTags() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC3_NAME,
                TEST_METRIC4_NAME
        );

        String[][] expectedRows = {
                {"3", "4"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join Using Entity (one metric has no tags) gives wrong result");
    }

    /**
     * #3741
     */
    @Test
    public void testJoinUsingEntityDifferentTags() {
        String sqlQuery = String.format(
                "SELECT t1.value, t2.value FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC3_NAME,
                TEST_METRIC5_NAME
        );

        String[][] expectedRows = {
                {"3", "5"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join Using Entity with different tags gives wrong result");
    }
}
