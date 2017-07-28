package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlBigintAliasMetaTest extends SqlTest {
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
        assertColumnType("Incorrect type alias for LONG metric value", sqlQuery, 0, "bigint");
    }

    /**
     * #4420
     */
    @Test
    public void testBigintAliasForValueField() {
        String sqlQuery = String.format("SELECT value FROM '%s'", metricName);
        assertBigintAliasForQuery(sqlQuery);
    }

    /**
     * #4420
     */
    @Test
    public void testBigintAliasCountFunction() {
        String sqlQuery = String.format("SELECT count(*) FROM '%s'", metricName);
        assertBigintAliasForQuery(sqlQuery);
    }

    /**
     * #4420
     */
    @Test
    public void testBigintAliasRowNumberFunction() {
        String sqlQuery = String.format(
                "SELECT row_number()\n" +
                        "  FROM '%s'\n" +
                        "  WITH ROW_NUMBER(entity ORDER BY datetime DESC) <= 2\n" +
                        "ORDER BY row_number()",
                metricName
        );
        assertBigintAliasForQuery(sqlQuery);
    }
}
