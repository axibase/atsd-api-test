package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlClauseWhereWithLimitDoubleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-where-with-limit-double-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDateDecimal("2016-06-19T11:00:00.000Z", new BigDecimal("1.23")),
                Sample.ofDateDecimal("2016-06-19T11:01:00.000Z", new BigDecimal("0.89"))
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("3282")
    @Test
    public void testGreaterOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value > 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testLessOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value <= 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testLessOrEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value <= 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testGreaterOrEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value > 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testValueAsRightOperand() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE  1.01 > value LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testMathematicalFunction() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE  sqrt(1.01) > value LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3282")
    @Test
    public void testEquals() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value = 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.emptyList();
        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3282")
    @Test
    public void testIsNull() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value IS NOT NULL LIMIT 2",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList("1.23"),
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3282")
    @Test
    public void testNotEquals() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE value <> 1.01 LIMIT 2",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList("1.23"),
                Collections.singletonList("0.89")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3282")
    @Test
    public void testSqrtFromValueComparison() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE SQRT(value) > 1.01 LIMIT 2",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }
}
