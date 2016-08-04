package com.axibase.tsd.api.method.sql.where;

import com.axibase.tsd.api.method.entitygroup.EntityGroupsMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Igor Shmagrinskiy
 */
public class SqlEntityGroupsWhereClauseTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-entity-groups-where-clause-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_GROUP1_NAME = TEST_ENTITY_NAME + "-group-1";
    private static final String TEST_ENTITY_GROUP2_NAME = TEST_ENTITY_NAME + "-group-2";
    private static final String TEST_CASE_SENSITIVITY_GROUP_NAME = "SQL-entity-groups-where-clause-entity-group";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        sendSamplesToSeries(series,
                new Sample("2016-07-14T15:00:07.000Z", "0"));
        EntityGroupsMethod.createOrReplaceEntityGroup(TEST_ENTITY_GROUP1_NAME);
        EntityGroupsMethod.createOrReplaceEntityGroup(TEST_ENTITY_GROUP2_NAME);
        EntityGroupsMethod.createOrReplaceEntityGroup(TEST_CASE_SENSITIVITY_GROUP_NAME);
    }


    @BeforeMethod
    public void clearEntityGroups() throws InterruptedException {
        List<String> emptyList = new ArrayList<>();
        EntityGroupsMethod.setEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, emptyList);
        EntityGroupsMethod.setEntitiesToEntityGroup(TEST_ENTITY_GROUP2_NAME, emptyList);
        EntityGroupsMethod.setEntitiesToEntityGroup(TEST_CASE_SENSITIVITY_GROUP_NAME, emptyList);
    }


    /*
    Following tests related to #3020 issue
     */

    /**
     * Issue #3020
     */
    @Test
    public void testEntityGroupsInOneElementSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('" + TEST_ENTITY_GROUP1_NAME + "')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3020
     */
    @Test
    public void testEntityGroupsNotInOneElementSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE NOT entity.groups IN ('" + TEST_ENTITY_GROUP1_NAME + "')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList();

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3020
     */
    @Test
    public void testInEntityGroupsContainOneElement() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE '" + TEST_ENTITY_GROUP1_NAME + "' IN entity.groups\n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3020
     */
    @Test
    public void testNotInEntityGroupsContainOneElement() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE '" + TEST_ENTITY_GROUP1_NAME + "' NOT IN entity.groups\n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList();

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testOneEntityGroupInThreeElementSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('" + TEST_ENTITY_GROUP1_NAME + "', 'group', '" + TEST_ENTITY_GROUP2_NAME + "')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testEntityGroupsNotInSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('" + TEST_ENTITY_GROUP1_NAME + "', 'group', '" + TEST_ENTITY_GROUP2_NAME + "')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testTwoEntityGroupsIntersectingOneElementSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));

        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP2_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('" + TEST_ENTITY_GROUP1_NAME + "')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME + ';' + TEST_ENTITY_GROUP2_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testTwoEntityGroupsNotIntersectingOneElementSet() {
        EntityGroupsMethod
                .addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));

        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP2_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('group-1')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testZeroEntityGroupsIntersectingTwoElementSet() {
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('group-1', 'group-2')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testZeroEntityGroupsNotIntersectingTwoElementSet() {
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups NOT IN ('group-1', 'group-2')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", "sql-entity-groups-where-clause-entity", "0", "null")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testZeroEntityGroupsNotIntersectingOneElementSet() {
        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('group-1')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testEntityGroupsInCaseSensitivitySet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('SQL-entity-groups-where-clause-entity-group-1')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
        );

        assertTableRows(expectedRows, resultTable);
    }

    /**
     * Issue #3020
     */
    @Test
    public void testEntityGroupsNotInCaseSensitivitySet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_ENTITY_GROUP1_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups NOT IN ('SQL-entity-groups-where-clause-entity-group-1')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_ENTITY_GROUP1_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testCaseSensitivityEntityGroupsInSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_CASE_SENSITIVITY_GROUP_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups IN ('SQL-entity-groups-where-clause-entity-group')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-14T15:00:07.000Z", TEST_ENTITY_NAME, "0", TEST_CASE_SENSITIVITY_GROUP_NAME)
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issue #3020
     */
    @Test
    public void testCaseSensitivityEntityGroupsNotInSet() {
        EntityGroupsMethod.addEntitiesToEntityGroup(TEST_CASE_SENSITIVITY_GROUP_NAME, Arrays.asList(TEST_ENTITY_NAME));

        String sqlQuery =
                "SELECT datetime, entity, value, entity.groups FROM '" + TEST_METRIC_NAME + "'\n" +
                        "WHERE entity.groups NOT IN ('SQL-entity-groups-where-clause-entity-group')  \n" +
                        "AND datetime = '2016-07-14T15:00:07.000Z'\n";
        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
        );

        assertTableRows(expectedRows, resultTable);

    }
}
