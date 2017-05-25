package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class SqlOrderByColumnAliasTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    @BeforeTest
    public static void prepareData() throws Exception {
        String testEntity = entity();
        Registry.Entity.register(testEntity);
        Registry.Metric.register(TEST_METRIC);

        List<Series> seriesList = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Series series = new Series(testEntity, TEST_METRIC);
            series.addData(new Sample(String.format("2017-01-01T00:0%s:00Z", i), i));

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3838
     */
    @Test
    public void testOrderByColumnAlias() {
        String sqlQuery = String.format(
                "SELECT value as 'ValueColumn' FROM '%s' ORDER BY 'ValueColumn'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "1" },
                { "2" },
                { "3" }
        };

        assertSqlQueryRows("ORDER BY column alias error", expectedRows, sqlQuery);
    }

    /**
     * #3838
     */
    @Test
    public void testOrderByColumnAliasWithoutQuotes() {
        String sqlQuery = String.format(
                "SELECT value as 'ValueColumn' FROM '%s' ORDER BY ValueColumn",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "1" },
                { "2" },
                { "3" }
        };

        assertSqlQueryRows("ORDER BY column alias without quotes error", expectedRows, sqlQuery);
    }

    /**
     * #3838
     */
    @Test
    public void testOrderByColumnAliasExpression() {
        String sqlQuery = String.format(
                "SELECT value / 2 as 'ValueColumn' FROM '%s' ORDER BY 'ValueColumn' / 2",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "0.5" },
                { "1" },
                { "1.5" }
        };

        assertSqlQueryRows("ORDER BY column alias expression error", expectedRows, sqlQuery);
    }

    /**
     * #3838
     */
    @Test
    public void testOrderByColumnAliasExpressionWithoutQuotes() {
        String sqlQuery = String.format(
                "SELECT value / 2 as 'ValueColumn' FROM '%s' ORDER BY ValueColumn / 2",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "0.5" },
                { "1" },
                { "1.5" }
        };

        assertSqlQueryRows("ORDER BY column alias expression without quotes error", expectedRows, sqlQuery);
    }

    /**
     * #3838
     */
    @Test
    public void testOrderByNonExistingColumnAliasExpression() {
        String sqlQuery = String.format(
                "SELECT value / 2 as 'ValueColumn' FROM '%s' ORDER BY 'NonExistingColumn' / 2",
                TEST_METRIC
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest("Invalid expression: ''NonExistingColumn' / 2'", response);
    }
}
