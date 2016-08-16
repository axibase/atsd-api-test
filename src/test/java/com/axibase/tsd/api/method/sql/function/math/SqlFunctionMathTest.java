package com.axibase.tsd.api.method.sql.function.math;

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
public class SqlFunctionMathTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-function-math-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(
                new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
                    setData(Arrays.asList(
                            new Sample("2016-06-29T08:00:00.000Z", "2.11"),
                            new Sample("2016-06-29T08:00:01.000Z", "7.567"),
                            new Sample("2016-06-29T08:00:02.000Z", "-1.23")
                            )
                    );
                }}
        );
    }
    /*
    Following tasks related to #3049 issue
     */

    /**
     * Issue #3049
     */
    @Test
    public void testAbs() {
        String sqlQuery = String.format(
                "SELECT ABS(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.abs(2.11))),
                Collections.singletonList(Double.toString(Math.abs(7.567))),
                Collections.singletonList(Double.toString(Math.abs(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testСeil() {
        String sqlQuery = String.format(
                "SELECT CEIL(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.ceil(2.11))),
                Collections.singletonList(Double.toString(Math.ceil(7.567))),
                Collections.singletonList(Double.toString(Math.ceil(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testFloor() {
        String sqlQuery = String.format(
                "SELECT FLOOR(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.floor(2.11))),
                Collections.singletonList(Double.toString(Math.floor(7.567))),
                Collections.singletonList(Double.toString(Math.floor(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testRound() {
        String sqlQuery = String.format(
                "SELECT ROUND(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.round(2.11))),
                Collections.singletonList(Double.toString(Math.round(7.567))),
                Collections.singletonList(Double.toString(Math.round(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testRoundTo2DecimalPlaces() {
        String sqlQuery = String.format(
                "SELECT ROUND(value,2) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.round(2.11 * 100) / 100d)),
                Collections.singletonList(Double.toString(Math.round(7.567 * 100) / 100d)),
                Collections.singletonList(Double.toString(Math.round(-1.23 * 100) / 100d))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testMod() {
        String sqlQuery = String.format(
                "SELECT MOD(value,2.11) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Mod(2.11, 2.11))),
                Collections.singletonList(Double.toString(Mod(7.567, 2.11))),
                Collections.singletonList(Double.toString(Mod(-1.23, 2.11)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testPower() {
        String sqlQuery = String.format(
                "SELECT Power(value,2.11) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.pow(2.11, 2.11))),
                Collections.singletonList(Double.toString(Math.pow(7.567, 2.11))),
                Collections.singletonList(Double.toString(Math.pow(-1.23, 2.11)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testExp() {
        String sqlQuery = String.format(
                "SELECT EXP(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.exp(2.11))),
                Collections.singletonList(Double.toString(Math.exp(7.567))),
                Collections.singletonList(Double.toString(Math.exp(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testLn() {
        String sqlQuery = String.format(
                "SELECT LN(value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.log(2.11))),
                Collections.singletonList(Double.toString(Math.log(7.567))),
                Collections.singletonList(Double.toString(Math.log(-1.23)))
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testLog() {
        String sqlQuery = String.format(
                "SELECT LOG(1.5, value) FROM'%s'\nWHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        Double denominator = Math.log(1.5);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.log(2.11) / denominator)),
                Collections.singletonList(Double.toString(Math.log(7.567) / denominator)),
                Collections.singletonList("NaN")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3049
     */
    @Test
    public void testSqrt() {
        String sqlQuery = String.format(
                "SELECT SQRT(value) FROM'%s'\n WHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );


        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Collections.singletonList(Double.toString(Math.sqrt(2.11))),
                Collections.singletonList(Double.toString(Math.sqrt(7.567))),
                Collections.singletonList("NaN")
        );

        assertTableRows(expectedRows, resultTable);
    }




    private Double Mod(Double m, Double n) {
        return m - n * Math.floor(m / n);
    }
}
