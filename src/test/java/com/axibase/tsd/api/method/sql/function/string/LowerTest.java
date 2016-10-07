package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.util.Collections;

import static com.axibase.tsd.api.Util.TestNames.generateEntityName;
import static com.axibase.tsd.api.Util.TestNames.generateMetricName;


public class LowerTest extends SqlTest {
    private final static String SELECT_GROUP = "lower-string-function-select-test-group";
    private final static Sample DEFAULT_SAMPLE = new Sample("2016-06-03T09:23:00.000Z", 0);
    private static final String APPLY_GROUP = "lower-string-function-apply-test-group";
    private static String APPLY_TEST_GROUP_METRIC;


    @BeforeClass
    public static void generateNames() {
        APPLY_TEST_GROUP_METRIC = generateMetricName();
    }

    @BeforeGroups(groups = {APPLY_GROUP})
    public void prepareApplyTestData() throws FileNotFoundException, InterruptedException {
        String entityName = generateEntityName();
        Series series = new Series(entityName, APPLY_TEST_GROUP_METRIC);
        series.setEntity(entityName);
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeries(Collections.singletonList(series));
        Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);
    }

    @DataProvider(name = "applyTestProvider", parallel = true)
    public Object[][] provideApplyTestsData() {
        return new Object[][]{
                {"entity"},
                {"metric"},
                {"tags"},
                {"tags.a"},
                {"tags.'a'"},
                {"tags.\"a\""},
                {"metric.tags"},
                {"metric.tags.a"},
                {"metric.tags.'a'"},
                {"metric.tags.\"a\""},
                {"entity.tags"},
                {"entity.tags.a"},
                {"entity.tags.'a'"},
                {"entity.tags.\"a\""},
                {"entity.groups"},
                {"\"abc\""},
                {"'a'"}

        };
    }

    @Test(dataProvider = "applyTestProvider", groups = {APPLY_GROUP})
    public void applyTest(String param) throws Exception {
        String sqlQuery = String.format("SELECT LOWER(%s) FROM '%s'",
                param, APPLY_TEST_GROUP_METRIC
        );
        assertOkRequest(executeQuery(sqlQuery));
    }


    @DataProvider(name = "selectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"VaLuE", "value"},
                {"VALUE", "value"},
                {"444'a3'A4", "444'a3'a4"},
                {"aBc12@", "abC12@"}
        };
    }


    @Test(groups = {SELECT_GROUP}, dataProvider = "selectTestProvider")
    public void testSelectEntity(String entityName, String expectedFormattedEntityName) throws Exception {
        String metricName = generateMetricName();

        Series series = new Series(entityName, metricName);
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeries(Collections.singletonList(series));
        Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);

        String sqlQuery = String.format("SELECT LOWER(entity) FROM '%s'",
                metricName
        );

        String[][] expectedRows = {
                {expectedFormattedEntityName}
        };

        assertSqlQueryRows(sqlQuery, expectedRows);
    }
}
