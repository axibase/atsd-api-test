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
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static org.testng.AssertJUnit.assertEquals;

public class SqlSelectMetricFieldsTest extends SqlTest {

    private static final String TEST_METRIC = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Metric.register(TEST_METRIC);

        Metric metric = new Metric();
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
        metric.setTags(Mocks.TAGS);

        MetricMethod.createOrReplaceMetricCheck(metric);

        String entity = entity();
        Registry.Entity.register(entity);
        Series series = new Series();
        series.setEntity(entity);
        series.setMetric(TEST_METRIC);
        series.addData(Mocks.SAMPLE);

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
     * #3882, #3658, #4079
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
}
