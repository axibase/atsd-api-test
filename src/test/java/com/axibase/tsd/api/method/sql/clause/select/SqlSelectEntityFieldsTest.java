package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SqlSelectEntityFieldsTest extends SqlTest {
    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Entity entity = new Entity(TEST_ENTITY, Mocks.TAGS);
        entity.setLabel(Mocks.LABEL);
        entity.setEnabled(true);
        entity.setInterpolationMode(InterpolationMode.PREVIOUS);
        entity.setTimeZoneID(Mocks.TIMEZONE_ID);

        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        series.addSamples(Mocks.SAMPLE);

        EntityMethod.createOrReplaceEntityCheck(entity);
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
     * #4117
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

    /**
     * #4117
     */
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInWhere(String field, String value) {
        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            String sqlQuery = String.format(
                    "SELECT m.entity.%1$s FROM '%2$s' m WHERE m.entity.%1$s IS NOT NULL",
                    field,
                    TEST_METRIC,
                    value);

            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in entity field query with WHERE (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM '%2$s' m WHERE m.entity.%1$s = '%3$s'",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with WHERE (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInGroupBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM '%2$s' m GROUP BY m.entity.%1$s",
                field,
                TEST_METRIC);

        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in entity field query with GROUP BY (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with GROUP BY (%s)", expectedRows, sqlQuery);
    }

    /**
     * #4117
     */
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInOrderBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM '%2$s' m ORDER BY m.entity.%1$s",
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
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInHaving(String field, String value) {
        // cannot predefine last insert time value - just check for existence
        if (field.equals("lastInsertTime")) {
            String sqlQuery = String.format(
                    "SELECT m.entity.%1$s FROM '%2$s' m GROUP BY m.entity.%1$s HAVING m.entity.%1$s IS NOT NULL",
                    field,
                    TEST_METRIC);

            StringTable resultTable = queryTable(sqlQuery);
            assertEquals(String.format("Error in entity field query with HAVING (%s)", field), resultTable.getRows().size(), 1);
            return;
        }

        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM '%2$s' m GROUP BY m.entity.%1$s HAVING m.entity.%1$s = '%3$s'",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with HAVING (%s)", expectedRows, sqlQuery);
    }
}
