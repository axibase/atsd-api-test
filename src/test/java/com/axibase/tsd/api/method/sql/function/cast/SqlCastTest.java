package com.axibase.tsd.api.method.sql.function.cast;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class SqlCastTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series();
        Series series2 = new Series();
        Series series3 = new Series();

        series1.setMetric(TEST_METRIC1_NAME);
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );
        series1.addTag("numeric_tag", "4");

        series2.setMetric(TEST_METRIC2_NAME);
        series2.setEntity(TEST_ENTITY_NAME);
        series2.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );
        series2.addTag("numeric_tag", "123");

        series3.setMetric(TEST_METRIC3_NAME);
        series3.setEntity(TEST_ENTITY_NAME);
        series3.setData(Arrays.asList(
                new Sample("2016-06-03T09:20:00.000Z", "1")
                )
        );
        series3.addTag("numeric_tag", "text12a3a");

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2, series3));
    }

    /**
     * #3661
     */
    @Test
    public void testCastSumJoin() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Sum of CASTs with Join gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastSumJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag) + cast(t2.tags.numeric_tag) FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"127"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Sum of CASTs with Join Using Entity gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastMultiply() {
        String sqlQuery = String.format(
                "SELECT cast(t1.tags.numeric_tag)*2, cast(t2.tags.numeric_tag)*2, cast(t3.tags.numeric_tag)*2 " +
                        "FROM '%s' t1 JOIN USING ENTITY '%s' t2 JOIN USING ENTITY '%s' t3",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"8", "246", "NaN"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "Multiplication of CASTs gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastConcat() {
        String sqlQuery = String.format(
                "SELECT cast(concat(t1.tags.numeric_tag, t2.tags.numeric_tag))*2 FROM '%s' t1 JOIN USING ENTITY '%s' t2",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"8246"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "CAST of CONCAT gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastGroupBy() {
        String sqlQuery = String.format(
                "SELECT count(t1.value),t1.tags.numeric_tag FROM '%s' t1 OUTER JOIN '%s' t2 OUTER JOIN '%s' t3 " +
                        "GROUP BY CAST(t1.tags.numeric_tag)",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "4"},
                {"0", "null"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "CAST in GROUP BY gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastWhere() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM '%s' t1 JOIN USING ENTITY '%s' t2" +
                        " WHERE CAST(t1.tags.numeric_tag) + CAST(t2.tags.numeric_tag) = 127",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "CAST in WHERE gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastWhereAndConcat() {
        String sqlQuery = String.format(
                "SELECT t1.value" +
                        " FROM '%s' t1 JOIN USING ENTITY '%s' t2" +
                        " WHERE CAST(CONCAT(t1.tags.numeric_tag, t2.tags.numeric_tag)) = 4123",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "CAST in WHERE with CONCAT gives wrong result");
    }

    /**
     * #3661
     */
    @Test
    public void testCastHaving() {
        String sqlQuery = String.format(
                "SELECT count(t1.value),t1.tags.numeric_tag FROM '%s' t1 OUTER JOIN '%s' t2 " +
                        "OUTER JOIN '%s' t3 " +
                        "GROUP BY CAST(t1.tags.numeric_tag) " +
                        "HAVING SUM(CAST(t1.tags.numeric_tag)) != 0",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME
        );

        String[][] expectedRows = {
                {"1", "4"},
                {"0", "null"}
        };

        assertSqlQueryRows(sqlQuery, expectedRows, "CAST in HAVING gives wrong result");
    }

}
