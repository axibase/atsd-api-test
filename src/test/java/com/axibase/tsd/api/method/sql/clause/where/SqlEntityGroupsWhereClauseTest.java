package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlEntityGroupsWhereClauseTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-entity-groups-where-clause-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_ENTITY_NAME + "-group-1";
    private static final String TEST_ENTITY_GROUP2_NAME = TEST_ENTITY_NAME + "-group-2";
    private static final String TEST_CASE_SENSITIVITY_GROUP_NAME = "SQL-entity-groups-where-clause-entity-group";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            addSamples(Sample.ofDateInteger("2016-07-14T15:00:07.000Z", 0));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));

        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP1_NAME));
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_ENTITY_GROUP2_NAME));
        EntityGroupMethod.createOrReplaceEntityGroup(new EntityGroup(TEST_CASE_SENSITIVITY_GROUP_NAME));
    }


    @BeforeMethod
    public void clearEntityGroups() throws InterruptedException {
        List<String> emptyList = Collections.emptyList();
        EntityGroupMethod.setEntities(TEST_ENTITY_GROUP1_NAME, emptyList);
        EntityGroupMethod.setEntities(TEST_ENTITY_GROUP2_NAME, emptyList);
        EntityGroupMethod.setEntities(TEST_CASE_SENSITIVITY_GROUP_NAME, emptyList);
    }


    @Issue("3020")
    @Test
    public void testEntityGroupsInOneElementSet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s')   %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3020")
    @Test
    public void testEntityGroupsNotInOneElementSet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE NOT entity.groups IN ('%s')   %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3020")
    @Test
    public void testInEntityGroupsContainOneElement() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE '%s' IN entity.groups %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3020")
    @Test
    public void testNotInEntityGroupsContainOneElement() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE '%s' NOT IN entity.groups %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testOneEntityGroupInThreeElementSet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s', 'group', '%s')   %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME, TEST_ENTITY_GROUP2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testEntityGroupsNotInSet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s', 'group', '%s')   %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME, TEST_ENTITY_GROUP2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testTwoEntityGroupsIntersectingOneElementSet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_ENTITY_GROUP1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME + ';' + TEST_ENTITY_GROUP2_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testTwoEntityGroupsNotIntersectingOneElementSet() {
        EntityGroupMethod
                .addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP2_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('group-1')   %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testZeroEntityGroupsIntersectingTwoElementSet() {
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('group-1', 'group-2') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testZeroEntityGroupsNotIntersectingTwoElementSet() {
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups NOT IN ('group-1', 'group-2') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", "null")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testZeroEntityGroupsNotIntersectingOneElementSet() {
        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('group-1') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testEntityGroupsInCaseSensitivitySet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_CASE_SENSITIVITY_GROUP_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3020")
    @Test
    public void testEntityGroupsNotInCaseSensitivitySet() {
        EntityGroupMethod.addEntities(TEST_ENTITY_GROUP1_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups NOT IN ('%s') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_CASE_SENSITIVITY_GROUP_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testCaseSensitivityEntityGroupsInSet() {
        EntityGroupMethod.addEntities(TEST_CASE_SENSITIVITY_GROUP_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %n" +
                        "WHERE entity.groups IN ('%s') %nAND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_CASE_SENSITIVITY_GROUP_NAME
        );
        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_CASE_SENSITIVITY_GROUP_NAME)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3020")
    @Test
    public void testCaseSensitivityEntityGroupsNotInSet() {
        EntityGroupMethod.addEntities(TEST_CASE_SENSITIVITY_GROUP_NAME, Collections.singletonList(TEST_ENTITY_NAME));

        String sqlQuery = String.format(
                "SELECT datetime, entity, value, entity.groups FROM \"%s\" %nWHERE entity.groups NOT IN ('%s')   %n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z' %n",
                TEST_METRIC_NAME, TEST_CASE_SENSITIVITY_GROUP_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.emptyList();

        assertTableRowsExist(expectedRows, resultTable);

    }
}
