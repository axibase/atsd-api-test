package com.axibase.tsd.api.method.sql.clause.where;

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

public class WhereWithDatetimeTest extends SqlTest {
    private final String TEST_METRIC = metric();

    @BeforeTest
    public void prepareData() throws Exception {
        String entity = entity();
        Series series = new Series(entity, TEST_METRIC);
        series.addSamples(
                new Sample("2017-01-01T00:00:00Z", 1),
                new Sample("2017-01-02T00:00:00Z", 2),
                new Sample("2017-01-03T00:00:00Z", 3),
                new Sample("2017-01-04T00:00:00Z", 4)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithoutParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE " +
                        "   datetime BETWEEN '2017-01-01T00:00:00.000Z' AND '2017-01-02T00:00:00.000Z'" +
                        "    OR datetime = '2017-01-04T00:00:00.000Z'",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithoutParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE " +
                        "   time BETWEEN date_parse('2017-01-01T00:00:00.000Z') AND date_parse('2017-01-02T00:00:00.000Z')" +
                        "    OR time = date_parse('2017-01-04T00:00:00.000Z')",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE " +
                        "   (datetime BETWEEN '2017-01-01T00:00:00.000Z' AND '2017-01-02T00:00:00.000Z')" +
                        "    OR (datetime = '2017-01-04T00:00:00.000Z')",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE " +
                        "   (time BETWEEN date_parse('2017-01-01T00:00:00.000Z') AND date_parse('2017-01-02T00:00:00.000Z'))" +
                        "    OR (time = date_parse('2017-01-04T00:00:00.000Z'))",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-01T00:00:00.000Z"},
                {"2017-01-02T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithNot() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT datetime = '2017-01-01T00:00:00.000Z'",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithNot() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT time = date_parse('2017-01-01T00:00:00.000Z')",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithNotParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT (datetime = '2017-01-01T00:00:00.000Z')",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithNotParentheses() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT (time = date_parse('2017-01-01T00:00:00.000Z'))",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithNotComplexCondition() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT ((datetime = '2017-01-01T00:00:00.000Z') OR (datetime = '2017-01-01T00:00:00.000Z'))",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithNotComplexCondition() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT ((time = date_parse('2017-01-01T00:00:00.000Z')) OR (time = date_parse('2017-01-01T00:00:00.000Z')))",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }

    /**
     * #4272
     */
    @Test
    public void testDatetimeFilterWithNotBetween() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT (datetime BETWEEN '2017-01-01T00:00:00.000Z' AND '2017-01-02T00:00:00.000Z')",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-03T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testTimeFilterWithNotBetween() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT (time BETWEEN date_parse('2017-01-01T00:00:00.000Z') AND date_parse('2017-01-02T00:00:00.000Z'))",
                TEST_METRIC
        );

        String[][] expectedRows = new String[][] {
                {"2017-01-03T00:00:00.000Z"},
                {"2017-01-04T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4272
     */
    @Test
    public void testMixedFilter() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%s' " +
                        "WHERE NOT ((datetime = '2017-01-01T00:00:00.000Z') OR (time = date_parse('2017-01-01T00:00:00.000Z')))",
                TEST_METRIC
        );

        Response response = SqlMethod.queryResponse(sqlQuery);

        assertBadRequest("Not equal operation is not supported for time and datetime", response);
    }
}
