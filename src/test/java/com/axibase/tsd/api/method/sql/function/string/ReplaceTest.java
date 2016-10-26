package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.sql.SqlTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;

import static com.axibase.tsd.api.Util.TestNames.generateMetricName;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.prepareApplyTestData;


public class ReplaceTest extends SqlTest {
    private static String TEST_METRIC = generateMetricName();

    @BeforeClass
    public void prepareData() throws FileNotFoundException, InterruptedException {
        prepareApplyTestData(TEST_METRIC);
    }

    @DataProvider(name = "applyTestProvider", parallel = true)
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_FUNCTION_ARGS.size();
        Object[][] result = new Object[size * size][1];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i * size + j][0] = String.format("%s, %s, 'a'",
                        POSSIBLE_FUNCTION_ARGS.get(i), POSSIBLE_FUNCTION_ARGS.get(j)
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
        assertOkRequest(String.format("Can't apply REPLACE function to %s", param), executeQuery(sqlQuery));
    }
}
