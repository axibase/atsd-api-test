package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlInsertNonExistentMetricTest extends SqlTest {
    private static final String METRIC_1 = Mocks.metric();
    private static final String METRIC_2 = Mocks.metric();
    private static final String METRIC_3 = Mocks.metric().replaceAll("-", " "); //metric is used to check metric creation with whitespaces
    private static final String METRIC_4 = Mocks.metric();

    private static final String ENTITY = Mocks.entity();
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    @BeforeClass
    public void prepareData() throws Exception{
        EntityMethod.createOrReplaceEntityCheck(ENTITY);
    }

    @Test(
            description = "Tests that metric is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testMetricCreationWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                , METRIC_1, ENTITY, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent metric failed!", sqlQuery);
        Checker.check(new MetricCheck(
                new Metric()
                        .setName(METRIC_1)
        ));
    }

    @Test(
            description = "Tests that metric with label is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithLabelWhenInserting() {
        String label = Mocks.LABEL;
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, metric.label) VALUES('%s', '%s', %d, '%s')"
                , METRIC_2, ENTITY, ISO_TIME, VALUE, label);
        assertOkRequest("Insertion of series with nonexistent metric with label failed!", sqlQuery);
        Checker.check(new MetricCheck(
                new Metric()
                        .setName(METRIC_2)
                        .setLabel(label)));
    }

    @Test(
            description = "Tests that metric with whitespaces in is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithWhitespacesWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                , METRIC_3, ENTITY, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent with whitespaces in name metric failed!", sqlQuery);
        Checker.check(new MetricCheck(
                new Metric()
                        .setName(METRIC_3.replaceAll(" ", "_"))));
    }

    @Test(
            description = "Tests that metric is created after INSERT INTO command with atsd_series if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithAtsdSeriesWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"atsd_series\"(entity, datetime, \"%s\") VALUES('%s', '%s', %d)"
                , METRIC_4, ENTITY, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent via atsd_series metric failed!", sqlQuery);
        Checker.check(new MetricCheck(
                new Metric()
                        .setName(METRIC_4)));
    }
}
