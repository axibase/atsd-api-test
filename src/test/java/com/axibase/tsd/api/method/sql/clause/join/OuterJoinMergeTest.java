package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class OuterJoinMergeTest extends SqlTest {
    private static final String[] METRIC_NAMES = new String[2];
    private static final String[] ENTITY_NAMES = new String[3];

    @BeforeClass
    public static void prepareData() throws Exception {

        for (int i = 0; i < METRIC_NAMES.length; i++) {
            METRIC_NAMES[i] = metric();
            Registry.Metric.register(METRIC_NAMES[i]);
        }

        for (int i = 0; i < ENTITY_NAMES.length; i++) {
            ENTITY_NAMES[i] = entity();
            Registry.Entity.register(ENTITY_NAMES[i]);
        }

        List<Series> seriesList = new ArrayList<>();

        for (String metricName : METRIC_NAMES) {
            for (String entityName : ENTITY_NAMES) {
                for (int i = 1; i < 4; i++) {
                    Series series = new Series();
                    series.setEntity(entityName);
                    series.setMetric(metricName);
                    series.addData(new Sample(String.format("2017-01-0%1sT00:00:00.000Z", i), i));
                    series.setTags(Mocks.TAGS);
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
                METRIC_NAMES[0],
                METRIC_NAMES[1]
        );

        String[][] expectedRows = {
                { ENTITY_NAMES[0], "1", "1" },
                { ENTITY_NAMES[0], "2", "2" },
                { ENTITY_NAMES[0], "3", "3" },
                { ENTITY_NAMES[1], "1", "1" },
                { ENTITY_NAMES[1], "2", "2" },
                { ENTITY_NAMES[1], "3", "3" },
                { ENTITY_NAMES[2], "1", "1" },
                { ENTITY_NAMES[2], "2", "2" },
                { ENTITY_NAMES[2], "3", "3" }
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
                METRIC_NAMES[0],
                METRIC_NAMES[1],

                ENTITY_NAMES[0]
        );

        String[][] expectedRows = {
                { ENTITY_NAMES[0], "1", "1" },
                { ENTITY_NAMES[0], "2", "2" },
                { ENTITY_NAMES[0], "3", "3" },
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
                METRIC_NAMES[0],
                METRIC_NAMES[1]
        );

        String[][] expectedRows = {
                {  ENTITY_NAMES[0], "3", "3" },
                {  ENTITY_NAMES[1], "3", "3" },
                {  ENTITY_NAMES[2], "3", "3" },
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }
}
