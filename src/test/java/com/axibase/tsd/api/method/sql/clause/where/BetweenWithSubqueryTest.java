package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class BetweenWithSubqueryTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series dataSeries = new Series(entity(), METRIC_NAME);
        dataSeries.addSamples(
                Sample.ofDateIntegerText("2017-01-01T12:00:00.000Z", 1, "a"),
                Sample.ofDateIntegerText("2017-01-02T12:00:00.000Z", 2, "b"),
                Sample.ofDateIntegerText("2017-01-03T12:00:00.000Z", 3, "c"),
                Sample.ofDateIntegerText("2017-01-04T12:00:00.000Z", 4, "d"),
                Sample.ofDateIntegerText("2017-01-05T12:00:00.000Z", 5, "e"),
                Sample.ofDateIntegerText("2017-01-06T12:00:00.000Z", 6, "f"),
                Sample.ofDateIntegerText("2017-01-07T12:00:00.000Z", 7, "g")
        );

        SeriesMethod.insertSeriesCheck(dataSeries);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubqueryEmpty() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value > 7)",
                METRIC_NAME
        );

        String[][] expectedRows = {};

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN when result of subquery is empty",
                expectedRows, sqlQuery);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubqueryLeftBound() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value = 4)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"4"},
                {"5"},
                {"6"},
                {"7"}
        };

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN with single row from subquery",
                expectedRows, sqlQuery);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubqueryBothBounds() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value = 2 OR value = 4)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"3"},
                {"4"}
        };

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN with two rows from subquery",
                expectedRows, sqlQuery);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubqueryMultipleRanges() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value %% 2 != 0)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                /* first range */
                {"1"},
                {"2"},
                {"3"},
                /* second range */
                {"5"},
                {"6"},
                {"7"}
        };

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN with multiple ranges from subquery",
                expectedRows, sqlQuery);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubqueryAggregation() {
        String sqlQuery = String.format(
                "SELECT avg(value), first(value), last(value), count(value) FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value %% 2 != 0)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"4", "1", "7", "6"}
        };

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN with subquery and further aggregation",
                expectedRows, sqlQuery);
    }

    @Issue("4086")
    @Test
    public void testBetweenSubquerySelectText() {
        String sqlQuery = String.format(
                "SELECT datetime, text FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN " +
                        "(SELECT datetime FROM \"%1$s\" WHERE value %% 2 != 0)",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-01-01T12:00:00.000Z", "a"},
                {"2017-01-02T12:00:00.000Z", "b"},
                {"2017-01-03T12:00:00.000Z", "c"},
                {"2017-01-05T12:00:00.000Z", "e"},
                {"2017-01-06T12:00:00.000Z", "f"},
                {"2017-01-07T12:00:00.000Z", "g"},
        };

        assertSqlQueryRows("Wrong result for WHERE ... BETWEEN with subquery when selecting text",
                expectedRows, sqlQuery);
    }
}
