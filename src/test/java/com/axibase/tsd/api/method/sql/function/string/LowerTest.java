package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.sql.operator.StringOperators;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.util.*;

import static com.axibase.tsd.api.Util.TestNames.generateMetricName;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.*;


public class LowerTest extends SqlTest {

    private static String TEST_METRIC;

    private static void generateNames() {
        TEST_METRIC = generateMetricName();
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
        assertOkRequest(String.format("Can't apply LOWER function to %s", param), executeQuery(sqlQuery));
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

    /**
     * #2920
     */
    @Test(dataProvider = "selectTestProvider")
    public void testSelect(String value, String expectedFormattedEntityName) throws Exception {
        String metricName = generateMetricName();
        Map<String, String> tags = new HashMap<>();
        tags.put("a", value);

        Entity entity = new Entity(value);
        entity.setTags(tags);
        EntityMethod.createOrReplaceEntity(entity);


        Series series = new Series();
        series.setEntity(value);
        series.setMetric(metricName);
        series.setTags(tags);
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeries(Collections.singletonList(series));
        Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);

        String sqlQuery = String.format("SELECT LOWER(entity), LOWER(tags.a), LOWER(entity.tags.a) FROM '%s'",
                metricName
        );

        List<String> expectedRow = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            expectedRow.add(expectedFormattedEntityName);
        }

        assertSqlQueryRows(sqlQuery, Collections.singletonList(expectedRow));
    }

    @DataProvider(name = "stringOperatorsTestProvider", parallel = true)
    public Object[][] provideStringBinaryOperatorTestsData() {
        List<List<String>> resultList = new ArrayList<>();
        for (StringOperators.Binary operator : StringOperators.Binary.values()) {
            for (String left : POSSIBLE_FUNCTION_ARGS) {
                for (String right : POSSIBLE_FUNCTION_ARGS) {
                    resultList.add(Arrays.asList(left, right, operator.toString()));
                }
            }
        }
        Object[][] result = new Object[resultList.size()][];
        int i = 0;
        for (List<String> element : resultList) {
            result[i] = new Object[element.size()];
            element.toArray(result[i]);
            i++;
        }
        return result;
    }

    /**
     * #2920
     * Yet should fall
     */
    @Test(dataProvider = "stringOperatorsTestProvider", enabled = false)
    public void testWhereStringOperatorsApplyToVariable(String left, String right, String operator) throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, value%nFROM '%s' WHERE LOWER(%s) %s %s ",
                TEST_METRIC, left, operator, right
        );
        String errorMessage = String.format(
                "Failed to apply string operator %s to result of LOWER function.%n\tQuery: %s",
                operator, sqlQuery
        );
        assertOkRequest(errorMessage, executeQuery(sqlQuery));
    }
}
