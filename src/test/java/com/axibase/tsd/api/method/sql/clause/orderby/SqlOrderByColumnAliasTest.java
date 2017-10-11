package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlOrderByColumnAliasTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String testEntity = entity();

        List<Series> seriesList = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Series series = new Series(testEntity, TEST_METRIC);
            series.addSamples(Sample.ofDateInteger(String.format("2017-01-01T00:0%s:00Z", i), i));

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3838")
    @Test
    public void testOrderByColumnAlias() {
        String sqlQuery = String.format(
                "SELECT value as \"ValueColumn\" FROM \"%s\" ORDER BY \"ValueColumn\"",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "1" },
                { "2" },
                { "3" }
        };

        assertSqlQueryRows("ORDER BY column alias error", expectedRows, sqlQuery);
    }

    @Issue("3838")
    @Test
    public void testOrderByColumnAliasWithoutQuotes() {
        String sqlQuery = String.format(
                "SELECT value as \"ValueColumn\" FROM \"%s\" ORDER BY ValueColumn",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "1" },
                { "2" },
                { "3" }
        };

        assertSqlQueryRows("ORDER BY column alias without quotes error", expectedRows, sqlQuery);
    }

    @Issue("3838")
    @Test
    public void testOrderByColumnAliasExpression() {
        String sqlQuery = String.format(
                "SELECT value / 2 as \"ValueColumn\" FROM \"%s\" ORDER BY \"ValueColumn\" / 2",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "0.5" },
                { "1" },
                { "1.5" }
        };

        assertSqlQueryRows("ORDER BY column alias expression error", expectedRows, sqlQuery);
    }

    @Issue("3838")
    @Test
    public void testOrderByColumnAliasExpressionWithoutQuotes() {
        String sqlQuery = String.format(
                "SELECT value / 2 as \"ValueColumn\" FROM \"%s\" ORDER BY ValueColumn / 2",
                TEST_METRIC
        );

        String[][] expectedRows = {
                { "0.5" },
                { "1" },
                { "1.5" }
        };

        assertSqlQueryRows("ORDER BY column alias expression without quotes error", expectedRows, sqlQuery);
    }

    @Issue("3838")
    @Test
    public void testOrderByNonExistingColumnAliasExpression() {
        String sqlQuery = String.format(
                "SELECT value / 2 as \"ValueColumn\" FROM \"%s\" ORDER BY \"NonExistingColumn\" / 2",
                TEST_METRIC
        );

        Response response = queryResponse(sqlQuery);

        assertBadRequest("Unexpected expression: '\"NonExistingColumn\"'", response);
    }
}
