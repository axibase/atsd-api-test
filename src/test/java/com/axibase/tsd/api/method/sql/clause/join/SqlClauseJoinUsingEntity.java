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
    private static String[] testMetricNames;
    private static final String TEST_ENTITY_NAME = entity();


    private static Series[] generateData(String[][] tags) {
        Series[] arraySeries = new Series[tags.length];
        testMetricNames = new String[tags.length];

        for (int i = 0; i < tags.length; i++) {
            arraySeries[i] = new Series();
            testMetricNames[i] = metric();
            arraySeries[i].setMetric(testMetricNames[i]);
            arraySeries[i].setEntity(TEST_ENTITY_NAME);
            arraySeries[i].setData(Collections.singletonList(
                    new Sample("2016-06-03T09:20:00.000Z", i + 1)
                    )
            );
            if (tags[i][0] != null) {
                arraySeries[i].addTag(tags[i][0], tags[i][1]);
            }
        }

        return arraySeries;
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        String[][] tags = {
                {"tag1", "4"},
                {"tag1", "123"},
                {"tag1", "123"},
                {null},
                {"tag2", "123"}
        };
        Series[] series = generateData(tags);

        SeriesMethod.insertSeriesCheck(Arrays.asList(series));
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

        assertSqlQueryRows("Query gives some result, but should give none", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Join Using Entity gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Join Using Entity with same tags gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Join Using Entity (one metric has no tags) gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Join Using Entity with different tags gives wrong result", expectedRows, sqlQuery);
    }
}
