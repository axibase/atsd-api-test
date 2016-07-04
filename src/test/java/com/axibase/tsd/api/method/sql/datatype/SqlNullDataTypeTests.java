package com.axibase.tsd.api.method.sql.datatype;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlNullDataTypeTests extends SqlMethod {
    private static final String TEST_PREFIX = "sql-data-type-null";

    private static void addTestSamplesToSeries(Series series, Sample... samples) {
        boolean isSuccessInsert;
        series.setData(Arrays.asList(samples));
        try {
            isSuccessInsert = SeriesMethod.insertSeries(series, 1000);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
        if (!isSuccessInsert) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
    }


    @BeforeClass
    public static void initialize() {
        Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric-1");

        addTestSamplesToSeries(testSeries,
                new Sample("2016-06-29T08:00:00.000Z", "0.00")
        );
        testSeries.setMetric(TEST_PREFIX + "-metric-2");
        addTestSamplesToSeries(testSeries,
                new Sample("2016-06-30T08:05:00.000Z", "0.00")
        );
    }

    /**
     * Bug#2934 operation on NULL should produce NULL instead of NaN
     */


    /**
     * Arithmetical function
     */

    /**
     * redmine: #2934
     */
    @Test
    public void testDivisionExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, t1.value/t2.value, 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }

    /**
     * redmine: #2934
     */
    @Test
    public void testExpressionNaNDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, t1.value/t2.value, 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("NaN", resultTable.getValueAt(5, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testMinusExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, t1.value-t2.value, 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }

    /**
     * redmine: #2934
     */
    @Test
    public void testPlusExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, t1.value+t2.value, 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testMultiplicationExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, t1.value*t2.value, 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }


    /**
     * Aggregate function
     */

    /**
     * redmine: #2934
     */
    @Test
    public void testCountExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, COUNT(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("1", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testSumExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, SUM(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testAvgExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * redmine: #2934
     */
    @Test
    public void testMinExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * redmine: #2934
     */
    @Test
    public void testMaxExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testFirstExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, FIRST(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testCounterExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, FIRST(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * redmine: #2934
     */
    @Test
    public void testDeltaExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, DELTA(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * redmine: #2934
     */
    @Test
    public void testLastExpressionWithNullValueDataType() {
        final String sqlQuery =
                "SELECT datetime, entity, t1.value, t2.value, LAST(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM 'sql-data-type-null-metric-1' t1\n" +
                        "OUTER JOIN 'sql-data-type-null-metric-2' t2\n" +
                        "WHERE entity = 'sql-data-type-null-entity'";
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }
}
