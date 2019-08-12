package com.axibase.tsd.api.method.sql.clause.insertandupdate;

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
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final int VALUE = Mocks.INT_VALUE;

    private final InsertionType insertionType;
    private final String metric = Mocks.metric();

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
        Entity entity = new Entity(Mocks.entity());
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity.getName(), "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent entity failed!", sqlQuery);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests that entity with label is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithLabelWhenInserting() {
        Entity entity = new Entity(Mocks.entity()).setLabel(Mocks.LABEL);
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entity.getName(), "datetime", ISO_TIME, "value", VALUE, "entity.label", entity.getLabel()));
        assertOkRequest("Insertion of series with nonexistent entity with label failed!", sqlQuery);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests that entity with whitespaces in is created after INSERT INTO command if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithWhitespacesWhenInserting() {
        String entityNameWithWhiteSpaces = Mocks.entity().replaceAll("-", " ");
        Entity entity = new Entity(entityNameWithWhiteSpaces.replaceAll(" ", "_"));
        String sqlQuery = insertionType.insertionQuery(metric,
                ImmutableMap.of("entity", entityNameWithWhiteSpaces, "datetime", ISO_TIME, "value", VALUE));
        assertOkRequest("Insertion of series with nonexistent entity with whitespaces in name entity failed!", sqlQuery);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests that entity is created after INSERT INTO command with atsd_series if it did not exist."
    )
    @Issue("5962")
    public void testEntityCreationWithAtsdSeriesWhenInserting() {
        Entity entity = new Entity(Mocks.entity());
        String sqlQuery = insertionType.insertionQuery("atsd_series",
                ImmutableMap.of("entity", entity.getName(), "datetime", ISO_TIME, String.format("\"%s\"", metric), VALUE));
        assertOkRequest("Insertion of series with nonexistent entity via atsd_series failed!", sqlQuery);
        Checker.check(new EntityCheck(entity));
    }
}
