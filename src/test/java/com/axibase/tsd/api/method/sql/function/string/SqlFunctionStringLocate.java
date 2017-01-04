package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlFunctionStringLocate extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();

        series1.setMetric(TEST_METRIC1_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );
        series1.addTag("tag1", "Word word WORD worD");

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1));
    }

    @DataProvider
    public Object[][] provideTestsDataForLocateTest() {
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

    /**
     * #3749
     */
    @Test(dataProvider = "provideTestsDataForLocateTest")
    public void testLocateInSelect(String word, String position) {
        String sqlQuery = String.format(
                "SELECT LOCATE(\"%s\", tags.tag1) FROM '%s' t1",
                word,
                TEST_METRIC1_NAME
        );

        String[][] expectedRows = {
                {position}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Locate in SELECT gives wrong result");
    }

    /**
     * #3749
     */
    @Test(dataProvider = "provideTestsDataForLocateTest")
    public void testLocateInWhere(String word, String position) {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' t1 WHERE LOCATE(\"%s\", tags.tag1) = %s",
                TEST_METRIC1_NAME,
                word,
                position
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Locate in WHERE gives wrong result");
    }

    /**
     * #3749
     */
    @Test(dataProvider = "provideTestsDataForLocateTest")
    public void testLocateInHaving(String word, String position) {
        String sqlQuery = String.format(
                "SELECT tags.tag1, count(value) FROM '%s' t1 " +
                        "GROUP BY tags.tag1 " +
                        "HAVING count(LOCATE(\"%s\", tags.tag1)) > 0",
                TEST_METRIC1_NAME,
                word
        );

        String[][] expectedRows = {
                {"Word word WORD worD", "1"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Locate in HAVING gives wrong result");
    }
}
