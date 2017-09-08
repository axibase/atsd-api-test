package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class DateFormatInsideClausesTest extends SqlTest {
    private static final String METRIC_NAME1 = metric();
    private static final String METRIC_NAME2 = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(entity(), METRIC_NAME1);
        series1.addSamples(
                Sample.ofDateInteger("2017-02-09T13:00:00.000Z", 10),
                Sample.ofDateInteger("2017-02-10T12:00:00.000Z", 11),
                Sample.ofDateInteger("2017-02-10T07:00:00.000Z", 12),
                Sample.ofDateInteger("2017-02-12T12:00:00.000Z", 13),
                Sample.ofDateInteger("2017-02-11T12:00:00.000Z", 14),
                Sample.ofDateInteger("2017-02-09T12:00:00.000Z", 15)
        );

        Series series2 = new Series(entity(), METRIC_NAME2);
        series2.addSamples(
                Sample.ofDateInteger("2017-02-09T12:00:00.000Z", 0),
                Sample.ofDateInteger("2017-02-09T13:00:00.000Z", 0),
                Sample.ofDateInteger("2017-02-10T12:00:00.000Z", 0),
                Sample.ofDateInteger("2017-02-11T12:00:00.000Z", 0),
                Sample.ofDateInteger("2017-02-12T12:00:00.000Z", 0)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    /**
     * #3893
     */
    @Test
    public void testDateFormatInsideHavingGroupingByPeriod() throws Exception {
        String sqlQuery = String.format(
                "SELECT count(value) FROM \"%s\" " +
                        "GROUP BY period(1 day) " +
                        "HAVING date_format(time, 'u') = '4'",
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {"2"}
        };

        assertSqlQueryRows("Query with date_format inside HAVING gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3746
     */
    @Test
    public void testDateFormatEWithTzInsideWhere() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE date_format(time, 'E', 'PST') = 'Thu' " +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"10"},
                {"12"},
                {"15"}
        };

        assertSqlQueryRows("Query with date_format inside WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3746
     */
    @Test
    public void testDateFormatUWithTzInsideWhere() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE date_format(time, 'u', 'PST') = 4 " +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"10"},
                {"12"},
                {"15"}
        };

        assertSqlQueryRows("Query with date_format inside WHERE gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #4231
     */
    @Test
    public void testDateFormatInsideWhereComplexClause() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE date_format(time) = '2017-02-10T07:00:00.000Z' OR date_format(time) = '2017-02-10T12:00:00.000Z'" +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"11"},
                {"12"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4231
     */
    @Test
    public void testDateFormatInsideWhereWithoutMs() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE date_format(time, 'yyyy-MM-dd''T''HH:mm:ssZZ', 'GMT0') = '2017-02-10T07:00:00Z' " +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"12"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4231
     */
    @Test
    public void testDateFormatDefaultInsideWhere() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "WHERE date_format(time) = '2017-02-10T07:00:00.000Z' " +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"12"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #3746
     */
    @Test
    public void testDateFormatWithTzInsideGroupBy() throws Exception {
        String sqlQuery = String.format(
                "SELECT count(*) AS k FROM \"%s\" " +
                        "GROUP BY date_format(time, 'E', 'PST') " +
                        "ORDER BY k",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"1"},
                {"1"},
                {"1"},
                {"3"}
        };

        assertSqlQueryRows("Query with date_format inside GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3746
     */
    @Test
    public void testDateFormatEWithTzInsideOrderBy() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "ORDER BY date_format(time, 'E', 'PST'), value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"11"},
                {"14"},
                {"13"},
                {"10"},
                {"12"},
                {"15"}
        };

        assertSqlQueryRows("Query with date_format inside ORDER BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3746
     */
    @Test
    public void testDateFormatUWithTzInsideOrderBy() throws Exception {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" " +
                        "ORDER BY date_format(time, 'u', 'PST'), value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"10"},
                {"12"},
                {"15"},
                {"11"},
                {"14"},
                {"13"}
        };

        assertSqlQueryRows("Query with date_format inside ORDER BY gives wrong result", expectedRows, sqlQuery);
    }
}
