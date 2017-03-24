package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

public class SqlSelectMetricFieldsTest extends SqlTest {

    private static final Series TEST_SERIES = Mocks.series();

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(TEST_SERIES);
    }

    @DataProvider(name = "metricFieldsProvider")
    private Object[][] provideMetricFields() {
        return new Object[][] {
                {"label",                   "null"},
                {"timeZone",                "null"},
                {"interpolate",             "LINEAR"},
                {"description",             "null"},
                {"dataType",                "FLOAT"},
                {"timePrecision",           "MILLISECONDS"},
                {"enabled",                 "true"},
                {"persistent",              "true"},
                {"filter",                  "null"},
                {"lastInsertTime",          "null"},
                {"retentionIntervalDays",   "0"},
                {"versioning",              "false"},
                {"minValue",                "null"},
                {"maxValue",                "null"},
                {"invalidValueAction",      "NONE"},
                {"counter",                 "false"}
        };
    }

    /**
     * #3882
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testQueryMetricFields(String field, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT m.metric.%s FROM '%s' m",
                field,
                TEST_SERIES.getMetric());

        String errorMessage = String.format("Error in metric field query (%s)", field);

        assertSqlQueryRows(
                errorMessage,
                new String[][] {{expectedResult}},
                sqlQuery);
    }
}
