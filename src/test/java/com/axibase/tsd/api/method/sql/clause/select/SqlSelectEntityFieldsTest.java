package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

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
                {"lastInsertTime", "" + Util.getUnixTime(Mocks.ISO_TIME)},
                {"tags", "tag=value"}
        };
    }

    @Issue("4117")
    @Test(dataProvider = "entityFieldsProvider")
    public void testQueryEntityFields(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%s FROM \"%s\" m",
                field,
                TEST_METRIC);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query (%s)", expectedRows, sqlQuery);
    }

    @Issue("4117")
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInWhere(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM \"%2$s\" m WHERE m.entity.%1$s = '%3$s'",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with WHERE (%s)", expectedRows, sqlQuery);
    }

    @Issue("4117")
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInGroupBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM \"%2$s\" m GROUP BY m.entity.%1$s",
                field,
                TEST_METRIC);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with GROUP BY (%s)", expectedRows, sqlQuery);
    }

    @Issue("4117")
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInOrderBy(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM \"%2$s\" m ORDER BY m.entity.%1$s",
                field,
                TEST_METRIC);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with GROUP BY (%s)", expectedRows, sqlQuery);
    }

    @Issue("4117")
    @Test(dataProvider = "entityFieldsProvider")
    public void testEntityFieldsInHaving(String field, String value) {
        String sqlQuery = String.format(
                "SELECT m.entity.%1$s FROM \"%2$s\" m GROUP BY m.entity.%1$s HAVING m.entity.%1$s = '%3$s'",
                field,
                TEST_METRIC,
                value);

        String[][] expectedRows = {{value}};

        assertSqlQueryRows("Error in entity field query with HAVING (%s)", expectedRows, sqlQuery);
    }
}
