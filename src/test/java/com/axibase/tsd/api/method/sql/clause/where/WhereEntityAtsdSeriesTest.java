package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WhereEntityAtsdSeriesTest extends SqlTest {
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();
    private static final String METRIC_NAME1 = Mocks.metric();
    private static final String METRIC_NAME2 = Mocks.metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME1);
        series1.addSamples(Sample.ofDateInteger("2017-10-01T00:00:00.000Z", 1));

        Series series2 = new Series(ENTITY_NAME1, METRIC_NAME2);
        series2.addSamples(Sample.ofDateInteger("2017-10-02T00:00:00.000Z", 2));

        Series series3 = new Series(ENTITY_NAME2, METRIC_NAME1);
        series3.addSamples(Sample.ofDateInteger("2017-10-03T00:00:00.000Z", 3));

        Series series4 = new Series(ENTITY_NAME2, METRIC_NAME2);
        series4.addSamples(Sample.ofDateInteger("2017-10-04T00:00:00.000Z", 4));

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);
    }

    @Issue("4588")
    @Test(
            description = "Test that filtering only by entity is not allowed for atsd_series (using or oprator)"
    )
    public void testEntityOnlyDisallowed() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE entity = '%s'",
                ENTITY_NAME1
        );

        assertBadRequest("Wrong result when using entity filter for atsd_series",
                "Missing metric expression in the where clause", sqlQuery);
    }

    @Issue("4259")
    @Issue("4588")
    @Test(
            description = "Test that filtering only by entity is not allowed for atsd_series (using or oprator)"
    )
    public void testOrEntityDisallowed() {
        String sqlQuery = String.format(
                "SELECT * " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' OR entity = '%s'",
                METRIC_NAME1, ENTITY_NAME1
        );

        assertBadRequest("Wrong result when using entity filter for atsd_series",
                "Invalid metric expression", sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting metric and entity filters is allowed for atsd_series"
    )
    public void testAndEntityAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' AND entity = '%s'",
                METRIC_NAME1, ENTITY_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, METRIC_NAME1, "1"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by metric and entity",
                expectedRows, sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting metric and negative entity filters is allowed for atsd_series"
    )
    public void testAndNotEntityAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' AND NOT(entity = '%s')",
                METRIC_NAME1, ENTITY_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME2, METRIC_NAME1, "3"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by metric and not entity",
                expectedRows, sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting metric and entity LIKE filters is allowed for atsd_series"
    )
    public void testAndEntityLikeAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' AND entity LIKE '%s'",
                METRIC_NAME1, ENTITY_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, METRIC_NAME1, "1"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by metric and entity LIKE",
                expectedRows, sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting metric and entity REGEX filters is allowed for atsd_series"
    )
    public void testAndEntityRegexAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' AND entity REGEX '%s'",
                METRIC_NAME1, ENTITY_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, METRIC_NAME1, "1"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by metric and entity REGEX",
                expectedRows, sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting metric and one of entity filters is allowed for atsd_series"
    )
    public void testAndOrEntityAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE metric = '%s' AND (entity = '%s' OR entity = '%s') " +
                        "ORDER BY entity",
                METRIC_NAME1, ENTITY_NAME1, ENTITY_NAME2
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, METRIC_NAME1, "1"},
                {ENTITY_NAME2, METRIC_NAME1, "3"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by metric and different entities",
                expectedRows, sqlQuery);
    }

    @Issue("4588")
    @Test(
            description = "Test that uniting one of metric filters and one of entity filters " +
                    "is allowed for atsd_series"
    )
    public void testOrMetricsAndOrEntitiesAllowed() {
        String sqlQuery = String.format(
                "SELECT entity, metric, value " +
                        "FROM atsd_series " +
                        "WHERE (metric = '%s' OR metric = '%s') AND (entity = '%s' OR entity = '%s') " +
                        "ORDER BY entity, metric",
                METRIC_NAME1, METRIC_NAME2, ENTITY_NAME1, ENTITY_NAME2
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, METRIC_NAME1, "1"},
                {ENTITY_NAME1, METRIC_NAME2, "2"},
                {ENTITY_NAME2, METRIC_NAME1, "3"},
                {ENTITY_NAME2, METRIC_NAME2, "4"}
        };

        assertSqlQueryRows("Wrong result when filtering atsd_series by different metrics and different entities",
                expectedRows, sqlQuery);
    }
}
