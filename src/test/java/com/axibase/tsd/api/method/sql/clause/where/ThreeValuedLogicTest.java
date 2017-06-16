package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.*;
import static org.testng.AssertJUnit.assertEquals;

public class ThreeValuedLogicTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeTest
    private static void prepareData() throws Exception {
        Series series = new Series(entity(), METRIC_NAME);
        series.addSamples(SAMPLE);

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(name = "logicTableTestProvider")
    public Object[][] provideTestsData() {
        return new Object[][]{
                {"NOT (1 = 1)", "false"},
                {"NOT (1 != 1)", "true"},
                {"NOT (NaN = NaN)", "null"},
                {"(1 = 1) AND (2 = 2)", "true"},
                {"(1 = 1) AND (2 != 2)", "false"},
                {"(1 != 1) AND (2 != 2)", "false"},
                {"(1 = 1) AND (NaN = NaN)", "null"},
                {"(1 != 1) AND (NaN = NaN)", "false"},
                {"(NaN = NaN) AND (NaN = NaN)", "null"},
                {"(1 = 1) OR (2 = 2)", "true"},
                {"(1 = 1) OR (2 != 2)", "true"},
                {"(1 != 1) OR (2 != 2)", "false"},
                {"(1 = 1) OR (NaN = NaN)", "true"},
                {"(1 != 1) OR (NaN = NaN)", "null"},
                {"(NaN = NaN) OR (NaN = NaN)", "null"}
        };
    }

    /**
     * #4286
     */
    @Test(dataProvider = "logicTableTestProvider")
    public void selectClauseLogicTableTest(String param, String expectedValue) {
        String query = String.format(
                "SELECT %s",
                param
        );
        String actualValue = queryTable(query).getValueAt(0, 0);
        String assertMessage = String.format("Incorrect result of %s function in SELECT clause", param);

        assertEquals(assertMessage, expectedValue, actualValue);
    }

    /**
     * #4286
     */
    @Test(dataProvider = "logicTableTestProvider")
    public void whereClauseLogicTableTest(String param, String expectedValue) {
        String query = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE %s",
                METRIC_NAME, param
        );

        String actualValue;
        try {
            actualValue = queryTable(query).getValueAt(0, 0);
        } catch (IndexOutOfBoundsException e) {
            actualValue = "";
        }

        String assertMessage = String.format("Incorrect result of %s function in WHERE clause", param);
        expectedValue = expectedValue.equals("true") ? DECIMAL_VALUE.toString() : "";

        assertEquals(assertMessage, expectedValue, actualValue);
    }
}
