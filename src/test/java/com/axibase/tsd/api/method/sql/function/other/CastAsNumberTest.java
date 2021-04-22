package com.axibase.tsd.api.method.sql.function.other;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CastAsNumberTest extends SqlTest {

    @Test(dataProvider = "testData")
    public void test(String column, String expected) {
        String sql = "select " + column;
        String[][] expectedResult = new String[][]{
                {expected}
        };
        assertSqlQueryRows("Unexpected result for: " + column, expectedResult, sql);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {"cast('2' as number)", "2"},
                {"cast('2.123' as number)", "2.123"},
                {"cast('-2.123' as number)", "-2.123"},
                {"cast(2 as number)", "2"},
                {"cast(2.123 as number)", "2.123"},
                {"cast(-2.123 as number)", "-2.123"},
                {"cast('2' as integer)", "2"},
                {"cast('2.123' as integer)", "2"},
                {"cast('-2.123' as integer)", "-2"},
                {"cast(2 as integer)", "2"},
                {"cast(2.123 as integer)", "2"},
                {"cast(-2.123 as integer)", "-2"},
                {"cast('abc' as integer)", "NaN"},
                {"cast('abc' as number)", "NaN"},
        };
    }
}