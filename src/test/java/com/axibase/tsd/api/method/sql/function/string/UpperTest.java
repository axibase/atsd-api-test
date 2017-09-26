package com.axibase.tsd.api.method.sql.function.string;


import com.axibase.tsd.api.method.sql.SqlTest;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.Assert.assertEquals;

public class UpperTest extends SqlTest {
    private static String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        insertSeriesWithMetric(TEST_METRIC);
    }

    @DataProvider(name = "applyTestProvider")
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_STRING_FUNCTION_ARGS.size();
        Object[][] result = new Object[size][1];
        for (int i = 0; i < size; i++) {
            result[i][0] = POSSIBLE_STRING_FUNCTION_ARGS.get(i);
        }
        return result;
    }

    @Issue("2920")
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT LOWER(%s) FROM \"%s\"",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply LOWER function to %s", param), queryResponse(sqlQuery));
    }


    @DataProvider(name = "functionResultProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"VaLuE", "VALUE"},
                {"VALUE", "VALUE"},
                {"444\"a3\"A4", "444\"A3\"A4"},
                {"aBc12@", "ABC12@"},
                {"Кириллица", "КИРИЛЛИЦА"}
        };
    }

    @Test(dataProvider = "functionResultProvider")
    public void testFunctionResult(String text, String expectedValue) throws Exception {
        String sqlQuery = String.format(
                "SELECT UPPER('%s') FROM \"%s\"",
                text, TEST_METRIC
        );
        String actualValue = queryTable(sqlQuery).getValueAt(0, 0);
        String assertMessage = String.format("Incorrect result of upper function with.%n\tQuery: %s%n",
                sqlQuery
        );
        assertEquals(actualValue, expectedValue, assertMessage);
    }

    @Issue("4233")
    @Test
    public void testUpperWithDate() {
        String sqlQuery = "SELECT UPPER('1970-01-01T00:00:00.00')";

        String[][] expectedRows = new String[][] {
                {"1970-01-01T00:00:00.00"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
