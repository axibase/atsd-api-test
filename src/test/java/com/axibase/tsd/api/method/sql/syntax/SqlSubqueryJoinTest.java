package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlSubqueryJoinTest extends SqlTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME_LEFT = Mocks.metric();
    private static final String METRIC_NAME_RIGHT = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_NAME, METRIC_NAME_LEFT);
        Series series2 = new Series(ENTITY_NAME, METRIC_NAME_LEFT);
        Series series3 = new Series(ENTITY_NAME, METRIC_NAME_RIGHT);
        Series series4 = new Series(ENTITY_NAME, METRIC_NAME_RIGHT);

        series1.addTag("t1", "Tag1");
        series1.addSamples(Sample.ofDateInteger("2017-09-01T12:00:00.000Z", 1));

        series2.addSamples(
                Sample.ofDateInteger("2017-09-02T12:00:00.000Z", 2),
                Sample.ofDateInteger("2017-09-03T12:00:00.000Z", 3)
        );

        series3.addSamples(
                Sample.ofDateInteger("2017-08-31T12:00:00.000Z", 1),
                Sample.ofDateInteger("2017-09-02T12:00:00.000Z", 3)
        );

        series4.addTag("t2", "Tag2");
        series4.addSamples(Sample.ofDateInteger("2017-09-01T12:00:00.000Z", 2));

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);
    }

    @Issue("4508")
    @Test(
            description = "Test if JOIN is supported in subqueries"
    )
    public void testJoin() throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM ( " +
                        "    SELECT *, t1.value * t2.value as \"value\" " +
                        "    FROM \"%s\" t1 " +
                        "    JOIN \"%s\" t2 " +
                        ") " +
                        "ORDER BY time",
                METRIC_NAME_LEFT,
                METRIC_NAME_RIGHT
        );

        String[][] expectedRows = {
                {"2017-09-02T12:00:00.000Z", "6"}
        };

        assertSqlQueryRows("Wrong result for JOIN in subquery", expectedRows, sqlQuery);
    }

    @Issue("4508")
    @Test(
            description = "Test if JOIN USING ENTITY is supported in subqueries"
    )
    public void testJoinUsingEntity() throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM ( " +
                        "    SELECT *, t1.value * t2.value as \"value\" " +
                        "    FROM \"%s\" t1 " +
                        "    JOIN USING ENTITY \"%s\" t2 " +
                        ") " +
                        "ORDER BY time",
                METRIC_NAME_LEFT,
                METRIC_NAME_RIGHT
        );

        String[][] expectedRows = {
                {"2017-09-01T12:00:00.000Z", "2"},
                {"2017-09-02T12:00:00.000Z", "6"}
        };

        assertSqlQueryRows("Wrong result for JOIN USING ENTITY in subquery", expectedRows, sqlQuery);
    }

    @Issue("4508")
    @Test(
            description = "Test if OUTER JOIN is supported in subqueries"
    )
    public void testOuterJoin() throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, value, text " +
                        "FROM ( " +
                        "    SELECT datetime, t1.value * t2.value as \"value\", concat(t1.value, '-', t2.value) as \"text\" " +
                        "    FROM \"%s\" t1 " +
                        "    OUTER JOIN \"%s\" t2 " +
                        ") " +
                        "ORDER BY time, value",
                METRIC_NAME_LEFT,
                METRIC_NAME_RIGHT
        );

        String[][] expectedRows = {
                {"2017-08-31T12:00:00.000Z", "null", "-1"},
                {"2017-09-01T12:00:00.000Z", "null", "1-"},
                {"2017-09-01T12:00:00.000Z", "null", "-2"},
                {"2017-09-02T12:00:00.000Z", "6", "2-3"},
                {"2017-09-03T12:00:00.000Z", "null", "3-"}
        };

        assertSqlQueryRows("Wrong result for OUTER JOIN in subquery", expectedRows, sqlQuery);
    }

    @Issue("4508")
    @Test(
            description = "Test if OUTER JOIN USING ENTITY is supported in subqueries"
    )
    public void testOuterJoinUsingEntity() throws Exception {
        String sqlQuery = String.format(
                "SELECT datetime, value, text " +
                        "FROM ( " +
                        "    SELECT datetime, t1.value * t2.value as \"value\", concat(t1.value, '-', t2.value) as \"text\" " +
                        "    FROM \"%s\" t1 " +
                        "    OUTER JOIN USING ENTITY \"%s\" t2 " +
                        ") " +
                        "ORDER BY time, value",
                METRIC_NAME_LEFT,
                METRIC_NAME_RIGHT
        );

        String[][] expectedRows = {
                {"2017-08-31T12:00:00.000Z", "null", "-1"},
                {"2017-09-01T12:00:00.000Z", "2", "1-2"},
                {"2017-09-02T12:00:00.000Z", "6", "2-3"},
                {"2017-09-03T12:00:00.000Z", "null", "3-"}
        };

        assertSqlQueryRows("Wrong result for OUTER JOIN USING ENTITY in subquery", expectedRows, sqlQuery);
    }
}
