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

public class SqlSelectLiteralTest extends SqlTest {
    private static final Series mockSeries = Mocks.series();

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(Collections.singletonList(mockSeries));
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
    public void testSelectLiteralWithMetric(String literal, String result) {
        String sqlQuery = String.format("SELECT %s FROM \"%s\" LIMIT 1", literal, mockSeries.getMetric());
        literalValuesChecks(sqlQuery, literal, result);
    }

    @Issue("3837")
    @Test(dataProvider = "literalAndResultProvider")
    public void testSelectLiteralWithoutMetric(String literal, String result) {
        String sqlQuery = String.format("SELECT %s", literal);
        literalValuesChecks(sqlQuery, literal, result);
    }

    private void literalValuesChecks(String sqlQuery, String literal, String expectedResult) {
        StringTable resultTable = queryTable(sqlQuery);
        List<List<String>> res = resultTable.filterRows(literal);
        assertEquals("Response is empty", 1, res.size());
        assertEquals(String.format("No column with name %s", literal), 1, res.get(0).size());
        assertEquals("Column value is not as expected", expectedResult, res.get(0).get(0));
    }

}
