package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlSyntaxComputedColumnsInClausesTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-syntax-computed-columns-in-clauses-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.setData(Arrays.asList(
                new Sample("2016-06-19T11:00:00.500Z", "0"),
                new Sample("2016-06-19T11:00:01.500Z", "1"),
                new Sample("2016-06-19T11:00:02.500Z", "-3")
        ));
        SeriesMethod.insertSeriesCheck(series);
    }

    @Test
    public void testSelectExpression() {
        String sqlQuery =
                "SELECT SUM(ABS(value)-1) AS \"computed\" FROM '" + TEST_METRIC_NAME + "'\n";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedColumns = Collections.singletonList(
                Collections.singletonList("1.0")
        );

        assertTableContainsColumnsValues(expectedColumns, resultTable, "computed");

    }

    @Test
    public void testOrderByComputedAlias() {
        String sqlQuery =
                "SELECT SUM(ABS(value)-1) AS \"computed\" FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY \"computed\"";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedColumns = Collections.singletonList(
                Collections.singletonList("1.0")
        );

        assertTableContainsColumnsValues(expectedColumns, resultTable, "computed");

    }

    @Test
    public void testOrderByComputedColumn() {
        String sqlQuery =
                "SELECT SUM(ABS(value)-1) AS \"computed\" FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY SUM(ABS(value)-1)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedColumns = Collections.singletonList(
                Collections.singletonList("1.0")
        );

        assertTableContainsColumnsValues(expectedColumns, resultTable, "computed");

    }

    @Test
    public void testOrder() {
        String sqlQuery =
                "SELECT SUM(ABS(value)-1) AS \"computed\" FROM '" + TEST_METRIC_NAME + "'\n" +
                        "ORDER BY SUM(ABS(value)-1)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedColumns = Collections.singletonList(
                Collections.singletonList("1.0")
        );

        assertTableContainsColumnsValues(expectedColumns, resultTable, "computed");

    }
}
