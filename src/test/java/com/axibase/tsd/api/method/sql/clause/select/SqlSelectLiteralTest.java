package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class SqlSelectLiteralTest extends SqlTest {

    private static String metricName;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = Mocks.series();
        metricName = series.getMetric();

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @DataProvider(name = "literalAndResultProvider")
    public static Object[][] provideLiteralTestData() {
        return new Object[][]{
                {"123.456", "123.456"},
                {"true", "true"},
                {"false", "false"},
                {"'true'", "true"},
                {"'abc'", "abc"},
                {"'column''s name'", "column's name"},
                {"'entity'", "entity"},
        };
    }

    @Issue("3837")
    @Test(dataProvider = "literalAndResultProvider")
    public void testSelectLiteral(String literal, String result) {
        String sqlQuery = String.format("SELECT %s FROM \"%s\"", literal, metricName);
        StringTable resultTable = queryTable(sqlQuery);

        List<List<String>> res = resultTable.filterRows(literal);

        assertFalse(String.format("No column with name %s", literal), res.get(0).isEmpty());
        assertEquals("Column value is not as expected", res.get(0).get(0), result);
    }

}
