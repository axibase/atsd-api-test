package com.axibase.tsd.api.method.sql.metadata;

import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * @author Igor SHmagrinskiy
 */
public class SqlMetaDataTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-metadata";
    private static Series testSeries = new Series(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric");
    private static StringTable resultTable;


    @BeforeClass
    public static void prepareDataSet() {
        final String sqlQuery = "SELECT entity, metric, value, value*100, datetime  FROM 'sql-metadata-metric'\n" +
                "WHERE entity = 'sql-metadata-entity'";
        sendSamplesToSeries(testSeries,
                new Sample("2016-06-29T08:00:00.000Z", "0.05")
        );
        resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);
    }


    /**
     * Following tests is related to bug #2973
     */

    /**
     * issue: #2973
     */
    @Test
    public void testEntityDataType() {
        String entityDataType = resultTable
                .getColumnMetaData(0)
                .getDataType();
        Assert.assertEquals("string", entityDataType);
    }


    /**
     * issue: #2973
     */
    @Test
    public void testEntityPropertyUrl() {
        String entityPropertyUrl = resultTable
                .getColumnMetaData(0)
                .getPropertyUrl();
        Assert.assertEquals("atsd:entity", entityPropertyUrl);
    }


    /**
     * issue: #2973
     */
    @Test
    public void testMetricDataType() {
        String metricDataType = resultTable
                .getColumnMetaData(1)
                .getDataType();
        Assert.assertEquals("string", metricDataType);
    }


    /**
     * issue: #2973
     */
    @Test
    public void testMetricPropertyUrl() {
        String metricPropertyUrl = resultTable
                .getColumnMetaData(1)
                .getPropertyUrl();
        Assert.assertEquals("atsd:metric", metricPropertyUrl);
    }


    /**
     * issue: #2973
     */
    @Test
    public void testValueDataType() {
        String valueDataType = resultTable
                .getColumnMetaData(2)
                .getDataType();
        Assert.assertEquals("float", valueDataType);
    }

    /**
     * issue: #2973
     */
    @Test
    public void testValuePropertyUrl() {
        String valuePropertyUrl = resultTable
                .getColumnMetaData(2)
                .getPropertyUrl();
        Assert.assertEquals("atsd:value", valuePropertyUrl);
    }

    /**
     * issue: #2973
     */
    @Test
    public void testValueWithExpressionDataType() {
        String valueWithExpressionDataType = resultTable
                .getColumnMetaData(3)
                .getDataType();
        Assert.assertEquals("double", valueWithExpressionDataType);
    }


    /**
     * Will fail until bug #2978 will not be fixed
     */

    /**
     * issue: #2973
     */
    @Test
    public void testValueWithExpressionPropertyUrl() {
        String valueWithExpressionPropertyUrl = resultTable
                .getColumnMetaData(3)
                .getPropertyUrl();
        Assert.assertEquals("atsd:value", valueWithExpressionPropertyUrl);
    }

    /**
     * issue: #2973
     */
    @Test
    public void testDateTimeDataType() {
        String dateTimeDataType = resultTable
                .getColumnMetaData(4)
                .getDataType();
        Assert.assertEquals("xsd:dateTimeStamp", dateTimeDataType);
    }

    /**
     * issue: #2973
     */
    @Test
    public void testDateTimePropertyUrl() {
        String dateTimePropertyUrl = resultTable
                .getColumnMetaData(4)
                .getPropertyUrl();
        Assert.assertEquals("atsd:datetime", dateTimePropertyUrl);
    }
}
