package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SqlSelectMetricFieldsTest extends SqlTest {

    private static final Series TEST_SERIES = Mocks.series();

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(TEST_SERIES);
    }

    @DataProvider(name = "metricFieldsProvider")
    private Object[][] provideMetricFields() {
        return new Object[][] {
                {"name"},
                {"label"},
                {"timeZone"},
                {"interpolate"},
                {"description"},
                {"dataType"},
                {"timePrecision"},
                {"enabled"},
                {"persistent"},
                {"filter"},
                {"lastInsertTime"},
                {"retentionIntervalDays"},
                {"versioning"},
                {"minValue"},
                {"maxValue"},
                {"invalidValueAction"},
                {"counter"},
                {"units"},
                {"tags"}
        };
    }

    /**
     * #3882, #3658
     */
    @Test(dataProvider = "metricFieldsProvider")
    public void testQueryMetricFields(String field) {
        String sqlQuery = String.format(
                "SELECT m.metric.%s FROM '%s' m",
                field,
                TEST_SERIES.getMetric());

        StringTable resultTable = queryTable(sqlQuery);

        // check for row existence
        assertEquals(String.format("Error in metric field query (%s)", field), resultTable.getRows().size(), 1);
    }

    /**
     * #4035
     */
    @Test
    public void testMetricLastInsertTimeNotNull() {
        String sqlQuery = String.format(
                "SELECT date_format(metric.lastInsertTime) FROM '%s'",
                TEST_SERIES.getMetric()
        );

        String[][] expectedRows = {{Mocks.ISO_TIME}};

        assertSqlQueryRows("Metric field 'lastInsertTime' has incorrect value", expectedRows, sqlQuery);
    }
}
