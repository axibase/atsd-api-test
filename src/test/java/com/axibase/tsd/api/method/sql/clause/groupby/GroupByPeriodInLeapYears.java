package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.DECIMAL_VALUE;
import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class GroupByPeriodInLeapYears extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String firstDayOf2016Year = "2016-01-01T12:00:00.000Z";
        String lastDayOf2016Year = "2016-12-31T12:00:00.000Z";
        String firstDayOf2016February = "2016-02-01T12:00:00.000Z";
        String lastDayOf2016February = "2016-02-29T12:00:00.000Z";

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addData(new Sample(firstDayOf2016Year, DECIMAL_VALUE));
        series.addData(new Sample(firstDayOf2016February, DECIMAL_VALUE));
        series.addData(new Sample(lastDayOf2016February, DECIMAL_VALUE));
        series.addData(new Sample(lastDayOf2016Year, DECIMAL_VALUE));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3825
     */
    @Test
    public void testGroupByPeriodLeapYear() {
        String sqlQuery = String.format(
                "SELECT count(value) FROM '%s' GROUP BY period(1 year)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"4"}
        };

        assertSqlQueryRows("GROUP BY period gives wrong result with leap year", expectedRows, sqlQuery);
    }

    /**
     * #3825
     */
    @Test
    public void testGroupByPeriodLeapFebruary() {
        String sqlQuery = String.format(
                "SELECT count(value) FROM '%s' GROUP BY period(1 month)",
                TEST_METRIC_NAME

        );

        String[][] expectedRows = {
                {"1"},
                {"2"},
                {"1"}
        };

        assertSqlQueryRows("GROUP BY period gives wrong result with leap year's February", expectedRows, sqlQuery);
    }
}
