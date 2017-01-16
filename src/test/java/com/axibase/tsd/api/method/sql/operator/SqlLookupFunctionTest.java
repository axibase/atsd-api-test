package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlLookupFunctionTest extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();
    private static final String TEST_METRIC_NAME1 = metric();
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_ENTITY_NAME1 = entity();

    private static void prepareReplacementTable(String name) throws IOException {
        ReplacementTable table = new ReplacementTable(name);

        Registry.ReplacementTable.register(name);

        table.addValue("-1", "negative");
        table.addValue("1", "positive");
        table.addValue("2", "2");
        table.addValue("word", "3");
        table.addValue("words", "letters");
        table.addValue("3", "-3");
        table.addValue("4", "3.14");
        table.addValue("PI", "3.14");
        table.addValue("3.14", "PI");

        ReplacementTableMethod.create(table);
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        prepareReplacementTable("tableForLookupTest1");
        prepareReplacementTable("tableForLookupTest2");

        List<Series> seriesList = new ArrayList<>();
        {
            Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

            series.addData(new TextSample("2016-06-03T09:20:00.000Z", "word"));
            series.addData(new TextSample("2016-06-03T09:21:00.000Z", "-1"));
            series.addData(new TextSample("2016-06-03T09:22:00.000Z", "1"));
            series.addData(new TextSample("2016-06-03T09:23:00.000Z", "2"));
            series.addData(new TextSample("2016-06-03T09:24:00.000Z", "word"));
            series.addData(new TextSample("2016-06-03T09:25:00.000Z", "words"));
            series.addData(new TextSample("2016-06-03T09:26:00.000Z", "3"));
            series.addData(new TextSample("2016-06-03T09:27:00.000Z", "4"));
            series.addData(new TextSample("2016-06-03T09:28:00.000Z", "PI"));
            series.addData(new TextSample("2016-06-03T09:29:00.000Z", "3.14"));
            series.addData(new TextSample("2016-06-03T09:30:00.000Z", "nothing"));
            seriesList.add(series);
        }


        {
            Map<String, String> tags = new HashMap<>();
            tags.put("1", "-1");
            tags.put("2", "1");
            tags.put("3", "2");
            tags.put("4", "word");
            tags.put("5", "words");
            tags.put("6", "3");
            tags.put("7", "4");
            tags.put("8", "PI");
            tags.put("9", "3.14");
            tags.put("10", "nothing");
            Metric metric = new Metric(TEST_METRIC_NAME1);
            metric.setTags(tags);
            MetricMethod.createOrReplaceMetricCheck(metric);
            Entity entity = new Entity(TEST_ENTITY_NAME1);
            entity.setTags(tags);
            EntityMethod.createOrReplaceEntityCheck(entity);

            Series series = new Series();
            series.setEntity(TEST_ENTITY_NAME1);
            series.setMetric(TEST_METRIC_NAME1);
            series.setTags(tags);
            series.addData(new Sample("2016-06-03T09:20:00.000Z", "1"));
            seriesList.add(series);
        }


        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "lookupTestTestProvider")
    public Object[][] provideTestsDataForLookupTest() {
        return new Object[][]{
                {
                        "1", "negative"
                },
                {
                        "2", "positive"
                },
                {
                        "3", "2"
                },
                {
                        "4", "3"
                },
                {
                        "5", "letters"
                },
                {
                        "6", "-3"
                },
                {
                        "7", "3.14"
                },
                {
                        "8", "3.14"
                },
                {
                        "9", "PI"
                },
                {
                        "10", "null"
                }

        };
    }

    /**
     * #3555
     */
    @Test
    public void testLookupFromText() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.text) " +
                        "FROM '%s' t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = { // values corresponding to Replacement Table
                {"3"},              // if we consider t1.text as RT's key
                {"negative"},
                {"positive"},
                {"2"},
                {"3"},
                {"letters"},
                {"-3"},
                {"3.14"},
                {"3.14"},
                {"PI"},
                {"null"}
        };

        assertSqlQueryRows("LOOKUP gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testDoubleLookupFromText() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', LOOKUP('table', t1.text)) " +
                        "FROM '%s' t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = { // looking twice in Replacement Table
                {"-3"},             // first time -- just like before
                {"null"},           // second time -- using values fromm 1st LOOKUP as new RT's keys
                {"null"},
                {"2"},
                {"-3"},
                {"null"},
                {"null"},
                {"PI"},
                {"PI"},
                {"3.14"},
                {"null"}
        };

        assertSqlQueryRows("LOOKUP in LOOKUP gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupCaseSensitiveness() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', 'Word'), LOOKUP('table', 'word') " +
                        "FROM '%s' t1 " +
                        "LIMIT 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"null", "3"}
        };

        assertSqlQueryRows("LOOKUP is not case-sensitive, as it should be", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupWithWrongTable() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('noTable', t1.text) " +
                        "FROM '%s' t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"},
                {"null"}
        };

        assertSqlQueryRows("LOOKUP should return null with nonexist table", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupInWhere() {
        String sqlQuery = String.format(
                "SELECT t1.text " +
                        "FROM '%s' t1 " +
                        "WHERE LOOKUP('table', t1.text) IS NOT NULL",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"word"},
                {"-1"},
                {"1"},
                {"2"},
                {"word"},
                {"words"},
                {"3"},
                {"4"},
                {"PI"},
                {"3.14"}
        };

        assertSqlQueryRows("LOOKUP in WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupInCast() {
        String sqlQuery = String.format(
                "SELECT CAST(LOOKUP('table', t1.text) as Number) " +
                        "FROM '%s' t1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"3"},
                {"NaN"},
                {"NaN"},
                {"2"},
                {"3"},
                {"NaN"},
                {"-3"},
                {"3.14"},
                {"3.14"},
                {"NaN"},
                {"NaN"}
        };

        assertSqlQueryRows("LOOKUP in CAST gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupInGroupBy() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.text), count(*) " +
                        "FROM '%s' t1 " +
                        "GROUP BY LOOKUP('table', t1.text) " +
                        "ORDER BY 1 DESC",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"positive", "1"},
                {"negative", "1"},
                {"letters", "1"},
                {"PI", "1"},
                {"3.14", "2"},
                {"3", "2"},
                {"2", "1"},
                {"-3", "1"},
                {"null", "1"}
        };

        assertSqlQueryRows("LOOKUP in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupInHaving() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.text), count(*) " +
                        "FROM '%s' t1 " +
                        "GROUP BY LOOKUP('table', t1.text) " +
                        "HAVING sum(CAST(LOOKUP('table', t1.text) as Number)) > 0 " +
                        "ORDER BY 1 DESC",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"3.14", "2"},
                {"3", "2"},
                {"2", "1"}
        };

        assertSqlQueryRows("LOOKUP in HAVING gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupInLength() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.text) " +
                        "FROM '%s' t1 " +
                        "WHERE LENGTH(LOOKUP('table', t1.text)) > 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"negative"},
                {"positive"},
                {"letters"},
                {"3.14"},
                {"3.14"}
        };

        assertSqlQueryRows("LOOKUP in LENGTH gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3555
     */
    @Test
    public void testLookupWithLike() {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.text) " +
                        "FROM '%s' t1 " +
                        "WHERE LOOKUP('table', t1.text) LIKE '3'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"3"},
                {"3"}
        };

        assertSqlQueryRows("LOOKUP with LIKE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3769
     */
    @Test(dataProvider = "lookupTestTestProvider")
    public void testLookupWithEntityTags(String key, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.entity.tags.'%s') " +
                        "FROM '%s' t1 ",
                key,
                TEST_METRIC_NAME1
        );

        String[][] expectedRows = {
                {expectedResult}
        };

        assertSqlQueryRows("LOOKUP with entity.tags gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3769
     */
    @Test(dataProvider = "lookupTestTestProvider")
    public void testLookupWithMetricTags(String key, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.metric.tags.'%s') " +
                        "FROM '%s' t1 ",
                key,
                TEST_METRIC_NAME1
        );

        String[][] expectedRows = {
                {expectedResult}
        };

        assertSqlQueryRows("LOOKUP with metric tags gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3769
     */
    @Test(dataProvider = "lookupTestTestProvider")
    public void testLookupWithSeriesTags(String key, String expectedResult) {
        String sqlQuery = String.format(
                "SELECT LOOKUP('table', t1.tags.'%s') " +
                        "FROM '%s' t1 ",
                key,
                TEST_METRIC_NAME1
        );

        String[][] expectedRows = {
                {expectedResult}
        };

        assertSqlQueryRows("LOOKUP with series tags gives wrong result", expectedRows, sqlQuery);
    }
}
