package com.axibase.tsd.api.method.sql.function.period;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlPeriodSyntaxTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-syntax-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDateInteger("2016-06-19T11:00:00.001Z", 0),
                Sample.ofDateInteger("2016-06-19T11:00:05.001Z", 1),
                Sample.ofDateInteger("2016-06-19T11:00:10.001Z", 2)
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("3058")
    @Test
    public void testPeriodEmptyOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %n" +
                        "WHERE entity = '%s' %nAND datetime >= '2016-06-19T11:00:00.000Z' AND " +
                        "datetime < '2016-06-19T11:00:11.000Z' %nGROUP BY PERIOD(5 SECOND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:10.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodAlignOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(5 SECOND, START_TIME)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "1"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodExtendOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' " +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, EXTEND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:10.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodInterpolateOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3058")
    @Test
    public void testPeriodAlignInterpolateOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, VALUE 0)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:03.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "0.0"),//<-INTERPOLATED BY VALUE 0
                Arrays.asList("2016-06-19T11:00:08.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3058")
    @Test
    public void testPeriodAlignExtendOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, EXTEND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:00.500Z", "1"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:03.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:08.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:10.500Z", "2")//<-EXTEND BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodInterpolateAlignOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, VALUE 0, START_TIME)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:03.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:05.500Z", "0.0"),
                Arrays.asList("2016-06-19T11:00:08.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodInterpolateExtendOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, LINEAR, EXTEND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodExtendInterpolateOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s'" +
                        "AND datetime >= '2016-06-19T11:00:00.500Z' AND datetime < '2016-06-19T11:00:11.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T11:00:02.500Z", "1"),//<-EXTEND BY NEXT
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodAlignInterpolateExtendOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, LINEAR, EXTEND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodAlignExtendInterpolateOptions() {
        String sqlQuery = String.format("SELECT datetime, AVG(value) FROM \"%s\" %n WHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, START_TIME, EXTEND, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodInterpolateAlignExtendOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND,LINEAR, START_TIME, EXTEND)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodInterpolateExtendAlignOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %n" +
                        "WHERE entity = '%s' %nAND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND,LINEAR, EXTEND, START_TIME)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3058")
    @Test
    public void testPeriodExtendInterpolateAlignOptions() {
        String sqlQuery = String.format(
                "SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, LINEAR, START_TIME)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3058")
    @Test
    public void testPeriodExtendAlignInterpolateOptions() {
        String sqlQuery = String.format("SELECT datetime, AVG(value) FROM \"%s\" %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-19T10:59:57.500Z' AND datetime < '2016-06-19T11:00:13.000Z' %n" +
                        "GROUP BY PERIOD(2500 MILLISECOND, EXTEND, START_TIME, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-19T10:59:57.500Z", "0"),//<-EXTENDED BY NEXT
                Arrays.asList("2016-06-19T11:00:00.000Z", "0"),
                Arrays.asList("2016-06-19T11:00:02.500Z", "0.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:05.000Z", "1"),
                Arrays.asList("2016-06-19T11:00:07.500Z", "1.5"),//<-INTERPOLATED BY LINEAR
                Arrays.asList("2016-06-19T11:00:10.000Z", "2"),
                Arrays.asList("2016-06-19T11:00:12.500Z", "2")//<-EXTENDED BY PREVIOUS
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
