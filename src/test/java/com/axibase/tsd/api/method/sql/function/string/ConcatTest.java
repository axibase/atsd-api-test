package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.method.sql.function.string.CommonData.POSSIBLE_STRING_FUNCTION_ARGS;
import static com.axibase.tsd.api.method.sql.function.string.CommonData.insertSeriesWithMetric;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class ConcatTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    private static final String TEST_METRIC1 = metric();
    private static final String TEST_METRIC2 = metric();
    private static final String TEST_METRIC3 = metric();

    private static void prepareFunctionalConcatTestData() throws Exception {
        String testEntity = entity();

        List<Series> seriesList = new ArrayList<>();
        {
            Series series = new Series(testEntity, TEST_METRIC1);
            series.addData(new Sample("2016-06-03T09:20:18.000Z", "3.0"));
            series.addData(new Sample("2016-06-03T09:21:18.000Z", "3.10"));
            series.addData(new Sample("2016-06-03T09:22:18.000Z", "3.14"));
            series.addData(new Sample("2016-06-03T09:23:18.000Z", "3.1415"));
            seriesList.add(series);
        }
        {
            Series series = new Series();
            Registry.Metric.register(TEST_METRIC2);
            series.setEntity(testEntity);
            series.setMetric(TEST_METRIC2);
            series.addData(new Sample("2016-06-03T09:23:18.000Z", "5.555"));
            seriesList.add(series);
        }
        {
            Series series = new Series();
            Registry.Metric.register(TEST_METRIC3);
            series.setEntity(testEntity);
            series.setMetric(TEST_METRIC3);
            series.addData(new Sample("2016-06-03T09:23:18.000Z", "5.0"));
            seriesList.add(series);
        }

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

    /**
     * #2920
     */
    @Test(dataProvider = "applyTestProvider")
    public void testApply(String param) throws Exception {
        String sqlQuery = String.format("SELECT CONCAT(%s) FROM '%s'",
                param, TEST_METRIC
        );
        assertOkRequest(String.format("Can't apply CONCAT function to %s", param), queryResponse(sqlQuery));
    }

    /**
     * #3768
     */
    @Test
    public void testConcatWordAndNumber() throws Exception {
        String sqlQuery = String.format("SELECT CONCAT('a:', value) FROM '%s'",
                TEST_METRIC1
        );

        String[][] expectedRows = {
                {"a:3"},
                {"a:3.1"},
                {"a:3.14"},
                {"a:3.14"},
        };

        assertSqlQueryRows("CONCAT word and number without CAST gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3768
     */
    @Test
    public void testConcatWordAndTwoNumbers() throws Exception {
        String sqlQuery = String.format(
                "SELECT CONCAT('a:', t1.value, ':', t2.value), CONCAT('a:', t1.value, ':', t3.value) " +
                "FROM '%s' t1 JOIN '%s' t2 JOIN '%s' t3",
                TEST_METRIC1,
                TEST_METRIC2,
                TEST_METRIC3
        );

        String[][] expectedRows = {
                {"a:3.14:5.56", "a:3.14:5"}
        };

        assertSqlQueryRows("CONCAT word and two numbers gives wrong result", expectedRows, sqlQuery);
    }
}
