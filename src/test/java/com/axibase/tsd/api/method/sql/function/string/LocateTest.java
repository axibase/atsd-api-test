package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;


public class LocateTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        insertSeriesWithMetric(TEST_METRIC1_NAME);

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "tag1", "Word word WORD worD");
        series.addSamples(new Sample("2016-06-03T09:20:00.000Z", 1));

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @DataProvider(name = "applyTestProvider")
    public Object[][] provideApplyTestsData() {
        Integer size = POSSIBLE_STRING_FUNCTION_ARGS.size();
        Object[][] result = new Object[size * size][1];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                result[i * size + j][0] = String.format("%s, %s",
                        POSSIBLE_STRING_FUNCTION_ARGS.get(i), POSSIBLE_STRING_FUNCTION_ARGS.get(j)
                );
            }

        }
        return result;
    }

    @DataProvider(name = "applyFunctionalTestProvider")
    public Object[][] provideTestsDataForLocateFunctionalTest() {
        return new Object[][]{
                {
                        "Word",
                        "1"
                },
                {
                        "word",
                        "6"
                },
                {
                        "WORD",
                        "11"
                },
                {
                        "worD",
                        "16"
                },
                {
                        "WorD",
                        "0"
                }
        };
    }

    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"'VaLuE', 'Lu'", "0"},
                {"'Lu', 'VaLuE'", "3"},
                {"'Lu', 'VaLuE', 4", "0"},
                {"'Lu', 'VaLuELu', 4", "6"}
        };
    }

    /**
     * #2910
     */
    @Test(dataProvider = "selectTestProvider")
    public void testFunctionResult(String param, String expectedValue) {
        String sqlQuery = String.format(
                "SELECT LOCATE(%s) FROM '%s'",
                param, TEST_METRIC1_NAME
        );
        String assertMessage = String.format("Incorrect result of LOCATE function with param '%s'.%n\tQuery: %s",
                param, sqlQuery
        );
        String actualValue = queryTable(sqlQuery).getValueAt(0, 0);
        assertEquals(assertMessage, expectedValue, actualValue);
    }


    /**
     * #2920
     */
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT LOCATE(%s) FROM '%s'",
                param, TEST_METRIC1_NAME
        );
        assertOkRequest(String.format("Can't apply LOCATE function to %s", param), queryResponse(sqlQuery));
    }

    /**
     * #3749
     */
    @Test(dataProvider = "applyFunctionalTestProvider")
    public void testLocateInSelect(String word, String position) {
        String sqlQuery = String.format(
                "SELECT LOCATE(\"%s\", tags.tag1) FROM '%s' t1",
                word,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {position}
        };

        assertSqlQueryRows("Locate in SELECT gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3749
     */
    @Test(dataProvider = "applyFunctionalTestProvider")
    public void testLocateInWhere(String word, String position) {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' t1 WHERE LOCATE(\"%s\", tags.tag1) = %s",
                TEST_METRIC2_NAME,
                word,
                position
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows("Locate in WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3749
     */
    @Test(dataProvider = "applyFunctionalTestProvider")
    public void testLocateInHaving(String word, String position) {
        String sqlQuery = String.format(
                "SELECT tags.tag1, count(value) FROM '%s' t1 " +
                        "GROUP BY tags.tag1 " +
                        "HAVING count(LOCATE(\"%s\", tags.tag1)) > 0",
                TEST_METRIC2_NAME,
                word
        );

        String[][] expectedRows = {
                {"Word word WORD worD", "1"}
        };

        assertSqlQueryRows("Locate in HAVING gives wrong result", expectedRows, sqlQuery);
    }
}
