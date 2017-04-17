package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SqlSelectEntityFieldsTest extends SqlTest {

    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Metric.register(TEST_METRIC);

        Entity entity = new Entity(TEST_ENTITY);
        entity.setLabel(Mocks.LABEL);
        entity.setEnabled(true);
        entity.setInterpolationMode(InterpolationMode.PREVIOUS);
        entity.setTimeZoneID(Mocks.TIMEZONE_ID);
        entity.setTags(Mocks.TAGS);
        EntityMethod.createOrReplaceEntityCheck(entity);

        Series series = new Series();
        series.setEntity(TEST_ENTITY);
        series.setMetric(TEST_METRIC);
        series.addData(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(name = "entityFieldsProvider")
    private Object[][] provideEntityFields() {
        return new Object[][] {
                {"name", TEST_ENTITY},
                {"label", Mocks.LABEL},
                {"timeZone", Mocks.TIMEZONE_ID},
                {"interpolate", "PREVIOUS"},
                {"enabled", "true"},
                {"lastInsertTime", "null"},
                {"tags", "tag=value"}
        };
    }

    /**
     * #4079
     */
    @Test(dataProvider = "entityFieldsProvider")
    public void testQueryEntityFields(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%s FROM '%s' m",
                field,
                TEST_METRIC);

        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in entity field query (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query (%s)", expectedRows, sqlQuery);
    }
}
