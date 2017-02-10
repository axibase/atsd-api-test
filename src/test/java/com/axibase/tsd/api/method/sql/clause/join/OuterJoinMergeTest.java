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
    private static final String[] metricNames = new String[2];
    private static final String[] entityNames = new String[3];

    @BeforeClass
    public static void prepareData() throws Exception {

        for (int i = 0; i < metricNames.length; i++) {
            metricNames[i] = metric();
            Registry.Metric.register(metricNames[i]);
        }

        for (int i = 0; i < entityNames.length; i++) {
            entityNames[i] = entity();
            Registry.Entity.register(entityNames[i]);
        }

        List<Series> seriesList = new ArrayList<>();

        for (String metricName : metricNames) {
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
                metricNames[0],
                metricNames[1]
        );

        String[][] expectedRows = {
                { entityNames[0], "1", "1" },
                { entityNames[0], "2", "2" },
                { entityNames[0], "3", "3" },
                { entityNames[1], "1", "1" },
                { entityNames[1], "2", "2" },
                { entityNames[1], "3", "3" },
                { entityNames[2], "1", "1" },
                { entityNames[2], "2", "2" },
                { entityNames[2], "3", "3" }
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }


    /**
     * #3872
     */
    @Test
    public void testOuterJoinWhereClause() {
        String sqlQuery = String.format(
                "SELECT '%1$s'.entity, '%1$s'.value, '%2$s'.value FROM '%1$s' OUTER JOIN USING entity '%2$s' WHERE '%1$s'.entity = '%3$s'",
                metricNames[0],
                metricNames[1],

                entityNames[0]
        );

        String[][] expectedRows = {
                { entityNames[0], "1", "1" },
                { entityNames[0], "2", "2" },
                { entityNames[0], "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }


    /**
     * #3872
     */
    @Test
    public void testOuterJoinGroupClause() {
        String sqlQuery = String.format(
                "SELECT '%1$s'.entity, LAST('%1$s'.value), LAST('%2$s'.value) FROM '%1$s' OUTER JOIN USING entity '%2$s' GROUP BY '%1$s'.entity",
                metricNames[0],
                metricNames[1]
        );

        String[][] expectedRows = {
                {  entityNames[0], "3", "3" },
                {  entityNames[1], "3", "3" },
                {  entityNames[2], "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }
}
