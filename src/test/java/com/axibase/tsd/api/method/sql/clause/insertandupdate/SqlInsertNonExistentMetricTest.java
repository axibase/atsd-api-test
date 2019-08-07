package com.axibase.tsd.api.method.sql.clause.insertandupdate;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SqlInsertNonExistentMetricTest extends SqlTest {
    private final String entity = Mocks.entity();
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertNonExistentMetricTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        EntityMethod.createOrReplaceEntityCheck(entity);
    }

    @Test(
            description = "Tests that metric is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testMetricCreationWhenInserting() {
        Metric metric = new Metric(Mocks.metric());
        String sqlQuery = insertionType.insertionQuery(metric.getName(),
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent metric failed!", sqlQuery);
        Checker.check(new MetricCheck(metric));
    }

    @Test(
            description = "Tests that metric with label is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithLabelWhenInserting() {
        Metric metric = new Metric(Mocks.entity()).setLabel(Mocks.LABEL);
        String sqlQuery = insertionType.insertionQuery(metric.getName(),
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", VALUE, "metric.label", metric.getLabel()));
        assertOkRequest("Insertion of series with nonexistent metric with label failed!", sqlQuery);
        Checker.check(new MetricCheck(metric));
    }

    @Test(
            description = "Tests that metric with whitespaces in is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithWhitespacesWhenInserting() {
        String metricNameWithWhiteSpaces = Mocks.metric().replaceAll("-", " ");
        Metric metric = new Metric(metricNameWithWhiteSpaces.replaceAll(" ", "_"));
        String sqlQuery = insertionType.insertionQuery(metricNameWithWhiteSpaces,
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent metric with whitespaces in name failed!", sqlQuery);
        Checker.check(new MetricCheck(metric));
    }

    @Test(
            description = "Tests that metric is created after INSERT INTO command with atsd_series if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithAtsdSeriesWhenInserting() {
        Metric metric = new Metric(Mocks.metric());
        String sqlQuery = insertionType.insertionQuery("atsd_series",
                ImmutableMap.of("entity", entity, "datetime", ISO_TIME, String.format("\"%s\"", metric.getName()), VALUE));
        assertOkRequest("Insertion of series with nonexistent metric via atsd_series failed!", sqlQuery);
        Checker.check(new MetricCheck(metric));
    }
}
