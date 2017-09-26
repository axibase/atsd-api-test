package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.Assert.assertEquals;


public class LowerTest extends SqlTest {

    private static String TEST_METRIC;

    private static void generateNames() {
        TEST_METRIC = metric();
    }

    @BeforeClass
    public void prepareData() throws Exception {
        generateNames();
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

    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"VaLuE", "value"},
                {"VALUE", "value"},
                {"444\"a3\"A4", "444\"a3\"a4"},
                {"aBc12@", "abc12@"},
                {"Кириллица", "кириллица"}
        };
    }

    @Test(dataProvider = "selectTestProvider")
    public void testFunctionResult(String text, String expectedValue) throws Exception {
        Series series = Mocks.series();
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        String sqlQuery = String.format(
                "SELECT LOWER('%s') FROM \"%s\"",
                text, series.getMetric()
        );

        String actualValue = queryTable(sqlQuery).getValueAt(0, 0);
        String assertMessage = String.format("Incorrect result of lower function with.%n\tQuery: %s",
                sqlQuery
        );
        assertEquals(actualValue, expectedValue, assertMessage);
    }

    @Issue("4233")
    @Test
    public void testLowerWithDate() {
        String sqlQuery = "SELECT LOWER('1970-01-01T00:00:00.00')";

        String[][] expectedRows = new String[][] {
                {"1970-01-01t00:00:00.00"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
