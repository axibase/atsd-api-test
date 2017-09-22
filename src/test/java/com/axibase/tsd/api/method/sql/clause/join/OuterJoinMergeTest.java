package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class OuterJoinMergeTest extends SqlTest {
    private static final int METRIC_COUNT = 2;
    private static final List<String> METRIC_NAMES = new ArrayList<>(METRIC_COUNT);

    private static final int ENTITY_COUNT = 3;
    private static final List<String> ENTITY_NAMES = new ArrayList<>(ENTITY_COUNT);

    private static final int VALUES_COUNT = 3;

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        for (int i = 0; i < METRIC_COUNT; i++) {
            String metric = metric();
            METRIC_NAMES.add(metric);
        }

        for (int i = 0; i < ENTITY_COUNT; i++) {
            String entity = entity();
            ENTITY_NAMES.add(entity);
        }

        for (String metricName : METRIC_NAMES) {
            for (String entityName : ENTITY_NAMES) {
                Series series = new Series(entityName, metricName, Mocks.TAGS);

                for (int i = 0; i < VALUES_COUNT; i++) {
                    series.addSamples(Sample.ofDateInteger(String.format("2017-01-0%1sT00:00:00.000Z", i + 1), i + 1));
                }

                seriesList.add(series);
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3872")
    @Test
    public void testOuterJoin() {
        String sqlQuery = String.format(
                "SELECT \"%1$s\".entity, \"%1$s\".value, \"%2$s\".value FROM \"%1$s\" OUTER JOIN USING entity \"%2$s\" ORDER BY \"%1$s\".entity",
                METRIC_NAMES.get(0),
                METRIC_NAMES.get(1)
        );

        String[][] expectedRows = {
                {ENTITY_NAMES.get(0), "1", "1"},
                {ENTITY_NAMES.get(0), "2", "2"},
                {ENTITY_NAMES.get(0), "3", "3"},
                {ENTITY_NAMES.get(1), "1", "1"},
                {ENTITY_NAMES.get(1), "2", "2"},
                {ENTITY_NAMES.get(1), "3", "3"},
                {ENTITY_NAMES.get(2), "1", "1"},
                {ENTITY_NAMES.get(2), "2", "2"},
                {ENTITY_NAMES.get(2), "3", "3"}
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }


    @Issue("3872")
    @Test
    public void testOuterJoinWhereClause() {
        String sqlQuery = String.format(
                "SELECT m1.entity, m1.value, m2.value FROM \"%1$s\" m1 OUTER JOIN USING entity \"%2$s\" m2 WHERE m1.entity = '%3$s'",
                METRIC_NAMES.get(0),
                METRIC_NAMES.get(1),

                ENTITY_NAMES.get(0)
        );

        String[][] expectedRows = {
                {ENTITY_NAMES.get(0),   "1",    "1"},
                {ENTITY_NAMES.get(0),   "2",    "2"},
                {ENTITY_NAMES.get(0),   "3",    "3"},
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }


    @Issue("3872")
    @Test
    public void testOuterJoinGroupClause() {
        String sqlQuery = String.format(
                "SELECT \"%1$s\".entity, LAST(\"%1$s\".value), LAST(\"%2$s\".value) FROM \"%1$s\" OUTER JOIN USING entity \"%2$s\" GROUP BY \"%1$s\".entity",
                METRIC_NAMES.get(0),
                METRIC_NAMES.get(1)
        );

        String[][] expectedRows = {
                {ENTITY_NAMES.get(0), "3", "3"},
                {ENTITY_NAMES.get(1), "3", "3"},
                {ENTITY_NAMES.get(2), "3", "3"},
        };

        assertSqlQueryRows("OUTER JOIN USING ENTITY query gives wrong result", expectedRows, sqlQuery);
    }
}
