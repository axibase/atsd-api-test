package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.prepareApplyTestData;
import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static org.testng.AssertJUnit.assertEquals;


public class ReplaceTest extends SqlTest {
    private static String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        prepareApplyTestData(TEST_METRIC);
    }

    @DataProvider(name = "applyTestProvider")
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_STRING_FUNCTION_ARGS.size();
        Object[][] result = new Object[size * size][1];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i * size + j][0] = String.format("%s, %s, 'a'",
                        POSSIBLE_STRING_FUNCTION_ARGS.get(i), POSSIBLE_STRING_FUNCTION_ARGS.get(j)
                );
            }

        }
        return result;
    }

    /**
     * #2920
     */
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT REPLACE(%s) FROM '%s'",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply REPLACE function to %s", param), queryResponse(sqlQuery));
    }


    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"'VaLue', 'Lu', 'lu'", "Value"},
                {"'VaLue', 'Lue', 'lu'", "Valu"},
                {"'VaLue', 'Lues', 'lu'", "VaLue"},
                {"'VaLue', text, 'lu'", "VaLue"},
                {"text, 'VaLue', 'lu'", "null"},

        };
    }

    /**
     * #2910
     */
    @Test(dataProvider = "selectTestProvider")
    public void testFunctionResult(String param, String expectedValue) {
        String sqlQuery = String.format(
                "SELECT REPLACE(%s) FROM '%s'",
                param, TEST_METRIC
        );
        String assertMessage = String.format("Incorrect result of REPLACE function with param '%s'.%n\tQuery: %s",
                param, sqlQuery
        );
        String actualValue = queryTable(sqlQuery).getValueAt(0, 0);
        assertEquals(assertMessage, expectedValue, actualValue);
    }
}
