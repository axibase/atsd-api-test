package com.axibase.tsd.api.method.sql.period.extend;

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
public class SqlPeriodExtendTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-extend-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity-1", TEST_PREFIX + "metric") {{
            addTag("a", "b");
            addTag("b", "c");
        }};
        sendSamplesToSeries(series,
                new Sample("2016-07-14T15:00:06.001Z", "1"),
                new Sample("2016-07-14T15:00:08.001Z", "2")
        );
        series.setEntity(TEST_PREFIX + "entity-2");
        sendSamplesToSeries(series,
                new Sample("2016-07-14T15:00:06.001Z", "3"));
    }


    /**
     * Following tests related to issue #3066.
     * It tests the EXTEND option in PERIOD function
     *
     * @see <a href="https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/interpolate-extend.md">EXTEND</a>
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionBegin() {
        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity = 'sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:05.000Z' AND datetime <'2016-07-14T15:00:07.000Z'" +
                        "GROUP BY PERIOD(1 SECOND,EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:05.000Z", "1.0"), //<--interpolated by NEXT value
                Arrays.asList("2016-07-14T15:00:06.000Z", "1.0")
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionTrail() {
        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity = 'sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:08.000Z' AND datetime <'2016-07-14T15:00:10.000Z'" +
                        "GROUP BY PERIOD(1 SECOND,EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("2016-07-14T15:00:09.000Z", "2.0") //<--interpolated by PREVIOUS value
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionBeginAndTrail() {
        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity = 'sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:05.000Z' AND datetime <'2016-07-14T15:00:10.000Z'" +
                        "GROUP BY PERIOD(1 SECOND,EXTEND)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:05.000Z", "1.0"), //<--interpolated by NEXT value
                Arrays.asList("2016-07-14T15:00:06.000Z", "1.0"),
                // Missing period
                Arrays.asList("2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("2016-07-14T15:00:09.000Z", "2.0") //<--interpolated by NEXT value

        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionBeginAndTrailWithValueInterpolation() {
        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity = 'sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:05.000Z' AND datetime <'2016-07-14T15:00:10.000Z'" +
                        "GROUP BY PERIOD(1 SECOND,EXTEND, VALUE 0)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:05.000Z", "0.0"), //<--interpolated by VALUE 0
                Arrays.asList("2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("2016-07-14T15:00:07.000Z", "0.0"),//<--value interpolated
                Arrays.asList("2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("2016-07-14T15:00:09.000Z", "0.0") //<--interpolated by VALUE 0

        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionBeginAndTrailWithLinearInterpolation() {
        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity = 'sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:05.000Z' AND datetime <'2016-07-14T15:00:10.000Z'" +
                        "GROUP BY PERIOD(1 SECOND,EXTEND, LINEAR)";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:05.000Z", "1.0"), //<--interpolated by NEXT
                Arrays.asList("2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("2016-07-14T15:00:09.000Z", "2.0") //<--interpolated by PREVIOUS

        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionWithIntervalLinear() {

        String sqlQuery =
                "SELECT datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE entity='sql-period-extend-entity-1'\n" +
                        "AND datetime >= '2016-07-14T15:00:06.000Z' AND datetime < '2016-07-14T15:00:09.000Z'" +
                        "GROUP BY entity, PERIOD(1 SECOND,EXTEND, LINEAR), tags.a, tags.b";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("2016-07-14T15:00:08.000Z", "2.0")

        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionWithMultipleEntityWithoutInterval() {

        String sqlQuery =
                "SELECT entity, datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE tags.a LIKE 'b*'\n" +
                        "GROUP BY entity, PERIOD(1 SECOND,EXTEND, LINEAR)\n" +
                        "ORDER BY datetime";


        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:06.000Z", "3.0"),
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:08.000Z", "2.0")

        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionWithMultipleEntityWithstartDate() {

        String sqlQuery =
                "SELECT entity, datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE tags.a LIKE 'b*' AND datetime >= '2016-07-14T15:00:05.000Z'\n" +
                        "GROUP BY entity, PERIOD(1 SECOND,EXTEND, LINEAR)\n" +
                        "ORDER BY datetime";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:05.000Z", "1.0"),//<--interpolated by NEXT
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:05.000Z", "3.0"),//<--interpolated by NEXT
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:06.000Z", "3.0"),
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:08.000Z", "2.0")

        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */

    @Test
    public void testPeriodExtendOptionWithMultipleEntityWithEndDate() {

        String sqlQuery =
                "SELECT entity, datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE tags.a LIKE 'b*' AND datetime < '2016-07-14T15:00:09.000Z'\n" +
                        "GROUP BY entity, PERIOD(1 SECOND,EXTEND, LINEAR)\n" +
                        "ORDER BY datetime";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:06.000Z", "3.0"),
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:07.000Z", "3.0"),//<--interpolated by PREVIOUS
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:08.000Z", "3.0")//<--interpolated by PREVIOUS

        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3066
     */
    @Test
    public void testPeriodExtendOptionWithMultipleEntityWithStartAndEndDate() {

        String sqlQuery =
                "SELECT entity, datetime, AVG(value) FROM 'sql-period-extend-metric'\n" +
                        "WHERE datetime >= '2016-07-14T15:00:05.000Z' AND datetime < '2016-07-14T15:00:09.000Z'\n" +
                        "GROUP BY entity, PERIOD(1 SECOND,EXTEND, LINEAR)\n" +
                        "ORDER BY datetime";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:05.000Z", "1.0"),//<--interpolated by NEXT
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:05.000Z", "3.0"),//<--interpolated by NEXT
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:06.000Z", "1.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:06.000Z", "3.0"),
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:07.000Z", "1.5"),//<--interpolated by LINEAR
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:07.000Z", "3.0"),//<--interpolated by PREVIOUS
                Arrays.asList("sql-period-extend-entity-1", "2016-07-14T15:00:08.000Z", "2.0"),
                Arrays.asList("sql-period-extend-entity-2", "2016-07-14T15:00:08.000Z", "3.0")//<--interpolated by PREVIOUS

        );

        assertTableRows(expectedRows, resultTable);
    }
}
