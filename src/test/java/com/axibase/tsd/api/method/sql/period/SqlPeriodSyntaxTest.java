package com.axibase.tsd.api.method.sql.period;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlPeriodSyntaxTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-syntax-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareDate() {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-06-19T11:00:00.001Z", "0"),
                new Sample("2016-06-19T11:00:05.001Z", "1"),
                new Sample("2016-06-19T11:00:10.001Z", "2")
        );
    }

    /*
    Following tests related to #3057 issue
     */

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodEmptyOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.000Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(5 SECOND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodAlignOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(5 SECOND, START_TIME)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodExtendOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1.0"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodInterpolateOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, LINEAR)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3058
     */
    @Test
    public void testPeriodAlignInterpolateOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, VALUE 0)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:03.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "0.0"),//<-INTERPOLATED BY VALUE 0
                Arrays.asList("2016-06-19T11:00:08.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3058
     */
    @Test
    public void testPeriodAlignExtendOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "1.0"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:03.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:08.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:10.500Z", "2.0")//<-EXTEND BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodInterpolateAlignOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, VALUE 0, START_TIME)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:03.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:08.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodInterpolateExtendOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, LINEAR, EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1.0"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodExtendInterpolateOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, LINEAR)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1.0"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodAlignInterpolateExtendOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, LINEAR, EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodAlignExtendInterpolateOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, EXTEND, LINEAR)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodInterpolateAlignExtendOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND,LINEAR, START_TIME, EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodInterpolateExtendAlignOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND,LINEAR, EXTEND, START_TIME)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3058
     */
    @Test
    public void testPeriodExtendInterpolateAlignOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, LINEAR, START_TIME)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3058
     */
    @Test
    public void testPeriodExtendAlignInterpolateOptions() {
        String sqlQuery = "SELECT datetime, AVG(value) FROM '" + TEST_METRIC_NAME + "'\n" +
                "WHERE entity = '" + TEST_ENTITY_NAME + "'\n" +
                "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z'\n" +
                "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, START_TIME, LINEAR)";

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0.0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1.0"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2.0"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2.0")//<-EXTENDED BY PREVIOUS
        );

        assertTableRows(expectedRows, resultTable);
    }
}
