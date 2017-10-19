package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereTagLikeAnyOfTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    private static void prepareData() throws Exception {
        String entity = entity();

        Series series1 = new Series(entity, METRIC_NAME, "tag", "ab");
        series1.addSamples(Mocks.SAMPLE);

        Series series2 = new Series(entity, METRIC_NAME, "tag", "ac");
        series2.addSamples(Mocks.SAMPLE);

        Series series3 = new Series(entity, METRIC_NAME, "tag", "ad");
        series3.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    @Issue("4034")
    @Test
    public void testWhereTagsAny() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM \"%s\" " +
                        "WHERE tags.tag LIKE '%%'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal LIKE pattern",
                expectedRows, sqlQuery);
    }

    @Issue("4034")
    @Test
    public void testWhereTagsAnyOrAny() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM \"%s\" " +
                        "WHERE tags.tag LIKE '%%' " +
                        "OR tags.tag LIKE '%%'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal LIKE pattern twice",
                expectedRows, sqlQuery);
    }

    @Issue("4034")
    @Test
    public void testWhereTagsAnyOrAnyOf() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM \"%s\" " +
                        "WHERE tags.tag LIKE '%%' " +
                        "OR tags.tag LIKE '%%b'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal and custom LIKE pattern",
                expectedRows, sqlQuery);
    }
}
