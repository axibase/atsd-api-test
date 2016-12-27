package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlClauseJoinUsingEntity extends SqlTest {
    private static String[] testMetricNames = new String[5];

    private static Series[] generateData(String[] tags) {
        Series[] arraySeries = new Series[tags.length / 2];

        for (int i = 0; i < tags.length / 2; i++) {
            arraySeries[i] = new Series();
            testMetricNames[i] = metric();
            arraySeries[i].setMetric(testMetricNames[i]);
            arraySeries[i].setEntity(entity());
            arraySeries[i].setData(Collections.singletonList(
                    new Sample("2016-06-03T09:20:00.000Z", (new Integer(i + 1)).toString())
                    )
            );
            if (! tags[2* i].equals("")) {
                arraySeries[i].addTag(tags[2 * i], tags[2 * i + 1]);
            }
        }

        return arraySeries;
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        String[] tags = {"tag1", "4", "tag1", "123", "tag1", "123", "", "", "tag2", "123"};
        Series[] series = generateData(tags);

        SeriesMethod.insertSeriesCheck(Arrays.asList(series[0], series[1], series[2], series[3], series[4]));
    }

    /**
     * #3741
     */
    @Test
    public void testJoin() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' t1 JOIN '%s' t2",
                testMetricNames[0],
                testMetricNames[1]
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
                testMetricNames[0],
                testMetricNames[1]
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
                testMetricNames[1],
                testMetricNames[2]
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
                testMetricNames[2],
                testMetricNames[3]
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
                testMetricNames[2],
                testMetricNames[4]
        );

        String[][] expectedRows = {
                {"3", "5"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Join Using Entity with different tags gives wrong result");
    }
}
