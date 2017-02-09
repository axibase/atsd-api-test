package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.DECIMAL_VALUE;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class GroupByCaseExpression extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.setData(Arrays.asList(
                new Sample("2017-02-09T12:00:00.000Z", DECIMAL_VALUE),
                new Sample("2017-02-10T12:00:00.000Z", DECIMAL_VALUE),
                new Sample("2017-02-11T12:00:00.000Z", DECIMAL_VALUE),
                new Sample("2017-02-12T12:00:00.000Z", DECIMAL_VALUE)
                )
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInSelectWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END " +
                        "FROM '%s' " +
                        "ORDER BY 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend"},
                {"weekend"},
                {"workday"},
                {"workday"}
        };

        assertSqlQueryRows("CASE in SELECT without GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInGroupByOnly() {
        String sqlQuery = String.format(
                "SELECT count(value) FROM '%s' " +
                        "GROUP BY CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"2"}
        };

        assertSqlQueryRows("CASE in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInSelectAndGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END AS day_type, " +
                        "count(value) " +
                        "FROM '%s' " +
                        "GROUP BY day_type " +
                        "ORDER BY 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend", "2"},
                {"workday", "2"}
        };

        assertSqlQueryRows("CASE in SELECT and GROUP BY gives wrong result", expectedRows, sqlQuery);
    }
}
