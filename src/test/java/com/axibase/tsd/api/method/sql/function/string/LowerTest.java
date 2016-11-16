package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;

import static com.axibase.tsd.api.util.Util.TestNames.metric;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.prepareApplyTestData;


public class LowerTest extends SqlTest {

    private static String TEST_METRIC;

    private static void generateNames() {
        TEST_METRIC = metric();
    }

    @BeforeClass
    public void prepareData() throws FileNotFoundException, InterruptedException {
        generateNames();
        prepareApplyTestData(TEST_METRIC);
    }

    @DataProvider(name = "applyTestProvider", parallel = true)
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_FUNCTION_ARGS.size();
        Object[][] result = new Object[size][1];
        for (int i = 0; i < size; i++) {
            result[i][0] = POSSIBLE_FUNCTION_ARGS.get(i);
        }
        return result;
    }

    /**
     * #2920
     */
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT LOWER(%s) FROM '%s'",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply LOWER function to %s", param), queryResponse(sqlQuery));
    }

    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"VaLuE", "value"},
                {"VALUE", "value"},
                {"444'a3'A4", "444'a3'a4"},
                {"aBc12@", "abc12@"},
                {"Кириллица", "кириллица"}
        };
    }
}
