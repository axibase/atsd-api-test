package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.*;
import static org.testng.AssertJUnit.assertEquals;

public class ThreeValuedLogicTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME_1 = metric();
    private static final String METRIC_NAME_2 = metric();
    private static final String METRIC_NAME_3 = metric();

    @BeforeTest
    private static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        Series series = new Series(ENTITY_NAME, METRIC_NAME_1);
        series.addSamples(new Sample(ISO_TIME, 1));
        seriesList.add(series);

        series = new Series(ENTITY_NAME, METRIC_NAME_2);
        series.addSamples(new Sample(ISO_TIME, 2));
        seriesList.add(series);

        series = new Series(ENTITY_NAME, METRIC_NAME_3);
        series.addSamples(new TextSample(ISO_TIME, "hello"));
        seriesList.add(series);

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "logicTableSelectTestProvider")
    public Object[][] provideSelectTestsData() {
        return new Object[][]{
                {"1 = 1", "true"}, // SIMPLE
                {"1 != 1", "false"},
                {"NaN = NaN", "null"},
                {"NULL = NULL", "null"},
                {"NOT (1 = 1)", "false"},
                {"NOT (1 != 1)", "true"},
                {"NOT (NaN = NaN)", "null"},
                {"NOT (NULL = NULL)", "null"},
                {"(1 = 1) AND (2 = 2)", "true"}, // COMPLEX
                {"(1 = 1) AND (2 != 2)", "false"},
                {"(1 != 1) AND (2 != 2)", "false"},
                {"(1 = 1) AND (NaN = NaN)", "null"},
                {"(1 != 1) AND (NaN = NaN)", "false"},
                {"(NaN = NaN) AND (NaN = NaN)", "null"},
                {"(1 = 1) AND (NULL = NULL)", "null"},
                {"(1 != 1) AND (NULL = NULL)", "false"},
                {"(NULL = NULL) AND (NULL = NULL)", "null"},
                {"(1 = 1) OR (2 = 2)", "true"},
                {"(1 = 1) OR (2 != 2)", "true"},
                {"(1 != 1) OR (2 != 2)", "false"},
                {"(1 = 1) OR (NaN = NaN)", "true"},
                {"(1 != 1) OR (NaN = NaN)", "null"},
                {"(NaN = NaN) OR (NaN = NaN)", "null"},
                {"(1 = 1) OR (NULL = NULL)", "true"},
                {"(1 != 1) OR (NULL = NULL)", "null"},
                {"(NULL = NULL) OR (NULL = NULL)", "null"}
        };
    }

    /**
     * #4286
     */
    @Test(dataProvider = "logicTableSelectTestProvider")
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
    @Test(dataProvider = "logicTableSelectTestProvider")
    public void whereClauseLogicTableTest(String param, String expectedValue) {
        String query = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE %s",
                METRIC_NAME_1, param
        );

        String actualValue;
        try {
            actualValue = queryTable(query).getValueAt(0, 0);
        } catch (IndexOutOfBoundsException e) {
            actualValue = "";
        }

        String assertMessage = String.format("Incorrect result of %s function in WHERE clause", param);
        expectedValue = expectedValue.equals("true") ? "1" : "";

        assertEquals(assertMessage, expectedValue, actualValue);
    }

    @DataProvider(name = "logicTableJoinTestProvider")
    public Object[][] provideJoinTestsData() {
        return new Object[][]{
                {"t1.tags IS NULL", "true"},
                {"t2.tags IS NULL", "true"},
                {"t3.tags IS NULL", "true"},
                {"t1.value = t3.value", "null"},
                {"t1.text = t3.text", "null"},
                {"t1.tags = t2.tags", "null"},
                {"t2.tags = t3.tags", "null"},
                {"t1.metric.label = t2.metric.label", "null"},
                {"t1.metric.description = t2.metric.description", "null"},
                {"t1.metric.units = t2.metric.units", "null"},
                {"t1.metric.minvalue = t2.metric.minvalue", "null"},
                {"t1.metric.maxvalue = t2.metric.maxvalue", "null"},
                {"t1.metric.tags = t2.metric.tags", "null"},
                {"t1.entity.label = t2.entity.label", "null"},
                {"t1.entity.tags = t2.entity.tags", "null"}
        };
    }

    /**
     * #4286
     */
    @Test(dataProvider = "logicTableJoinTestProvider")
    public void joinedMetricsLogicTableTest(String param, String expectedValue) {
        String query = String.format(
                "SELECT %s " +
                        "FROM '%s' t1 JOIN '%s' t2 JOIN '%s' t3 ",
                param, METRIC_NAME_1, METRIC_NAME_2, METRIC_NAME_3
        );

        String actualValue;
        try {
            actualValue = queryTable(query).getValueAt(0, 0);
        } catch (IndexOutOfBoundsException e) {
            actualValue = "";
        }

        String assertMessage = String.format("Incorrect result of %s function in WHERE clause", param);

        assertEquals(assertMessage, expectedValue, actualValue);
    }
}
