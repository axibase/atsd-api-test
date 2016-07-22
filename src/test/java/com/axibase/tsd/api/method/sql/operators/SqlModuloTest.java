package com.axibase.tsd.api.method.sql.operators;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import javax.ws.rs.ProcessingException;
import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlModuloTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-modulo-";
    private static double EPS = 10e-4;


    @BeforeClass
    public static void prepareData() {
        Series numeratorSeries = new Series(TEST_PREFIX + "entity-1", TEST_PREFIX + "metric-1");
        numeratorSeries.addTag("a", "b");
        numeratorSeries.addTag("b", "c");
        new Sample(Util.parseDate("2016-06-03T09:23:00.000Z").getTime(), "7");
        sendSamplesToSeries(numeratorSeries,
                new Sample(Util.parseDate("2016-06-03T09:23:00.000Z").getTime(), "7"),
                new Sample(Util.parseDate("2016-06-03T09:24:00.000Z").getTime(), "0"),
                new Sample(Util.parseDate("2016-06-03T09:25:00.000Z").getTime(), "12"),
                new Sample(Util.parseDate("2016-06-03T09:26:00.000Z").getTime(), "10.3"),
                new Sample(Util.parseDate("2016-06-03T09:27:00.000Z").getTime(), "10")
        );
        numeratorSeries.setMetric(TEST_PREFIX + "metric-2");

        Series denominatorSeries = numeratorSeries;
        sendSamplesToSeries(denominatorSeries,
                new Sample(Util.parseDate("2016-06-03T09:23:00.000Z").getTime(), "5"),
                new Sample(Util.parseDate("2016-06-03T09:24:00.000Z").getTime(), "7"),
                new Sample(Util.parseDate("2016-06-03T09:25:00.000Z").getTime(), "-2"),
                new Sample(Util.parseDate("2016-06-03T09:26:00.000Z").getTime(), "-2.1")
        );
    }


    /**
     * Following tests related to issue #2922
     */

    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByPositiveInteger() {
        String sqlQuery =
                "SELECT entity, datetime, m1.value AS 'num', m2.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z' AND entity = 'sql-modulo-entity-1'";
        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("7.0", "5.0", "2.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingZeroByPositiveInteger() {
        String sqlQuery =
                "SELECT entity, datetime, m1.value AS 'num', m2.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:24:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("0.0", "7.0", "0.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }

    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByZeroInteger() {
        String sqlQuery =
                "SELECT entity, datetime, m2.value AS 'num', m1.value AS 'den', m2.value % m1.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:24:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("7.0", "0.0", "NaN")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByNegativeInteger() {
        String sqlQuery =
                "SELECT entity, datetime, m1.value AS 'num', m2.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:25:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("12.0", "-2.0", "0.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");

        assertTableRows(expectedRows, resultRows);
    }

    /**
     * issue #2922
     */
    @Test
    public void testDividingNegativeByPositiveInteger() {
        String sqlQuery =
                "SELECT entity, datetime, m2.value AS 'num', m1.value AS 'den', m2.value % m1.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:25:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("-2.0", "12.0", "-2.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingPositiveByNegativeDecimal() {
        String sqlQuery =
                "SELECT entity, datetime, m1.value AS 'num', m2.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:26:00.000Z' AND entity = 'sql-modulo-entity-1'";

        Double expectedModulo = 1.9;

        Double resultModulo = Double.parseDouble(executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getValueAt(4, 0)
        );

        assertEquals(expectedModulo, resultModulo, EPS);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNegativeByPositiveDecimal() {
        String sqlQuery =
                "SELECT entity, datetime, m2.value AS 'num', m1.value AS 'den', m2.value % m1.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:26:00.000Z' AND entity = 'sql-modulo-entity-1'";

        Double expectedModulo = -2.1;

        Double resultModulo = Double.parseDouble(executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .getValueAt(4, 0)
        );

        assertEquals(expectedModulo, resultModulo, EPS);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNullByNumber() {
        String sqlQuery =
                "SELECT entity, datetime, m1.value AS 'num', m2.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:27:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("10.0", "null", "null")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test
    public void testDividingNumberByNull() {
        String sqlQuery =
                "SELECT entity, datetime, m2.value AS 'num', m1.value AS 'den', m1.value % m2.value AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:27:00.000Z' AND entity = 'sql-modulo-entity-1'";

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("null", "10.0", "null")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows("num", "den", "modulo");
        assertTableRows(expectedRows, resultRows);
    }


    /**
     * issue #2922
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testDividingStringByString() {
        String sqlQuery =
                "SELECT entity, datetime, tags.a , tags.b , tags.a % tags.b AS 'modulo' FROM 'sql-modulo-metric-1' m1\n " +
                        "OUTER JOIN 'sql-modulo-metric-2' m2 " +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z' AND entity = 'sql-modulo-entity-1'";

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

    /**
     * issue #2922
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testDividingNaNByNumber() {
        String sqlQuery =
                "SELECT entity, datetime, value, 0/0 % m1.value AS 'modulo' FROM 'sql-modulo-metric-1'\n " +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z' AND entity = 'sql-modulo-entity-1' ";

        executeQuery(sqlQuery)
                .readEntity(StringTable.class);
    }

}
