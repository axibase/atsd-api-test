package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class OrderByAggregatedValuesFromMultipleTablesTest extends SqlTest {
    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC_1 = metric();
    private static final String TEST_METRIC_2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        {
            Series series = new Series(TEST_ENTITY, TEST_METRIC_1);
            series.setEntity(TEST_ENTITY);
            series.setMetric(TEST_METRIC_1);
            for (int i = 0; i < 10; i++) {
                series.addSamples(new Sample(Util.ISOFormat(Mocks.MILLS_TIME + i), i));
            }
            seriesList.add(series);
        }
        {
            Series series = new Series(null, TEST_METRIC_2);
            series.setEntity(TEST_ENTITY);
            for (int i = 0; i < 10; i++) {
                series.addSamples(new Sample(Util.ISOFormat(Mocks.MILLS_TIME + i), 2 * i));
            }
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3840
     */
    @Test
    public void testOrderByColumnDescWithAggregationsOfDifferentMetrics() {
        String sqlQuery = String.format(
                "SELECT SUM(t2.value)-SUM(t1.value) AS col " +
                        "FROM \"%s\" t1 JOIN \"%s\" t2 " +
                        "GROUP BY t1.PERIOD(2 MILLISECOND) " +
                        "ORDER BY col DESC",
                TEST_METRIC_1,
                TEST_METRIC_2
        );

        String[][] expectedRows = {
                {"17"},
                {"13"},
                {"9"},
                {"5"},
                {"1"}
        };

        assertSqlQueryRows("ORDER BY (DESC) doesn't sort column with aggregations of different metrics",
                            expectedRows, sqlQuery);
    }

    /**
     * #3840
     */
    @Test
    public void testOrderByColumnAscWithAggregationsOfDifferentMetrics() {
        String sqlQuery = String.format(
                "SELECT SUM(t2.value)-SUM(t1.value) AS col " +
                        "FROM \"%s\" t1 JOIN \"%s\" t2 " +
                        "GROUP BY t1.PERIOD(2 MILLISECOND) " +
                        "ORDER BY col ASC",
                TEST_METRIC_1,
                TEST_METRIC_2
        );

        String[][] expectedRows = {
                {"1"},
                {"5"},
                {"9"},
                {"13"},
                {"17"}
        };

        assertSqlQueryRows("ORDER BY (ASC) doesn't sort column with aggregations of different metrics",
                expectedRows, sqlQuery);
    }

    /**
     * #3840
     */
    @Test
    public void testOrderByColumnDescWithOneAggregationOfDifferentMetrics() {
        String sqlQuery = String.format(
                "SELECT SUM(t2.value-t1.value) AS col " +
                        "FROM \"%s\" t1 JOIN \"%s\" t2 " +
                        "GROUP BY t1.PERIOD(2 MILLISECOND) " +
                        "ORDER BY col DESC",
                TEST_METRIC_1,
                TEST_METRIC_2
        );

        String[][] expectedRows = {
                {"17"},
                {"13"},
                {"9"},
                {"5"},
                {"1"}
        };

        assertSqlQueryRows("ORDER BY (DESC) doesn't sort column with one aggregation of different metrics",
                            expectedRows, sqlQuery);
    }

    /**
     * #3840
     */
    @Test
    public void testOrderByColumnAscWithOneAggregationOfDifferentMetrics() {
        String sqlQuery = String.format(
                "SELECT SUM(t2.value-t1.value) AS col " +
                        "FROM \"%s\" t1 JOIN \"%s\" t2 " +
                        "GROUP BY t1.PERIOD(2 MILLISECOND) " +
                        "ORDER BY col ASC",
                TEST_METRIC_1,
                TEST_METRIC_2
        );

        String[][] expectedRows = {
                {"1"},
                {"5"},
                {"9"},
                {"13"},
                {"17"}
        };

        assertSqlQueryRows("ORDER BY (ASC) doesn't sort column with one aggregation of different metrics",
                expectedRows, sqlQuery);
    }
}
