package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class JoinWithDatetimeTest extends SqlTest {
    private static String TEST_METRIC_1 = metric();
    private static String TEST_METRIC_2 = metric();

    @BeforeTest
    public void prepareData() throws Exception {
        String entity = entity();

        Series series1 = new Series(entity, TEST_METRIC_1);
        series1.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-07T00:00:00Z", 7)
        );

        Series series2 = new Series(entity, TEST_METRIC_2);
        series2.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-06T00:00:00Z", 6)
        );

        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    /**
     * #4225
     */
    @Test
    public void testShortDatetimeSyntaxInSelect() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                "FROM \"%s\" m1 " +
                "OUTER JOIN \"%s\" m2",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z"},
                {"2017-01-06T00:00:00.000Z", "null",                     "2017-01-06T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z", "2017-01-07T00:00:00.000Z", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortTimeSyntaxInSelect() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483228800000", "1483228800000", "1483228800000"},
                {"1483315200000", "1483315200000", "1483315200000"},
                {"1483660800000", "null",          "1483660800000"},
                {"1483747200000", "1483747200000", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortDatetimeSyntaxInWhere() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-06T00:00:00.000Z' AND '2017-01-07T00:00:00.000Z' " +
                        "   AND m2.datetime BETWEEN '2017-01-06T00:00:00.000Z' AND '2017-01-06T00:00:00.000Z'",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-06T00:00:00.000Z", "null", "2017-01-06T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortTimeSyntaxInWhere() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE time >= 1483660800000 AND m1.time <= 1483747200000",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483660800000", "null",          "1483660800000"},
                {"1483747200000", "1483747200000", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortDatetimeSyntaxInGroupBy() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE datetime BETWEEN '2017-01-02T00:00:00.000Z' AND '2017-01-07T00:00:00.000Z' " +
                        "GROUP BY datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-06T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullDatetimeSyntaxInGroupBy() {
        String sqlQuery = String.format(
                "SELECT m1.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.datetime BETWEEN '2017-01-02T00:00:00.000Z' AND '2017-01-07T00:00:00.000Z' " +
                        "GROUP BY m1.datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"null"},
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortTimeSyntaxInGroupBy() {
        String sqlQuery = String.format(
                "SELECT time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE time >= 1483315200000 AND time <= 1483747200000 " +
                        "GROUP BY time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483315200000"},
                {"1483660800000"},
                {"1483747200000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullTimeSyntaxInGroupBy() {
        String sqlQuery = String.format(
                "SELECT m1.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.time >= 1483315200000 AND m1.time <= 1483747200000 " +
                        "GROUP BY m1.time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"null"},
                {"1483315200000"},
                {"1483747200000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortDatetimeSyntaxInHaving() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "GROUP BY datetime " +
                        "HAVING datetime >= '2017-01-02T00:00:00.000Z' ",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-06T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullDatetimeSyntaxInHaving() {
        String sqlQuery = String.format(
                "SELECT m1.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "GROUP BY m1.datetime " +
                        "HAVING m1.datetime >= '2017-01-02T00:00:00.000Z'",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortTimeSyntaxInHaving() {
        String sqlQuery = String.format(
                "SELECT time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "GROUP BY time " +
                        "HAVING time >= 1483660800000",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483660800000"},
                {"1483747200000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullTimeSyntaxInHaving() {
        String sqlQuery = String.format(
                "SELECT m1.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "GROUP BY m1.time " +
                        "HAVING m1.time >= 1483315200000 OR m1.time IS NULL",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"null"},
                {"1483315200000"},
                {"1483747200000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortDatetimeSyntaxInOrderBy() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z"},
                {"2017-01-06T00:00:00.000Z", "null",                     "2017-01-06T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z", "2017-01-07T00:00:00.000Z", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testShortTimeSyntaxInOrderBy() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "ORDER BY time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483228800000", "1483228800000", "1483228800000"},
                {"1483315200000", "1483315200000", "1483315200000"},
                {"1483660800000", "null",          "1483660800000"},
                {"1483747200000", "1483747200000", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullDatetimeSyntaxInOrderBy() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "ORDER BY m1.datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-06T00:00:00.000Z", "null",                     "2017-01-06T00:00:00.000Z"},
                {"2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z", "2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z"},
                {"2017-01-07T00:00:00.000Z", "2017-01-07T00:00:00.000Z", "null"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4225
     */
    @Test
    public void testFullTimeSyntaxInOrderBy() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "ORDER BY m2.time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483747200000", "1483747200000", "null"},
                {"1483228800000", "1483228800000", "1483228800000"},
                {"1483315200000", "1483315200000", "1483315200000"},
                {"1483660800000", "null",          "1483660800000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4258
     */
    @Test
    public void testIntersectingPeriodDatetimeCondition() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-06T00:00:00Z' " +
                        "   AND m2.datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z"},
                {"2017-01-06T00:00:00.000Z", "null",                     "2017-01-06T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4258
     */
    @Test
    public void testIntersectingSingleDayDatetimeCondition() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-02T00:00:00Z' " +
                        "   AND m2.datetime BETWEEN '2017-01-02T00:00:00Z' AND '2017-01-07T00:00:00Z' " +
                        "ORDER BY datetime",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z", "2017-01-02T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4258
     */
    @Test
    public void testInvalidDatetimeCondition() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.datetime BETWEEN '2017-01-01T00:00:00Z' AND '2017-01-02T00:00:00Z' " +
                        "   AND m2.datetime BETWEEN '2017-01-05T00:00:00Z' AND '2017-01-07T00:00:00Z'",
                TEST_METRIC_1, TEST_METRIC_2
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Invalid date conditions", response);
    }


    /**
     * #4258
     */
    @Test
    public void testIntersectingPeriodTimeCondition() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.time >= 1483228800000 AND m1.time <= 1483660800000 " +
                        "   AND m2.time >= 1483315200000 AND m2.time <= 1483747200000 " +
                        "ORDER BY time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483315200000", "1483315200000", "1483315200000"},
                {"1483660800000", "null",          "1483660800000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4258
     */
    @Test
    public void testIntersectingSingleDayTimeCondition() {
        String sqlQuery = String.format(
                "SELECT time, m1.time, m2.time " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.time >= 1483228800000 AND m1.time <= 1483660800000 " +
                        "   AND m2.time >= 1483660800000 AND m2.time <= 1483747200000 " +
                        "ORDER BY time",
                TEST_METRIC_1, TEST_METRIC_2
        );

        String[][] expectedRows = new String[][] {
                {"1483660800000", "null",          "1483660800000"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4258
     */
    @Test
    public void testInvalidTimeCondition() {
        String sqlQuery = String.format(
                "SELECT datetime, m1.datetime, m2.datetime " +
                        "FROM \"%s\" m1 " +
                        "OUTER JOIN \"%s\" m2 " +
                        "WHERE m1.time >= 1483228800000 AND m1.time <= 1483315200000 " +
                        "   AND m2.time >= 1483660800000 AND m2.time <= 1483747200000",
                TEST_METRIC_1, TEST_METRIC_2
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Invalid date conditions", response);
    }
}
