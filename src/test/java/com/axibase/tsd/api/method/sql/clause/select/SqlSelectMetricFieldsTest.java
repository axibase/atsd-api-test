package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SqlSelectMetricFieldsTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    //@BeforeClass
    public static void prepareData() throws Exception {
        Metric metric = new Metric(TEST_METRIC, Mocks.TAGS);
        metric.setName(TEST_METRIC);
        metric.setLabel(Mocks.LABEL);
        metric.setTimeZoneID(Mocks.TIMEZONE_ID);
        metric.setInterpolate(InterpolationMode.PREVIOUS);
        metric.setDescription(Mocks.DESCRIPTION);
        metric.setDataType(DataType.INTEGER);
        metric.setTimePrecision("SECONDS");
        metric.setEnabled(true);
        metric.setPersistent(true);
        metric.setFilter("name = '*'");
        metric.setVersioned(false);
        metric.setAdditionalProperty("minValue", 0);
        metric.setAdditionalProperty("maxValue", 9);
        metric.setInvalidAction("NONE");
        metric.setCounter(false);
        metric.setAdditionalProperty("units", "kg");

        String entity = entity();
        Series series = new Series(entity, TEST_METRIC);
        series.addSamples(Mocks.SAMPLE);

        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(name = "metricFieldsProvider")
    private Object[][] provideMetricFields() {
        return new Object[][] {
                {"name", TEST_METRIC},
                {"label", Mocks.LABEL},
                {"timeZone", Mocks.TIMEZONE_ID},
                {"interpolate", "PREVIOUS"},
                {"description", Mocks.DESCRIPTION},
                {"dataType", "INTEGER"},
                {"timePrecision", "SECONDS"},
                {"enabled", "true"},
                {"persistent", "true"},
                {"filter", "name = '*'"},
                {"lastInsertTime", "null"},
                {"retentionIntervalDays", "0"},
                {"versioning", "false"},
                {"minValue", "0"},
                {"maxValue", "9"},
                {"invalidValueAction", "NONE"},
                {"counter", "false"},
                {"units", "kg"},
                {"tags", "tag=value"}
        };
    }

    /**
     * #4117
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testQueryMetricFields(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.metric.%s FROM '%s' m",
                field,
                TEST_METRIC);

        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in metric field query (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in metric field query (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testMetricFieldsInWhere(String field, String value) {
        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            String sqlQuery = String.format(
                    "SELECT m.metric.%1$s FROM '%2$s' m WHERE m.metric.%1$s IS NOT NULL",
                    field,
                    TEST_METRIC);

            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in metric field query with WHERE (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String sqlQuery = String.format(
                "SELECT m.metric.%1$s FROM '%2$s' m WHERE m.metric.%1$s = \"%3$s\"",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in metric field query with WHERE (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testMetricFieldsInGroupBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.metric.%1$s FROM '%2$s' m GROUP BY m.metric.%1$s",
                field,
                TEST_METRIC);

        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in metric field query with GROUP BY (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in metric field query with GROUP BY (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testMetricFieldsInOrderBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.metric.%1$s FROM '%2$s' m ORDER BY m.metric.%1$s",
                field,
                TEST_METRIC);

        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in entity field query with ORDER BY (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with GROUP BY (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testMetricFieldsInHaving(String field, String value) {
        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            String sqlQuery = String.format(
                    "SELECT m.metric.%1$s FROM '%2$s' m GROUP BY m.metric.%1$s HAVING m.metric.%1$s IS NOT NULL",
                    field,
                    TEST_METRIC);

            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in metric field query with HAVING (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String sqlQuery = String.format(
                "SELECT m.metric.%1$s FROM '%2$s' m GROUP BY m.metric.%1$s HAVING m.metric.%1$s = \"%3$s\"",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in metric field query with HAVING (%s)", expectedRows, sqlQuery);
    }
}
