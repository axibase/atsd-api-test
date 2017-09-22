package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMetaMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.TableMetaData;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

public class SqlBigintAliasMetaTest {
    private static String metricName;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series testSeries = Mocks.series();
        metricName = testSeries.getMetric();

        Metric testMetric = new Metric(metricName);
        testMetric.setDataType(DataType.LONG);

        MetricMethod.createOrReplaceMetricCheck(testMetric);
        SeriesMethod.insertSeriesCheck(testSeries);
    }

    private void assertBigintAliasForQuery(String sqlQuery) {
        TableMetaData metaFromSql = SqlMethod.queryTable(sqlQuery).getTableMetaData();
        TableMetaData metaFromSqlMeta = SqlMetaMethod.queryMetaData(sqlQuery);

        assertColumnType("Incorrect type alias for LONG metric value [/api/sql]",
                metaFromSql, 0, "bigint");
        assertColumnType("Incorrect type alias for LONG metric value [/api/sql/meta]",
                metaFromSqlMeta, 0, "bigint");
    }

    private void assertColumnType(String assertMessage, TableMetaData tableMeta,
                                  int columnIndex, String expectedDataType) {
        String actualDataType = tableMeta.asList().get(columnIndex).getDataType();

        assertEquals(assertMessage + ": Error type is different form expected on column " + columnIndex,
                expectedDataType, actualDataType);
    }

    @Issue("4426")
    @Test(
            description = "Check that LONG value has bigint type alias"
    )
    public void testBigintAliasForValueField() {
        String sqlQuery = String.format("SELECT value FROM \"%s\"", metricName);
        assertBigintAliasForQuery(sqlQuery);

    }

    @Issue("4426")
    @Test(
            description = "Check function application to LONG values has bigint type alias"
    )
    public void testBigintAliasCountFunction() {
        String sqlQuery = String.format("SELECT count(*) FROM \"%s\"", metricName);
        assertBigintAliasForQuery(sqlQuery);
    }

    @Issue("4426")
    @Test(
            description = "Check that result for some other function has bigint type alias"
    )
    public void testBigintAliasRowNumberFunction() {
        String sqlQuery = String.format(
                "SELECT row_number()\n" +
                        "  FROM \"%s\"\n" +
                        "  WITH ROW_NUMBER(entity ORDER BY datetime DESC) < 2\n" +
                        "ORDER BY row_number()",
                metricName
        );
        assertBigintAliasForQuery(sqlQuery);
    }
}
