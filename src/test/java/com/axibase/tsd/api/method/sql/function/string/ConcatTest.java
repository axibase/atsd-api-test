package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Mocks.metric;

public class ConcatTest extends SqlTest {
    private static final String TEST_ENTITY_LEFT = "test-concat";
    private static final String TEST_ENTITY_RIGHT = "-entity";
    private static final int TEST_ENTITY_INDEX = 0;
    private static final String TEST_ENTITY = TEST_ENTITY_LEFT + TEST_ENTITY_RIGHT + TEST_ENTITY_INDEX;

    private static final String TEST_METRIC = metric();

    private static final String TEST_METRIC1 = metric();
    private static final String TEST_METRIC2 = metric();
    private static final String TEST_METRIC3 = metric();

    private static void prepareFunctionalConcatTestData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        Series series = new Series(TEST_ENTITY, TEST_METRIC1);
        series.addSamples(
                Sample.ofDate("2016-06-03T09:19:18.000Z"),
                Sample.ofDateInteger("2016-06-03T09:20:18.000Z", 3),
                Sample.ofDateDecimal("2016-06-03T09:21:18.000Z", new BigDecimal("3.10")),
                Sample.ofDateDecimal("2016-06-03T09:22:18.000Z", new BigDecimal("3.14")),
                Sample.ofDateDecimal("2016-06-03T09:23:18.000Z", new BigDecimal("3.1415"))
        );

        seriesList.add(series);

        series = new Series(TEST_ENTITY, TEST_METRIC2);
        series.addSamples(Sample.ofDateDecimal("2016-06-03T09:23:18.000Z", new BigDecimal("5.555")));
        seriesList.add(series);

        series = new Series(TEST_ENTITY, TEST_METRIC3);
        series.addSamples(Sample.ofDateInteger("2016-06-03T09:23:18.000Z", 5));
        seriesList.add(series);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @BeforeClass
    public void prepareData() throws Exception {
        insertSeriesWithMetric(TEST_METRIC);

        prepareFunctionalConcatTestData();
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

    @Issue("2920")
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT CONCAT(%s) FROM \"%s\"",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply CONCAT function to %s", param), queryResponse(sqlQuery));
    }

    @Issue("3768")
    @Issue("4000")
    @Test
    public void testConcatWordAndNumber() throws Exception {
        String sqlQuery = String.format("SELECT CONCAT('a:', value) FROM \"%s\"",
                TEST_METRIC1
        );

        String[][] expectedRows = {
                {"a:"},
                {"a:3"},
                {"a:3.1"},
                {"a:3.14"},
                {"a:3.14"},
        };

        assertSqlQueryRows("CONCAT word and number without CAST gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3768")
    @Test
    public void testConcatWordAndTwoNumbers() throws Exception {
        String sqlQuery = String.format(
                "SELECT CONCAT('a:', t1.value, ':', t2.value), CONCAT('a:', t1.value, ':', t3.value) " +
                        "FROM \"%s\" t1 JOIN \"%s\" t2 JOIN \"%s\" t3",
                TEST_METRIC1,
                TEST_METRIC2,
                TEST_METRIC3
        );

        String[][] expectedRows = {
                {"a:3.14:5.55", "a:3.14:5"}
        };

        assertSqlQueryRows("CONCAT word and two numbers gives wrong result", expectedRows, sqlQuery);
    }


    @Issue("4017")
    @Test
    public void testConcatInWhere() {
        String sqlQuery = String.format(
                "SELECT entity FROM \"%s\" WHERE entity = CONCAT('%s', '')",
                TEST_METRIC2,
                TEST_ENTITY
        );

        String[][] expectedRows = {
                {TEST_ENTITY}
        };

        assertSqlQueryRows("CONCAT in WHERE clause gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("4017")
    @Test
    public void testConcatInWhereNonEmpty() {
        String sqlQuery = String.format(
                "SELECT entity FROM \"%s\" WHERE entity = CONCAT('%s', '%s')",
                TEST_METRIC2,
                TEST_ENTITY_LEFT,
                TEST_ENTITY_RIGHT + TEST_ENTITY_INDEX
        );

        String[][] expectedRows = {
                {TEST_ENTITY}
        };

        assertSqlQueryRows("CONCAT in WHERE clause gives wrong result", expectedRows, sqlQuery);
    }


    @Issue("4017")
    @Test
    public void testConcatInWhereNumber() {
        String sqlQuery = String.format(
                "SELECT entity FROM \"%s\" WHERE entity = CONCAT('%s', %d)",
                TEST_METRIC2,
                TEST_ENTITY_LEFT + TEST_ENTITY_RIGHT,
                TEST_ENTITY_INDEX
        );

        String[][] expectedRows = {
                {TEST_ENTITY}
        };

        assertSqlQueryRows("CONCAT in WHERE clause gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("4233")
    @Test
    public void testConcatWithDate() {
        String sqlQuery = "SELECT CONCAT('1970-01-01T00:00:00.00', '0', 'Z')";

        String[][] expectedRows = new String[][] {
                {"1970-01-01T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
