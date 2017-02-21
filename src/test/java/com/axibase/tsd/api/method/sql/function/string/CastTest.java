package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class CastTest extends SqlTest {
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();


    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] metricNames = {TEST_METRIC1_NAME, TEST_METRIC2_NAME, TEST_METRIC3_NAME};
        String[] tags = {"4", "123", "text12a3a"};

        Registry.Entity.register(TEST_ENTITY_NAME);

        for (int i = 0; i < metricNames.length; i++) {
            String metricName = metricNames[i];
            Registry.Metric.register(metricName);

            Series series = new Series();
            series.setEntity(TEST_ENTITY_NAME);
            series.setMetric(metricName);

            series.setData(Collections.singletonList(
                    new Sample("2016-06-03T09:20:00.000Z", "1")));

            String tag = tags[i];
            series.addTag("numeric_tag", tag);

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
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

        assertSqlQueryRows("Sum of CASTs with Join gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Sum of CASTs with Join Using Entity gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("Multiplication of CASTs gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("CAST of CONCAT gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("CAST in GROUP BY gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("CAST in WHERE gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("CAST in WHERE with CONCAT gives wrong result", expectedRows, sqlQuery);
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

        assertSqlQueryRows("CAST in HAVING gives wrong result", expectedRows, sqlQuery);
    }

}
