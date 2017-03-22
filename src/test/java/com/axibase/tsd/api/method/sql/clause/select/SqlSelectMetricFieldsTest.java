package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlSelectMetricFieldsTest extends SqlTest {

    private static final Series TEST_SERIES = Mocks.series();

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(TEST_SERIES);
    }

    /**
     * #3882
     */
    @Test
    public void testQueryMetricFields() {
        String sqlQuery = String.format(
                "SELECT\n" +
                        "m.metric.label,\n" +
                        "m.metric.timeZone,\n" +
                        "m.metric.interpolate,\n" +
                        "m.metric.description,\n" +
                        "m.metric.dataType,\n" +
                        "m.metric.timePrecision,\n" +
                        "m.metric.enabled,\n" +
                        "m.metric.persistent,\n" +
                        "m.metric.filter,\n" +
                        "m.metric.lastInsertTime,\n" +
                        "m.metric.retentionIntervalDays,\n" +
                        "m.metric.versioning,\n" +
                        "m.metric.minValue,\n" +
                        "m.metric.maxValue,\n" +
                        "m.metric.invalidValueAction\n" +
                 "FROM '%1s' m",
                TEST_SERIES.getMetric()
        );

        String[][] expectedRows = {
                {
                    "null", "null", "LINEAR", "null", "FLOAT", "MILLISECONDS",
                    "true", "true", "null", "null", "0", "false", "null", "null", "NONE"}
        };

        assertSqlQueryRows("JOIN with Entity filter gives wrong result", expectedRows, sqlQuery);
    }
}
