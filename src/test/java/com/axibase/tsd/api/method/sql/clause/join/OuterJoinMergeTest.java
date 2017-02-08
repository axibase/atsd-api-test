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

public class OuterJoinMergeTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();

    private static final String TEST_ENTITY1_NAME = entity();
    private static final String TEST_ENTITY2_NAME = entity();
    private static final String TEST_ENTITY3_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME};
        String[] entityNames = {TEST_ENTITY1_NAME, TEST_ENTITY2_NAME, TEST_ENTITY3_NAME};

        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Entity.register(TEST_ENTITY3_NAME);

        for (String metricName : metricNames) {
            Registry.Metric.register(metricName);

            for (String entityName : entityNames) {

                for (int i = 1; i < 4; i++) {
                    Series series = new Series();
                    series.setEntity(entityName);
                    series.setMetric(metricName);
                    series.addData(new Sample(String.format("2017-01-0%1sT00:00:00.000Z", i), i));
                    series.addTag("tag1", "value1");
                    seriesList.add(series);
                }
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3872
     */
    @Test
    public void testOuterJoin() {
        String sqlQuery = String.format(
                "SELECT '%1$s'.entity, '%1$s'.value, '%2$s'.value FROM '%1$s' OUTER JOIN USING entity '%2$s'",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                { TEST_ENTITY1_NAME, "1", "1" },
                { TEST_ENTITY1_NAME, "2", "2" },
                { TEST_ENTITY1_NAME, "3", "3" },
                { TEST_ENTITY2_NAME, "1", "1" },
                { TEST_ENTITY2_NAME, "2", "2" },
                { TEST_ENTITY2_NAME, "3", "3" },
                { TEST_ENTITY3_NAME, "1", "1" },
                { TEST_ENTITY3_NAME, "2", "2" },
                { TEST_ENTITY3_NAME, "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN query gives wrong result", expectedRows, sqlQuery);
    }


    /**
     * #3872
     */
    @Test
    public void testOuterJoinWhereClause() {
        String sqlQuery = String.format(
                "SELECT '%1$s'.entity, '%1$s'.value, '%2$s'.value FROM '%1$s' OUTER JOIN USING entity '%2$s' WHERE '%1$s'.entity = '%3$s'",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_ENTITY1_NAME
        );

        String[][] expectedRows = {
                { TEST_ENTITY1_NAME, "1", "1" },
                { TEST_ENTITY1_NAME, "2", "2" },
                { TEST_ENTITY1_NAME, "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN query gives wrong result", expectedRows, sqlQuery);
    }


    /**
     * #3872
     */
    @Test
    public void testOuterJoinGroupClause() {
        String sqlQuery = String.format(
                "SELECT '%1$s'.entity, LAST('%1$s'.value), LAST('%2$s'.value) FROM '%1$s' OUTER JOIN USING entity '%2$s' GROUP BY '%1$s'.entity",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                { TEST_ENTITY1_NAME, "3", "3" },
                { TEST_ENTITY2_NAME, "3", "3" },
                { TEST_ENTITY3_NAME, "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN query gives wrong result", expectedRows, sqlQuery);
    }
}
