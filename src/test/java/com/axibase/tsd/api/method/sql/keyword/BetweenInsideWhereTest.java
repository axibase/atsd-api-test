package com.axibase.tsd.api.method.sql.keyword;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class BetweenInsideWhereTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.addSamples(
                Sample.ofDateInteger("2017-03-09T12:00:00.000Z", 1),
                Sample.ofDateInteger("2017-03-10T12:00:00.000Z", 2),
                Sample.ofDateInteger("2017-03-11T12:00:00.000Z", 3),
                Sample.ofDateInteger("2017-03-12T12:00:00.000Z", 4)
        );

        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("4014")
    @Test(description = "test datetime BETWEEN")
    public void checkIfBetweenSuccededByAndWorks() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE datetime BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z' AND value != 0",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"3"}
        };

        assertSqlQueryRows("BETWEEN fails when joined with another condition with AND", expectedRows, sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test datetime NOT BETWEEN")
    public void testDatetimeNotBetween() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE datetime NOT BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"4"}
        };

        assertSqlQueryRows("datetime NOT BETWEEN filter returned incorrect result", expectedRows, sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test NOT datetime BETWEEN")
    public void testNotDatetimeBetween() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE NOT datetime BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z'",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"4"}
        };

        assertSqlQueryRows("NOT datetime BETWEEN filter returned incorrect result", expectedRows, sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test datetime NOT BETWEEN complex condition AND")
    public void testDatetimeNotBetweenComplexAnd() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE datetime NOT BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z' AND value = 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(
                "complex datetime NOT BETWEEN filter with AND returned incorrect result",
                expectedRows,
                sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test datetime NOT BETWEEN complex condition OR")
    public void testDatetimeNotBetweenComplexOR() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE datetime NOT BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z' OR value = 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"3"},
                {"4"}
        };

        assertSqlQueryRows(
                "complex datetime NOT BETWEEN filter with OR returned incorrect result",
                expectedRows,
                sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test datetime NOT BETWEEN complex condition AND with parentheses")
    public void testDatetimeNotBetweenComplexAndParentheses() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE (datetime NOT BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z') AND (value = 1)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"}
        };

        assertSqlQueryRows(
                "complex datetime NOT BETWEEN filter with AND in parentheses returned incorrect result",
                expectedRows,
                sqlQuery);
    }

    @Issue("4555")
    @Test(description = "test datetime NOT BETWEEN complex condition OR with parentheses")
    public void testDatetimeNotBetweenComplexORParentheses() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\" WHERE (datetime NOT BETWEEN '2017-03-10T12:00:00.000Z' " +
                        "AND '2017-03-11T12:00:00.000Z') OR (value = 3)",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"3"},
                {"4"}
        };

        assertSqlQueryRows(
                "complex datetime NOT BETWEEN filter with OR in parentheses returned incorrect result",
                expectedRows,
                sqlQuery);
    }
}
