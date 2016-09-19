package com.axibase.tsd.api.method.sql.function.period.interpolation;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;


public class SqlPeriodInterpolationTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-period-interpolation-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    private static final Set<String> DEFAULT_ROW_FILTER = new HashSet<>(Arrays.asList("datetime", "AVG(value)"));

    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            setData(Arrays.asList(
                    new Sample("2016-06-03T09:26:00.000Z", "8.1"),
                    new Sample("2016-06-03T09:36:00.000Z", "6.0"),
                    new Sample("2016-06-03T09:41:00.000Z", "19.0")
            ));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }


    /*
     * #1475 task
     * SQL: period() interpolation
     */


    /**
     *  #1475
     */
    @Test
    public void testNoInterpolation() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:45:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                //<-missing period
                Arrays.asList("2016-06-03T09:35:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testConstantValue0FillTheGaps() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:45:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE, VALUE 0)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                Arrays.asList("2016-06-03T09:30:00.000Z", "0.0"),//<-constant
                Arrays.asList("2016-06-03T09:35:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testNegativeConstantValueFillTheGaps() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:45:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE, VALUE -1)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                Arrays.asList("2016-06-03T09:30:00.000Z", "-1.0"),//<-constant
                Arrays.asList("2016-06-03T09:35:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testPreviousValueFillTheGaps() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:45:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE, PREVIOUS)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                Arrays.asList("2016-06-03T09:30:00.000Z", "8.1"),//<-previous value
                Arrays.asList("2016-06-03T09:35:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testLinearInterpolatedValueFillTheGaps() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:23:00.000Z' AND datetime < '2016-06-03T09:45:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                Arrays.asList("2016-06-03T09:30:00.000Z", "7.05"),//<-interpolated
                Arrays.asList("2016-06-03T09:35:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testLinearInterpolatedValueFillTheMultipleGaps() {
        final String sqlQuery = String.format(
                "SELECT entity, datetime, AVG(value) FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:36:00.000Z' AND datetime < '2016-06-03T09:42:00.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(1 MINUTE, LINEAR)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:36:00.000Z", "6.0"),
                Arrays.asList("2016-06-03T09:37:00.000Z", "8.6"),//<-interpolated
                Arrays.asList("2016-06-03T09:38:00.000Z", "11.2"),//<-interpolated
                Arrays.asList("2016-06-03T09:39:00.000Z", "13.8"),//<-interpolated
                Arrays.asList("2016-06-03T09:40:00.000Z", "16.4"),//<-interpolated
                Arrays.asList("2016-06-03T09:41:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }

    /**
     *  #1475
     */
    @Test
    public void testHavingClauseWithPeriodFunction() {
        final String sqlQuery = String.format("SELECT entity, datetime, AVG(value)FROM '%s' %n" +
                        "WHERE datetime >= '2016-06-03T09:25:00.000Z' AND datetime < '2016-06-03T09:41:30.000Z' %n" +
                        "AND entity = '%s' %nGROUP BY entity,PERIOD(5 MINUTE, VALUE 0) HAVING AVG(value) > 7",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-06-03T09:25:00.000Z", "8.1"),
                Arrays.asList("2016-06-03T09:30:00.000Z", "0.0"),//<-constant
                Arrays.asList("2016-06-03T09:35:00.000Z", "0.0"),//<--instead not suitable value 6.0(>7)
                Arrays.asList("2016-06-03T09:40:00.000Z", "19.0")
        );

        List<List<String>> resultRows = executeQuery(sqlQuery)
                .readEntity(StringTable.class)
                .filterRows(DEFAULT_ROW_FILTER);
        Assert.assertEquals(expectedRows, resultRows);
    }
}
