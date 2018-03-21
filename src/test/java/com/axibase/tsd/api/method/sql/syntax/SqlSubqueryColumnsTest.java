package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlSubqueryColumnsTest extends SqlTest {
    private String TEST_ENTITY;
    private String TEST_METRIC;

    @BeforeTest
    public void prepareData() throws Exception {
        TEST_ENTITY = entity();
        TEST_METRIC = metric();

        Series series = new Series(TEST_ENTITY, TEST_METRIC, "tag1", "value1", "tag2", "value2");
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00Z", 1),
                Sample.ofDateInteger("2017-01-02T00:00:00Z", 2),
                Sample.ofDateInteger("2017-01-03T00:00:00Z", 3)
        );

        insertSeriesCheck(series);
    }

    @Issue("5070")
    @Test
    public void testAllColumns() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, time, value, tags " +
                "FROM ( " +
                "   SELECT entity, datetime, time, value, tags " +
                "   FROM \"%s\")", TEST_METRIC);

        String[][] expectedRows = new String[][] {
                {TEST_ENTITY, "2017-01-01T00:00:00.000Z", "1483228800000", "1", "tag1=value1;tag2=value2"},
                {TEST_ENTITY, "2017-01-02T00:00:00.000Z", "1483315200000", "2", "tag1=value1;tag2=value2"},
                {TEST_ENTITY, "2017-01-03T00:00:00.000Z", "1483401600000", "3", "tag1=value1;tag2=value2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testAllColumnsWithAliases() {
        String sqlQuery = String.format(
                "SELECT \"entity_alias\", \"datetime_alias\", \"time_alias\", \"value_alias\", \"tag1_alias\", \"tag2_alias\" " +
                        "FROM ( " +
                        "   SELECT " +
                        "       entity AS \"entity_alias\"," +
                        "       datetime AS \"datetime_alias\", " +
                        "       time AS \"time_alias\", " +
                        "       value AS \"value_alias\", " +
                        "       tags.tag1 AS \"tag1_alias\", " +
                        "       tags.tag2 AS \"tag2_alias\" " +
                        "   FROM \"%s\")", TEST_METRIC);

        String[][] expectedRows = new String[][] {
                {TEST_ENTITY, "2017-01-01T00:00:00.000Z", "1483228800000", "1", "value1", "value2"},
                {TEST_ENTITY, "2017-01-02T00:00:00.000Z", "1483315200000", "2", "value1", "value2"},
                {TEST_ENTITY, "2017-01-03T00:00:00.000Z", "1483401600000", "3", "value1", "value2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testTagsColumns() {
        String sqlQuery = String.format(
                "SELECT tags.tag1, tags.tag2 " +
                        "FROM ( " +
                        "   SELECT tags.tag1, tags.tag2 " +
                        "   FROM \"%s\")", TEST_METRIC);

        String[][] expectedRows = new String[][] {
                {"value1", "value2"},
                {"value1", "value2"},
                {"value1", "value2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testTagsColumnsWithAliases() {
        String sqlQuery = String.format(
                "SELECT \"alias1\", \"alias2\" " +
                        "FROM ( " +
                        "   SELECT tags.tag1 AS \"alias1\", tags.tag2 AS \"alias2\" " +
                        "   FROM \"%s\")", TEST_METRIC);

        String[][] expectedRows = new String[][] {
                {"value1", "value2"},
                {"value1", "value2"},
                {"value1", "value2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testExtraValueColumn() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, value " +
                        "FROM ( " +
                        "   SELECT entity, datetime " +
                        "   FROM \"%s\")", TEST_METRIC);

        assertBadSqlRequest("Unexpected expression: 'value'", sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testExtraAliasColumn() {
        String sqlQuery = String.format(
                "SELECT entity, \"abc\" " +
                        "FROM ( " +
                        "   SELECT entity " +
                        "   FROM \"%s\")", TEST_METRIC);

        assertBadSqlRequest("Unexpected expression: 'abc'", sqlQuery);
    }

    @Issue("5070")
    @Test
    public void testDatetimeAlias() {
        String sqlQuery = String.format(
                "SELECT entity, \"abc\" " +
                        "FROM ( " +
                        "   SELECT  entity, datetime AS \"abc\" " +
                        "   FROM \"%s\")", TEST_METRIC);

        String[][] expectedRows = new String[][] {
                {TEST_ENTITY, "2017-01-01T00:00:00.000Z"},
                {TEST_ENTITY, "2017-01-02T00:00:00.000Z"},
                {TEST_ENTITY, "2017-01-03T00:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
