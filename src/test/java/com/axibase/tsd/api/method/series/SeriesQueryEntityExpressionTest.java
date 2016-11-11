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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.*;

import static com.axibase.tsd.api.method.series.SeriesQueryEntityExpressionTest.FormatEntityExpression.*;
import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * @author Aleksandr Veselov
 * @issue 3612
 */
public class SeriesQueryEntityExpressionTest extends SeriesMethod {

    private static final String METRIC_NAME = "m-test-entity-expression-asdef001";
    private static final String PROPERTY_TYPE = "test-entity-expression-asdef001";
    private static final String ENTITY_NAME_PREFIX = "e-test-entity-expression-";
    private static final String ENTITY_GROUP_NAME = "test-entity-expression-asdef001";

    private static final Metric METRIC = new Metric(METRIC_NAME);
    private static final List<Property> PROPERTIES = new LinkedList<>();

    private static final EntityNameSet ALL_ENTITIES = new EntityNameSet();
    private static final EntityNameSet NO_ENTITY = null;
    private static final EntityNameSet ENTITIES_IN_GROUP;

    private static class EntityNameSet extends HashSet<String> {
        public EntityNameSet() {
            super();
        }

        public EntityNameSet(Collection<? extends String> collection) {
            super(collection);
        }

    }

    static {
        METRIC.setDataType(DataType.INTEGER);

        Property property;

        property = createAndAppendDummyProperty("asdef001");
        property.addTag("group", "hello");
        property.addTag("multitag", "one");
        property.addKey("testkey", "test");
        property.addKey("otherkey", "other");

        property = createAndAppendDummyProperty("asdef002");
        property.addTag("group", "hell");
        property.addKey("testkey", "test");

        property = createAndAppendDummyProperty("asdef003");
        property.addKey("otherkey", "other");

        property = createAndAppendDummyProperty("asdef004");
        property.addTag("group", "main");
        property.addTag("multitag", "other");

        property = createAndAppendDummyProperty("asdef005");
        property.addTag("group", "foo");
        property.addKey("testkey", "test");

        for (Property prop: PROPERTIES) {
            ALL_ENTITIES.add(prop.getEntity());
        }

        ENTITIES_IN_GROUP = getPrefixedSet("asdef001", "asdef002", "asdef003");
    }

    @BeforeClass
    public static void createTestData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(METRIC);
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

    /*
     * Correct data test cases
     */
    @DataProvider(name = "entityExpressionProvider")
    public static Object[][] provideEntityExpression() {
        return new Object[][] {
            /**
             * Test case signature: { entityExpression: String, expectedEntities: EntityNameSet}
             * @param entityExpression Queried entity expression string
             * @param expectedEntities Max subset of ALL_ENTITIES set, all of which satisfy entityExpression
             */
            // Contains method
            {expr(propertyValues("name"), contains("asdef001")), getPrefixedSet("asdef001")},
            {expr(propertyValues("name", "testkey=test"), contains("asdef003")), NO_ENTITY},
            {expr(propertyValues("group"), contains("hell")), getPrefixedSet("asdef002")},
            {expr(propertyValues("name"), contains("lolololololololo002")), NO_ENTITY},
            // Matches method
            {matches("asdef*", propertyValues("name")), ALL_ENTITIES},
            {matches("*", propertyValues("name")), ALL_ENTITIES},
            {matches("*def0*", propertyValues("name")), ALL_ENTITIES},
            {matches("de", propertyValues("name")), NO_ENTITY},
            {matches("*001", propertyValues("name", "testkey=test")), getPrefixedSet("asdef001")},
            {matches("*", propertyValues("name", "testkey=test,otherkey=other")), getPrefixedSet("asdef001")},
            {matches("*", propertyValues("name", "otherkey=other")), getPrefixedSet("asdef001", "asdef003")},
            {matches("hel*", propertyValues("group")), getPrefixedSet("asdef001", "asdef002")},
            // IsEmpty method
            {expr(propertyValues("badtag"), isEmpty()), ALL_ENTITIES},
            {expr(propertyValues("name"), isEmpty()), NO_ENTITY},
            {expr(propertyValues("name", "testkey=test"), isEmpty()), getPrefixedSet("asdef003", "asdef004")},
            {expr(propertyValues("group"), isEmpty()), getPrefixedSet("asdef003")},
            // Property method
            {"property('not-"+ PROPERTY_TYPE +"::badtag') = ''", ALL_ENTITIES},
            {property("badtag")+ " = ''", ALL_ENTITIES},
            {property("name", "testkey=test") + " = ''", getPrefixedSet("asdef003", "asdef004")},
            {property("name") + " = 'asdef001'", getPrefixedSet("asdef001")},
            {property("multitag") + " = 'other'", getPrefixedSet("asdef004")},
            {property("group") + " LIKE 'hel*'", getPrefixedSet("asdef001", "asdef002")},
            {null, ALL_ENTITIES}
        };
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithWildcardEntity(String expression, EntityNameSet expected) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        if (expected == NO_ENTITY) {
            assertEquals("dummy result is not present", 1, result.size());
            Series series = result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), series.getEntity());
            assertEquals("dummy result data size", 0, series.getData().size());
        } else {
            EntityNameSet expectedResultSet = new EntityNameSet(ALL_ENTITIES);
            expectedResultSet.retainAll(expected);
            EntityNameSet gotResultSet = new EntityNameSet();
            for (Series series: result) {
                gotResultSet.add(series.getEntity());
            }
            assertEquals("invalid result entity set", expectedResultSet, gotResultSet);
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithFixedEntity(String expression, EntityNameSet expected) throws Exception {
        SeriesQuery query = createDummyQuery("e-test-entity-expression-asdef001");
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        if (expected == NO_ENTITY || !expected.contains(query.getEntity())) {
            assertEquals("dummy result is not present", 1, result.size());
            Series series = result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), series.getEntity());
            assertEquals("dummy result data size", 0, series.getData().size());
        } else {
            EntityNameSet expectedResultSet = new EntityNameSet();
            expectedResultSet.add(query.getEntity());
            EntityNameSet gotResultSet = new EntityNameSet();
            for (Series series: result) {
                gotResultSet.add(series.getEntity());
            }
            assertEquals("invalid result entity set", expectedResultSet, gotResultSet);
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithEntityGroup(String expression, EntityNameSet expected) throws Exception {
        SeriesQuery query = createDummyQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        // No dummy series !!!
        if (expected == NO_ENTITY) {
            expected = new EntityNameSet();
        }
        EntityNameSet expectedResultSet = new EntityNameSet(ALL_ENTITIES);
        expectedResultSet.retainAll(expected);
        expectedResultSet.retainAll(ENTITIES_IN_GROUP);
        EntityNameSet gotResultSet = new EntityNameSet();
        for (Series series: result) {
            gotResultSet.add(series.getEntity());
        }
        assertEquals("invalid result entity set", expectedResultSet, gotResultSet);
    }


    /*
     * Bad data test cases
     */
    @DataProvider(name = "errorEntityExpressionProvider")
    public static Object[][] provideErrorEntityExpression() {
        return new Object[][] {
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

    @Test(dataProvider = "errorEntityExpressionProvider")
    public static void testErrorOnBadEntityExpression(String expression) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        Response response = SeriesMethod.executeQueryRaw(Collections.singletonList(query));
        Response.Status.Family statusFamily = response.getStatusInfo().getFamily();
        if (statusFamily != Response.Status.Family.CLIENT_ERROR) {
            fail("Wrong result status code, expected 4**, got " + response.getStatus());
        }
    }

    /**
     * Contains methods to format entity expression functions as strings.
     * Each method, except expr(String, String...) refers to same for
     * entity expression syntax
     */
    public static class FormatEntityExpression {

        /**
         * Formats whole expression by chaining arguments with . (dot)
         * @param base Required initial object
         * @param others Optional method calls
         * @return Chained entity expression string
         */
        public static String expr(String base, String... others) {
            StringBuilder builder = new StringBuilder();
            builder.append(base);
            for (String cmd : others) {
                builder.append('.');
                builder.append(cmd);
            }
            return builder.toString();
        }

        public static String propertyValues(String tag) {
            return propertyValues(tag, "");
        }

        public static String propertyValues(String tag, String keys) {
            return String.format("property_values('%s:%s:%s')", PROPERTY_TYPE, keys, tag);
        }

        public static String contains(String value) {
            return String.format("contains('%s')", value);
        }

        public static String matches(String pattern, String expr) {
            return String.format("matches('%s', %s)", pattern, expr);
        }

        public static String property(String tag) {
            return property(tag, "");
        }

        public static String property(String tag, String keys) {
            return String.format("property('%s:%s:%s')", PROPERTY_TYPE, keys, tag);
        }

        public static String isEmpty() {
            return "isEmpty()";
        }

    }

    private static String withPrefix(String entityName) {
        return ENTITY_NAME_PREFIX + entityName;
    }

    private static EntityNameSet getPrefixedSet(String... entityNames) {
        EntityNameSet result = new EntityNameSet();
        for (String name: entityNames) {
            result.add(withPrefix(name));
        }
        return result;
    }

    private static SeriesQuery createDummyQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, METRIC_NAME);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }

    /**
     * Creates test-standard property (with test-standard type,
     * entity, prefixed with test-standard entity prefix
     * and "name tag", set to given name).
     *
     * After creation property is appended to PROPERTIES.
     * @param shortEntityName Short name for entity (without prefix)
     * @return Created standard property
     * @issue 3612
     */
    private static Property createAndAppendDummyProperty(String shortEntityName) {
        Property property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity(withPrefix(shortEntityName));
        property.addTag("name", shortEntityName);
        PROPERTIES.add(property);
        return property;
    }
}
