package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlInsertNonExistentEntityTest extends SqlTest {
    private static final String ENTITY_1 = Mocks.entity();
    private static final String ENTITY_2 = Mocks.entity();
    private static final String ENTITY_3 = Mocks.entity().replaceAll("-", " "); //entity is used to check entity creation with whitespaces
    private static final String ENTITY_4 = Mocks.entity();

    private static final String METRIC = Mocks.metric();
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    @BeforeClass
    public void prepareData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(METRIC);
    }

    @Test(
            description = "Tests that entity is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                , METRIC, ENTITY_1, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(ENTITY_1)));
    }

    @Test(
            description = "Tests that entity with label is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithLabelWhenInserting() {
        String label = Mocks.LABEL;
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value, entity.label) VALUES('%s', '%s', %d, '%s')"
                , METRIC, ENTITY_2, ISO_TIME, VALUE, label);
        assertOkRequest("Insertion of series with nonexistent entity with label failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(ENTITY_2)
                        .setLabel(label)));
    }

    @Test(
            description = "Tests that entity with whitespaces in is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithWhitespacesWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"%s\"(entity, datetime, value) VALUES('%s', '%s', %d)"
                , METRIC, ENTITY_3, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent with whitespaces in name entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(ENTITY_3.replaceAll(" ", "_"))));
    }

    @Test(
            description = "Tests that entity is created after INSERT INTO command with atsd_series if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithAtsdSeriesWhenInserting() {
        String sqlQuery = String.format("INSERT INTO \"atsd_series\"(entity, datetime, \"%s\") VALUES('%s', '%s', %d)"
                , METRIC, ENTITY_4, ISO_TIME, VALUE);
        assertOkRequest("Insertion of series with nonexistent via atsd_series entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(ENTITY_4)));
    }
}
