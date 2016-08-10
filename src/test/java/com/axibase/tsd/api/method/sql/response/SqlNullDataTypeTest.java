package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Igor Shmagrinskiy
 */
public class SqlNullDataTypeTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-data-type-null-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void initialize() throws IOException {
        Registry.Metric.register(TEST_METRIC1_NAME);
        Registry.Metric.register(TEST_METRIC2_NAME);
        Registry.Entity.register(TEST_METRIC1_NAME);

        List<Series> seriesList = new ArrayList<>();


        seriesList.add(new Series() {{
            setEntity(TEST_ENTITY_NAME);
            setMetric(TEST_METRIC1_NAME);
            addData(new Sample("2016-06-29T08:00:00.000Z", "0.00"));
        }});

        seriesList.add(new Series() {{
            setEntity(TEST_ENTITY_NAME);
            setMetric(TEST_METRIC2_NAME);
            addData(new Sample("2016-06-29T08:00:01.000Z", "0.00"));
        }});

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /*
      Bug#2934 operation on NULL should produce NULL instead of NaN
     */


    /*
      Arithmetical function
     */

    /**
     * issue: #2934
     */
    @Test
    public void testDivisionExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, t1.value/t2.value, 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }

    /**
     * issue: #2934
     */
    @Test
    public void testExpressionNaNDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, t1.value/t2.value, 0.0/0.0 as nancol\n" +
                        "FROM '%s' t1\nOUTER JOIN '%s' t2\nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("NaN", resultTable.getValueAt(5, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testMinusExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, t1.value-t2.value, 0.0/0.0 as nancol\n" +
                        "FROM '%s' t1\nOUTER JOIN '%s' t2\nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }

    /**
     * issue: #2934
     */
    @Test
    public void testPlusExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, t1.value+t2.value, 0.0/0.0 as nancol\n" +
                        "FROM '%s' t1\nOUTER JOIN '%s' t2\nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);

        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testMultiplicationExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, t1.value*t2.value, 0.0/0.0 as nancol\n" +
                        "FROM '%s' t1\nOUTER JOIN '%s' t2\nWHERE entity = '%s'",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("null", resultTable.getValueAt(4, 0));
    }


    /*
      Aggregate function
     */

    /**
     * issue: #2934
     */
    @Test
    public void testCountExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, COUNT(t2.value), 0.0/0.0 as nancol\n" +
                        "FROM '%s' t1\nOUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("1", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testSumExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, SUM(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testAvgExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * issue: #2934
     */
    @Test
    public void testMinExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * issue: #2934
     */
    @Test
    public void testMaxExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, AVG(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'GROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testFirstExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, FIRST(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testCounterExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, FIRST(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }


    /**
     * issue: #2934
     */
    @Test
    public void testDeltaExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, DELTA(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }

    /**
     * issue: #2934
     */
    @Test
    public void testLastExpressionWithNullValueDataType() {
        final String sqlQuery = String.format(
                "SELECT datetime, entity, t1.value, t2.value, LAST(t2.value), 0.0/0.0 as nancol\nFROM '%s' t1\n" +
                        "OUTER JOIN '%s' t2\nWHERE entity = '%s'\nGROUP BY entity,time, t1.value, t2.value",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_ENTITY_NAME
        );
        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        Assert.assertEquals("0.0", resultTable.getValueAt(4, 0));
    }
}
