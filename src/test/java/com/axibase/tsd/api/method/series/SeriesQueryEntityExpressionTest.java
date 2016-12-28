package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.*;

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
    static {
        METRIC.setDataType(DataType.INTEGER);
    }

    private static final List<Property> PROPERTIES = new LinkedList<>();
    private static final HashSet<String> ALL_ENTITIES = new HashSet<>();
    private static final HashSet<String> ENTITIES_IN_GROUP = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @BeforeClass
    public static void createTestData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(METRIC);
        populateProperties();
        for (Property property: PROPERTIES) {
            String entityName = property.getEntity();
            Entity entity = new Entity(entityName);
            if (ENTITIES_IN_GROUP.contains(entityName)) {
                entity.addTag("group", "append");
            }
            EntityMethod.createOrReplaceEntityCheck(entity);
            PropertyMethod.insertPropertyCheck(property);

            Series series = new Series();
            series.setEntity(entityName);
            series.setMetric(METRIC_NAME);
            series.addData(new Sample("2026-11-15T12:23:49.520Z", 1));
            SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        }

        EntityGroup group = new EntityGroup(ENTITY_GROUP_NAME);
        group.setExpression("tags.group = 'append'");
        EntityGroupMethod.createOrReplaceEntityGroupCheck(group);
    }

    public static void populateProperties() {
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
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "entityExpressionProvider")
    public static Object[][] provideEntityExpression() {
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
    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithWildcardEntity(String expression, HashSet<String> expected) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity expression: {}", query.getEntityExpression());
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> gotResultSet = new HashSet<>();
        for (Series series: result) {
            gotResultSet.add(series.getEntity());
        }
        assertEquals("invalid result entity set", expected, gotResultSet);
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "falseEntityExpressionProvider")
    public static Object[][] provideFalseEntityExpression() {
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
    @Test(dataProvider = "falseEntityExpressionProvider")
    public static void testFalseEntityExpressionWithWildcardEntity(String expression) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity expression: {}", query.getEntityExpression());

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals("dummy result is not present", 1, result.size());

        Series series = result.get(0);
        assertEquals("dummy result entity name", query.getEntity(), series.getEntity());
        assertEquals("dummy result data size", 0, series.getData().size());
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
        SeriesQuery query = createDummyQuery(FIXED_ENTITY_NAME);
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity expression: {}", query.getEntityExpression());

        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals("Wrong number of results", 1, result.size());
        assertEquals("Wrong entity selected", FIXED_ENTITY_NAME, result.get(0).getEntity());
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "falseEntityExpressionProviderForFixedEntity")
    public static Object[][] provideFalseEntityExpressionForFixedEntity() {
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
    @Test(dataProvider = "falseEntityExpressionProviderForFixedEntity")
    public static void testFalseEntityExpressionWithFixedEntity(String expression) throws Exception {
        SeriesQuery query = createDummyQuery(FIXED_ENTITY_NAME);
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity expression: {}", query.getEntityExpression());
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals("dummy result should be the one present", 1, result.size());
        Series series = result.get(0);
        assertEquals("dummy result entity name should be preserved", FIXED_ENTITY_NAME, series.getEntity());
        assertTrue("dummy result data should be empty", series.getData().isEmpty());
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
    public static void testEntityExpressionWithEntityGroup(String expression, HashSet<String> expected) throws Exception {
        SeriesQuery query = createDummyQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity group: {}", query.getEntityGroup());
        logger.info("Entity expression: {}", query.getEntityExpression());
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> gotResultSet = new HashSet<>();
        for (Series series: result) {
            gotResultSet.add(series.getEntity());
        }
        assertEquals("wrong result entity set", expected, gotResultSet);
    }

    /*
     * Correct data test cases
     */
    @DataProvider(name = "falseEntityExpressionProviderForEntityGroup")
    public static Object[][] provideFalseEntityExpressionForEntityGroup() {
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
    @Test(dataProvider = "falseEntityExpressionProviderForEntityGroup")
    public static void testEntityExpressionWithEntityGroup(String expression) throws Exception {
        SeriesQuery query = createDummyQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity group: {}", query.getEntityGroup());
        logger.info("Entity expression: {}", query.getEntityExpression());
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        HashSet<String> gotResultSet = new HashSet<>();
        for (Series series: result) {
            gotResultSet.add(series.getEntity());
        }
        assertEquals("result set should be empty", new HashSet<>(), gotResultSet);
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
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        logger.info("Entity: {}", query.getEntity());
        logger.info("Entity expression: {}", query.getEntityExpression());
        Response response = SeriesMethod.executeQueryRaw(Collections.singletonList(query));
        Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
        if (statusFamily != Response.Status.Family.CLIENT_ERROR) {
            fail("Wrong result status code, expected 4**, got " + response.getStatus());
        }
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

    private static SeriesQuery createDummyQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, METRIC_NAME);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }
}
