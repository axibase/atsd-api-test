package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlClauseOrderByAggregatedColumnAliasTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-order-by-aggregated-column-alias-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws IOException {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY1_NAME);
            setData(Arrays.asList(
                    new Sample("2016-06-17T19:16:01.000Z", "1"),
                    new Sample("2016-06-17T19:16:03.000Z", "3")
                    )
            );
        }});


        seriesList.add(new Series() {{
            setMetric(TEST_METRIC_NAME);
            setEntity(TEST_ENTITY2_NAME);
            setData(Arrays.asList(
                    new Sample("2016-06-17T19:16:02.000Z", "2"),
                    new Sample("2016-06-17T19:16:04.000Z", "4")
            ));
        }});

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * Issue #3185
     */
    @Test
    public void testColumnNames() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS 'aggregated' FROM '%s'\nGROUP BY entity\nORDER BY 'aggregated'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList("entity", "aggregated");

        assertTableColumnsNames(expectedColumnNames, resultTable);
    }


    /**
     * Issue #3185
     */
    @Test
    public void testASC() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS 'aggregated' FROM '%s'\nGROUP BY entity\nORDER BY 'aggregated'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2.0"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3185
     */
    @Test
    public void testDESC() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS 'aggregated' FROM '%s'\nGROUP BY entity\nORDER BY 'aggregated' DESC",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY2_NAME, "3.0"),
                Arrays.asList(TEST_ENTITY1_NAME, "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3185
     */
    @Test
    public void testOrderMultipleColumn() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) AS 'aggregated' FROM '%s'\nGROUP BY entity\nORDER BY entity ASC, 'aggregated' DESC",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY1_NAME, "2.0"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

}
