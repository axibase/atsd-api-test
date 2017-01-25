package com.axibase.tsd.api.method.sql.function.string;


import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SubStrTest extends SqlTest {
    private static String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        insertSeriesWithMetric(TEST_METRIC);
    }

    @DataProvider(name = "applyTestProvider")
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_STRING_FUNCTION_ARGS.size();
        Object[][] result = new Object[4 * size][1];
        for (int i = 0; i < size; i++) {
            String arg = POSSIBLE_STRING_FUNCTION_ARGS.get(i);
            result[4 * i][0] = String.format("%s, %d, %d", arg, 0, 1);
            result[4 * i + 1][0] = String.format("%s, %d, %d", arg, 1, 10);
            result[4 * i + 2][0] = String.format("%s, %d, %d", arg, 1, 2);
            result[4 * i + 3][0] = String.format("%s, %d, %d", arg, 0, 0);
        }
        return result;
    }

    /**
     * #2920
     */
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT SUBSTR(%s) FROM '%s'",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply SUBSTR function to %s%n\tQuery: %s", param, sqlQuery), queryResponse(sqlQuery));
    }

    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"'VaLue', 1, 3", "VaL"},
                {"'VaLue', 1, 10", "VaLue"},
                {"'VaLue', 1, 0", "VaLue"},
                {"'aaa', 10, 0", ""},
                {"text, 1, 0", "null"}

        };
    }

    /**
     * #2910
     */
    @Test(dataProvider = "selectTestProvider")
    public void testFunctionResult(String param, String expectedValue) {
        String sqlQuery = String.format(
                "SELECT SUBSTR(%s) FROM '%s'",
                param, TEST_METRIC
        );
        String assertMessage = String.format("Incorrect result of SUBSTR function with param '%s'.%n\tQuery: %s",
                param, sqlQuery
        );
        String actualValue = queryTable(sqlQuery).getValueAt(0, 0);
        assertEquals(assertMessage, expectedValue, actualValue);
    }
}
