package com.axibase.tsd.api.method.sql.clause.insert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class SqlInsertNonExistentEntityTest extends SqlTest {
    private final String entity1 = Mocks.entity();
    private final String entity2 = Mocks.entity();
    private final String entity3 = Mocks.entity().replaceAll("-", " "); //entity is used to check entity creation with whitespaces
    private final String entity4 = Mocks.entity();

    private final String metric = Mocks.metric();
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final InsertionType insertionType;

    @Factory(dataProvider = "insertionType", dataProviderClass = InsertionType.class)
    public SqlInsertNonExistentEntityTest(InsertionType insertionType) {
        this.insertionType = insertionType;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(metric);
    }

    @Test(
            description = "Tests that entity is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWhenInserting() {
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity1, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(entity1)));
    }

    @Test(
            description = "Tests that entity with label is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithLabelWhenInserting() {
        String label = Mocks.LABEL;
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity2, "datetime", ISO_TIME, "value", VALUE, "entity.label", label));
        assertOkRequest("Insertion of series with nonexistent entity with label failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(entity2)
                        .setLabel(label)));
    }

    @Test(
            description = "Tests that entity with whitespaces in is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithWhitespacesWhenInserting() {
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity3, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent with whitespaces in name entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(entity3.replaceAll(" ", "_"))));
    }

    @Test(
            description = "Tests that entity is created after INSERT INTO command with atsd_series if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithAtsdSeriesWhenInserting() {
        String sqlQuery = insertionType.insertionQuery("atsd_series",
                ImmutableMap.of("entity", entity4, "datetime", ISO_TIME, "\"metric\"", VALUE));
        assertOkRequest("Insertion of series with nonexistent via atsd_series entity failed!", sqlQuery);
        Checker.check(new EntityCheck(
                new Entity()
                        .setName(entity4)));
    }
}
