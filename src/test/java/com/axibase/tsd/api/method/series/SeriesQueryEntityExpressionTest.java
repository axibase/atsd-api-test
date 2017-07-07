package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * #3612
 */
public class SeriesQueryEntityExpressionTest extends SeriesMethod {

    private static final String METRIC_NAME = "m-test-entity-expression-001";
    private static final String PROPERTY_TYPE = "test-entity-expression-001";
    private static final String ENTITY_NAME_PREFIX = "e-test-entity-expression-";
    private static final String ENTITY_GROUP_NAME = "test-entity-expression-001";
    private static final String FIXED_ENTITY_NAME = entityNameWithPrefix("asdef001");


    private static final Metric METRIC = new Metric(METRIC_NAME);
    private static final List<Property> PROPERTIES = new LinkedList<>();
    private static final HashSet<String> ALL_ENTITIES = new HashSet<>();
    private static final HashSet<String> ENTITIES_IN_GROUP = new HashSet<>();
    private static final List<Series> SERIES_LIST = new ArrayList<>();
    private static final String TEST_DATASET_DESCRIPTION;

    static {
        {
            Property property = createTestProperty("asdef001");
            property.addTag("group", "hello");
            property.addTag("multitag", "one");
            property.addKey("testkey", "test");
            property.addKey("otherkey", "other");
            PROPERTIES.add(property);

            ENTITIES_IN_GROUP.add(property.getEntity());
        }

        {
            Property property = createTestProperty("asdef002");
            property.addTag("group", "hell");
            property.addKey("testkey", "test");
            PROPERTIES.add(property);

            ENTITIES_IN_GROUP.add(property.getEntity());
        }

        {
            Property property = createTestProperty("asdef003");
            property.addKey("otherkey", "other");
            PROPERTIES.add(property);

            ENTITIES_IN_GROUP.add(property.getEntity());
        }

        {
            Property property = createTestProperty("asdef004");
            property.addTag("group", "main");
            property.addTag("multitag", "other");

            PROPERTIES.add(property);
        }

        {
            Property property = createTestProperty("asdef005");
            property.addTag("group", "foo");
            property.addKey("testkey", "test");

            PROPERTIES.add(property);
        }

        for (Property prop: PROPERTIES) {
            ALL_ENTITIES.add(prop.getEntity());
        }

        for (String entityName: ALL_ENTITIES) {
            SERIES_LIST.add(createTestSeries(entityName));
        }

    }

    static {
        StringBuilder testDatasetDescriptionBuilder = new StringBuilder();
        testDatasetDescriptionBuilder.append("\n=====================================");
        testDatasetDescriptionBuilder.append("\n              Test data              ");
        testDatasetDescriptionBuilder.append("\n=====================================\n");

        testDatasetDescriptionBuilder.append(String.format("Metric:%n"));
        testDatasetDescriptionBuilder.append(String.format("%s%n%n", TestUtil.prettyPrint(METRIC)));

        testDatasetDescriptionBuilder.append(String.format("Series: [%n"));
        for (Series series: SERIES_LIST) {
            testDatasetDescriptionBuilder.append(String.format("%s%n", TestUtil.prettyPrint(series)));
        }
        testDatasetDescriptionBuilder.append(String.format("]%n%n"));

        testDatasetDescriptionBuilder.append(String.format("Properties: [%n"));
        for (Property property: PROPERTIES) {
            testDatasetDescriptionBuilder.append(String.format("%s%n", TestUtil.prettyPrint(property)));
        }
        testDatasetDescriptionBuilder.append(String.format("]%n%n"));

        testDatasetDescriptionBuilder.append(String.format("Entities in group %s: [%n", ENTITY_GROUP_NAME));
        for (String entity: ENTITIES_IN_GROUP) {
            testDatasetDescriptionBuilder.append(String.format("  %s,%n", entity));
        }
        testDatasetDescriptionBuilder.append(String.format("]%n%n"));

        TEST_DATASET_DESCRIPTION = testDatasetDescriptionBuilder.toString();
    }

    @BeforeClass
    public static void createTestData() throws Exception {
        // Create metric
        MetricMethod.createOrReplaceMetricCheck(METRIC);

        // Create series
        for (Series series: SERIES_LIST) {
            SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        }

        // Create properties
        for (Property property: PROPERTIES) {
            PropertyMethod.insertPropertyCheck(property);
        }

        // Create entity group
        EntityGroup group = new EntityGroup(ENTITY_GROUP_NAME);
        EntityGroupMethod.createOrReplaceEntityGroupCheck(group);
        EntityGroupMethod.addEntities(ENTITY_GROUP_NAME, false, new ArrayList<>(ENTITIES_IN_GROUP));

    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "entityExpressionProviderForWildcardEntity")
    public static Object[][] provideEntityExpressionForWildcardEntity() {
        return new Object[][] {
            // Contains method
            {"property_values('" + PROPERTY_TYPE + "::name').contains('asdef001')", getPrefixedSet("asdef001")},
            {"property_values('" + PROPERTY_TYPE + "::group').contains('hell')", getPrefixedSet("asdef002")},

            // Matches method
            {"matches('asdef*', property_values('" + PROPERTY_TYPE + "::name'))", ALL_ENTITIES},
            {"matches('*', property_values('" + PROPERTY_TYPE + "::name'))", ALL_ENTITIES},
            {"matches('*def0*', property_values('" + PROPERTY_TYPE + "::name'))", ALL_ENTITIES},
            {"matches('*001', property_values('" + PROPERTY_TYPE + ":testkey=test:name'))", getPrefixedSet("asdef001")},
            {"matches('*', property_values('" + PROPERTY_TYPE + ":testkey=test,otherkey=other:name'))", getPrefixedSet("asdef001")},
            {"matches('*', property_values('" + PROPERTY_TYPE + ":otherkey=other:name'))", getPrefixedSet("asdef001", "asdef003")},
            {"matches('hel*', property_values('" + PROPERTY_TYPE + "::group'))", getPrefixedSet("asdef001", "asdef002")},

            // IsEmpty method
            {"property_values('" + PROPERTY_TYPE + "::badtag').isEmpty()", ALL_ENTITIES},
            {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').isEmpty()", getPrefixedSet("asdef003", "asdef004")},
            {"property_values('" + PROPERTY_TYPE + "::group').isEmpty()", getPrefixedSet("asdef003")},

            // Property method
            {"property('not-"+ PROPERTY_TYPE +"::name') = ''", ALL_ENTITIES},
            {"property('" + PROPERTY_TYPE + "::badtag') = ''", ALL_ENTITIES},
            {"property('"+ PROPERTY_TYPE +":testkey=test:name') = ''", getPrefixedSet("asdef003", "asdef004")},
            {"property('"+ PROPERTY_TYPE +"::name') = 'asdef001'", getPrefixedSet("asdef001")},
            {"property('"+ PROPERTY_TYPE +"::multitag') = 'other'", getPrefixedSet("asdef004")},
            {"property('"+ PROPERTY_TYPE +"::group') LIKE 'hel*'", getPrefixedSet("asdef001", "asdef002")}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "entityExpressionProviderForWildcardEntity")
    public static void testEntityExpressionWithWildcardEntity(String expression, HashSet<String> expectedEntities) throws Exception {
        SeriesQuery query = createTestQuery("*");
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> receivedEntities = new HashSet<>();
        for (Series series: result) {
            receivedEntities.add(series.getEntity());
        }

        assertEquals(formatErrorMsg("Wrong result entity set", expression), expectedEntities, receivedEntities);
        for (Series series: result) {
            List<Sample> seriesData = series.getData();
            assertEquals(formatErrorMsg("Wrong number of data entries", expression), 1, seriesData.size());
            assertEquals(formatErrorMsg("Wrong data received", expression), Mocks.SAMPLE, seriesData.get(0));
        }
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "emptyResultEntityExpressionProvider")
    public static Object[][] provideEmptyResultEntityExpression() {
        return new Object[][] {
                // Contains method
                {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').contains('asdef003')"},
                {"property_values('" + PROPERTY_TYPE + "::name').contains('lolololololololo002')"},

                // Matches method
                {"matches('de', property_values('" + PROPERTY_TYPE + "::name'))"},

                // IsEmpty method
                {"property_values('" + PROPERTY_TYPE + "::name').isEmpty()"},

                // Property method
                {"property('"+ PROPERTY_TYPE +"::name') = ''"},

                {"null"}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "emptyResultEntityExpressionProvider")
    public static void testEmptyResultEntityExpressionWithWildcardEntity(String expression) throws Exception {
        SeriesQuery query = createTestQuery("*");
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);

        assertEquals(formatErrorMsg("Dummy result is not present", expression), 1, result.size());
        Series series = result.get(0);
        assertEquals(formatErrorMsg("Dummy result entity name", expression), query.getEntity(), series.getEntity());
        assertEquals(formatErrorMsg("Dummy result data size", expression), 0, series.getData().size());
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "entityExpressionProviderForFixedEntity")
    public static Object[][] provideEntityExpressionForFixedEntity() {
        return new Object[][] {
                // Contains method
                {"property_values('" + PROPERTY_TYPE + "::name').contains('asdef001')"},

                // Matches method
                {"matches('asdef*', property_values('" + PROPERTY_TYPE + "::name'))"},
                {"matches('*', property_values('" + PROPERTY_TYPE + "::name'))"},
                {"matches('*def0*', property_values('" + PROPERTY_TYPE + "::name'))"},
                {"matches('*001', property_values('" + PROPERTY_TYPE + ":testkey=test:name'))"},
                {"matches('*', property_values('" + PROPERTY_TYPE + ":testkey=test,otherkey=other:name'))"},
                {"matches('hel*', property_values('" + PROPERTY_TYPE + "::group'))"},

                // IsEmpty method
                {"property_values('" + PROPERTY_TYPE + "::badtag').isEmpty()"},

                // Property method
                {"property('not-"+ PROPERTY_TYPE +"::name') = ''"},
                {"property('" + PROPERTY_TYPE + "::badtag') = ''"},
                {"property('"+ PROPERTY_TYPE +"::name') = 'asdef001'"},
                {"property('"+ PROPERTY_TYPE +"::group') LIKE 'hel*'"}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "entityExpressionProviderForFixedEntity")
    public static void testEntityExpressionWithFixedEntity(String expression) throws Exception {
        SeriesQuery query = createTestQuery(FIXED_ENTITY_NAME);
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);

        assertEquals(formatErrorMsg("Wrong number of results", expression), 1, result.size());
        Series series = result.get(0);
        assertEquals(formatErrorMsg("Wrong entity selected", expression), FIXED_ENTITY_NAME, series.getEntity());
        List<Sample> seriesData = series.getData();
        assertEquals(formatErrorMsg("Wrong number of data entries", expression), 1, seriesData.size());
        assertEquals(formatErrorMsg("Wrong data received", expression), Mocks.SAMPLE, seriesData.get(0));
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "emptyResultEntityExpressionProviderForFixedEntity")
    public static Object[][] provideEmptyResultEntityExpressionForFixedEntity() {
        return new Object[][] {
                // Contains method
                {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').contains('asdef003')"},
                {"property_values('" + PROPERTY_TYPE + "::name').contains('lolololololololo002')"},
                {"property_values('" + PROPERTY_TYPE + "::group').contains('hell')"},

                // Matches method
                {"matches('de', property_values('" + PROPERTY_TYPE + "::name'))"},
                {"matches('*', property_values('" + PROPERTY_TYPE + ":foo=other:name'))"},

                // IsEmpty method
                {"property_values('" + PROPERTY_TYPE + "::name').isEmpty()"},
                {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').isEmpty()"},
                {"property_values('" + PROPERTY_TYPE + "::group').isEmpty()"},

                // Property method
                {"property('"+ PROPERTY_TYPE +"::name') = ''"},
                {"property('"+ PROPERTY_TYPE +":testkey=test:name') = ''"},
                {"property('"+ PROPERTY_TYPE +"::multitag') = 'other'"},


                {"null"}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "emptyResultEntityExpressionProviderForFixedEntity")
    public static void testEmptyResultEntityExpressionWithFixedEntity(String expression) throws Exception {
        SeriesQuery query = createTestQuery(FIXED_ENTITY_NAME);
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);

        assertEquals(formatErrorMsg("Dummy result should be the one present", expression), 1, result.size());
        Series series = result.get(0);
        assertEquals(formatErrorMsg("Dummy result entity name should be preserved", expression),
                FIXED_ENTITY_NAME, series.getEntity());
        assertTrue(formatErrorMsg("Dummy result data should be empty", expression), series.getData().isEmpty());
    }

    /*
    * Correct data test cases
    */
    @DataProvider(name = "entityExpressionProviderForEntityGroup")
    public static Object[][] provideEntityExpressionForEntityGroup() {
        return new Object[][] {
                // Contains method
                {"property_values('" + PROPERTY_TYPE + "::name').contains('asdef001')", getPrefixedSet("asdef001")},
                {"property_values('" + PROPERTY_TYPE + "::group').contains('hell')", getPrefixedSet("asdef002")},

                // Matches method
                {"matches('asdef*', property_values('" + PROPERTY_TYPE + "::name'))", ENTITIES_IN_GROUP},
                {"matches('*', property_values('" + PROPERTY_TYPE + "::name'))", ENTITIES_IN_GROUP},
                {"matches('*def0*', property_values('" + PROPERTY_TYPE + "::name'))", ENTITIES_IN_GROUP},
                {"matches('*001', property_values('" + PROPERTY_TYPE + ":testkey=test:name'))", getPrefixedSet("asdef001")},
                {"matches('*', property_values('" + PROPERTY_TYPE + ":testkey=test,otherkey=other:name'))", getPrefixedSet("asdef001")},
                {"matches('*', property_values('" + PROPERTY_TYPE + ":otherkey=other:name'))", getPrefixedSet("asdef001", "asdef003")},
                {"matches('hel*', property_values('" + PROPERTY_TYPE + "::group'))", getPrefixedSet("asdef001", "asdef002")},

                // IsEmpty method
                {"property_values('" + PROPERTY_TYPE + "::badtag').isEmpty()", ENTITIES_IN_GROUP},
                {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').isEmpty()", getPrefixedSet("asdef003")},
                {"property_values('" + PROPERTY_TYPE + "::group').isEmpty()", getPrefixedSet("asdef003")},

                // Property method
                {"property('not-"+ PROPERTY_TYPE +"::name') = ''", ENTITIES_IN_GROUP},
                {"property('" + PROPERTY_TYPE + "::badtag') = ''", ENTITIES_IN_GROUP},
                {"property('"+ PROPERTY_TYPE +":testkey=test:name') = ''", getPrefixedSet("asdef003")},
                {"property('"+ PROPERTY_TYPE +"::name') = 'asdef001'", getPrefixedSet("asdef001")},
                {"property('"+ PROPERTY_TYPE +"::group') LIKE 'hel*'", getPrefixedSet("asdef001", "asdef002")}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "entityExpressionProviderForEntityGroup")
    public static void testEntityExpressionWithEntityGroup(String expression, HashSet<String> expectedEntities) throws Exception {
        SeriesQuery query = createTestQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> receivedEntities = new HashSet<>();
        for (Series series: result) {
            receivedEntities.add(series.getEntity());
        }

        assertEquals(formatErrorMsg("Wrong result entity set", expression), expectedEntities, receivedEntities);
        for (Series series: result) {
            List<Sample> seriesData = series.getData();
            assertEquals(formatErrorMsg("Wrong number of data entries", expression), 1, seriesData.size());
            assertEquals(formatErrorMsg("Wrong data received", expression), Mocks.SAMPLE, seriesData.get(0));
        }
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "emptyResultEntityExpressionProviderForEntityGroup")
    public static Object[][] provideEmptyResultEntityExpressionForEntityGroup() {
        return new Object[][] {
                // Contains method
                {"property_values('" + PROPERTY_TYPE + ":testkey=test:name').contains('asdef003')"},
                {"property_values('" + PROPERTY_TYPE + "::name').contains('lolololololololo002')"},

                // Matches method
                {"matches('de', property_values('" + PROPERTY_TYPE + "::name'))"},

                // IsEmpty method
                {"property_values('" + PROPERTY_TYPE + "::name').isEmpty()"},

                // Property method
                {"property('"+ PROPERTY_TYPE +"::name') = ''"},
                {"property('"+ PROPERTY_TYPE +"::multitag') = 'other'"},

                {"null"}
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "emptyResultEntityExpressionProviderForEntityGroup")
    public static void testEntityExpressionWithEntityGroup(String expression) throws Exception {
        SeriesQuery query = createTestQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> receivedEntities = new HashSet<>();
        for (Series series: result) {
            receivedEntities.add(series.getEntity());
        }

        assertTrue(formatErrorMsg("Result set should be empty", expression), receivedEntities.isEmpty());
    }

    /*
     * Bad data test cases
     */
    @DataProvider(name = "errorEntityExpressionProvider")
    public static Object[][] provideErrorEntityExpression() {
        return new Object[][] {
                {""},
                {"foo"},

                {"property_values(foo).isEmpty()"},
                {"property_values('"+ PROPERTY_TYPE +"::name').foo()"},

                {"matches(foo).isEmpty()"},
                {"matches(foo).foo()"},
                {"matches(foo, '"+ PROPERTY_TYPE +"::name').isEmpty()"},
                {"matches(foo, '"+ PROPERTY_TYPE +"::name').foo()"},

                {"property(foo) = ''"},
                {"property('"+ PROPERTY_TYPE +"::name') = foo"},
        };
    }

    /**
     * #3612
     */
    @Test(dataProvider = "errorEntityExpressionProvider")
    public static void testErrorOnBadEntityExpression(String expression) throws Exception {
        SeriesQuery query = createTestQuery("*");
        query.setEntityExpression(expression);

        Response response = SeriesMethod.executeQueryRaw(Collections.singletonList(query));
        Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
        String errMsg = "Wrong result status code, expected 4**, got " + response.getStatus();
        errMsg = formatErrorMsg(errMsg, expression);

        assertEquals(errMsg, Response.Status.Family.CLIENT_ERROR, statusFamily);
    }


    private static HashSet<String> getPrefixedSet(String... entityNames) {
        HashSet<String> result = new HashSet<>();
        for (String name: entityNames) {
            result.add(entityNameWithPrefix(name));
        }
        return result;
    }

    private static Property createTestProperty(String shortEntityName) {
        Property property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity(entityNameWithPrefix(shortEntityName));
        property.addTag("name", shortEntityName);
        return property;
    }

    private static String entityNameWithPrefix(String entityName) {
        return ENTITY_NAME_PREFIX + entityName;
    }

    private static SeriesQuery createTestQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, METRIC_NAME);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }

    private static Series createTestSeries(String entityName) {
        Series series = new Series();
        Registry.Entity.checkExists(entityName);
        series.setEntity(entityName);
        series.setMetric(METRIC_NAME);
        series.addSamples(Mocks.SAMPLE);
        return series;
    }

    private static String formatErrorMsg(String msg, String expression) {
        return String.format("%s for expression %s%n%s", msg, expression, TEST_DATASET_DESCRIPTION);
    }
}
