package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


public class SqlClauseOrderByAggregatedColumnAliasTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-order-by-aggregated-column-alias-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME);
        series1.addSamples(
                Sample.ofDateInteger("2016-06-17T19:16:01.000Z", 1),
                Sample.ofDateInteger("2016-06-17T19:16:03.000Z", 3)
        );

        Series series2 = new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2016-06-17T19:16:02.000Z", 2),
                Sample.ofDateInteger("2016-06-17T19:16:04.000Z", 4)
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    @Issue("3185")
    @Test
    public void testColumnNames() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS \"aggregated\" FROM \"%s\" %nGROUP BY entity %nORDER BY \"aggregated\"",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnLabels = Arrays.asList("entity", "aggregated");

        assertTableColumnsLabels(expectedColumnLabels, resultTable);
    }


    @Issue("3185")
    @Test
    public void testASC() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS \"aggregated\" FROM \"%s\" %nGROUP BY entity %nORDER BY \"aggregated\"",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2"),
                Arrays.asList(TEST_ENTITY2_NAME, "3")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3185")
    @Test
    public void testDESC() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS \"aggregated\" FROM \"%s\" %nGROUP BY entity %nORDER BY \"aggregated\" DESC",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "3"),
                Arrays.asList(TEST_ENTITY1_NAME, "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3185")
    @Test
    public void testOrderMultipleColumn() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS \"aggregated\" FROM \"%s\" %nGROUP BY entity %nORDER BY entity ASC, \"aggregated\" DESC",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2"),
                Arrays.asList(TEST_ENTITY2_NAME, "3")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

}
