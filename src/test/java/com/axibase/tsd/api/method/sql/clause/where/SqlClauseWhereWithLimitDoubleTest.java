package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlClauseWhereWithLimitDoubleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-clause-where-with-limit-double-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2016-06-19T11:00:00.000Z", "1.23"));
        series.addData(new Sample("2016-06-19T11:01:00.000Z", "0.89"));
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3282
     */
    @Test
    public void testGreaterOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value > 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3282
     */
    @Test
    public void testLessOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value <= 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * #3282
     */
    @Test
    public void testLessOrEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value <= 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * #3282
     */
    @Test
    public void testGreaterOrEqualsOperator() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value > 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * #3282
     */
    @Test
    public void testValueAsRightOperand() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE  1.01 > value LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3282
     */
    @Test
    public void testMathematicalFunction() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE  sqrt(1.01) > value LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0.89")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * #3282
     */
    @Test
    public void testEquals() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value = 1.01 LIMIT 1",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3282
     */
    @Test
    public void testIsNull() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value IS NOT NULL LIMIT 2",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList("1.23"),
                Collections.singletonList("0.89")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3282
     */
    @Test
    public void testNotEquals() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE value <> 1.01 LIMIT 2",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList("1.23"),
                Collections.singletonList("0.89")
        );


        assertTableRows(expectedRows, resultTable);
    }


    /**
     * #3282
     */
    @Test
    public void testSqrtFromValueComparison() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE SQRT(value) > 1.01 LIMIT 2",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("1.23")
        );


        assertTableRows(expectedRows, resultTable);
    }
}
