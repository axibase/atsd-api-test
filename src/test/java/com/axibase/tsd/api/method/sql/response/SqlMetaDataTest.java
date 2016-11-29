package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.AssertJUnit.assertEquals;


public class SqlMetaDataTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-metadata-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static Series testSeries = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
    private static StringTable resultTable;


    @BeforeClass
    public static void prepareDataSet() throws Exception {
        final String sqlQuery = String.format(
                "SELECT entity, metric, value, value*100, datetime  FROM '%s' %n" +
                        "WHERE entity = '%s'",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );
        testSeries.addData(
                new Sample("2016-06-29T08:00:00.000Z", "0.05")
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(testSeries));
        resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);
    }


    /**
     * #2973
     */
    @Test
    public void testEntityDataType() {
        String entityDataType = resultTable
                .getColumnMetaData(0)
                .getDataType();
        assertEquals("string", entityDataType);
    }


    /**
     * #2973
     */
    @Test
    public void testEntityPropertyUrl() {
        String entityPropertyUrl = resultTable
                .getColumnMetaData(0)
                .getPropertyUrl();
        assertEquals("atsd:entity", entityPropertyUrl);
    }


    /**
     * #2973
     */
    @Test
    public void testMetricDataType() {
        String metricDataType = resultTable
                .getColumnMetaData(1)
                .getDataType();
        assertEquals("string", metricDataType);
    }


    /**
     * #2973
     */
    @Test
    public void testMetricPropertyUrl() {
        String metricPropertyUrl = resultTable
                .getColumnMetaData(1)
                .getPropertyUrl();
        assertEquals("atsd:metric", metricPropertyUrl);
    }


    /**
     * #2973
     */
    @Test
    public void testValueDataType() {
        String valueDataType = resultTable
                .getColumnMetaData(2)
                .getDataType();
        assertEquals("float", valueDataType);
    }

    /**
     * #2973
     */
    @Test
    public void testValuePropertyUrl() {
        String valuePropertyUrl = resultTable
                .getColumnMetaData(2)
                .getPropertyUrl();
        assertEquals("atsd:value", valuePropertyUrl);
    }

    /**
     * #2973
     */
    @Test
    public void testValueWithExpressionDataType() {
        String valueWithExpressionDataType = resultTable
                .getColumnMetaData(3)
                .getDataType();
        assertEquals("double", valueWithExpressionDataType);
    }


    /**
     * Will fail until bug #2978 will not be fixed
     */

    /**
     * #2973
     */
    @Test
    public void testValueWithExpressionPropertyUrl() {
        String valueWithExpressionPropertyUrl = resultTable
                .getColumnMetaData(3)
                .getPropertyUrl();
        assertEquals("atsd:value", valueWithExpressionPropertyUrl);
    }

    /**
     * #2973
     */
    @Test
    public void testDateTimeDataType() {
        String dateTimeDataType = resultTable
                .getColumnMetaData(4)
                .getDataType();
        assertEquals("xsd:dateTimeStamp", dateTimeDataType);
    }

    /**
     * #2973
     */
    @Test
    public void testDateTimePropertyUrl() {
        String dateTimePropertyUrl = resultTable
                .getColumnMetaData(4)
                .getPropertyUrl();
        assertEquals("atsd:datetime", dateTimePropertyUrl);
    }
}
