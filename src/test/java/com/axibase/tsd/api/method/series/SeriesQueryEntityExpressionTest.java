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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * Created by Aleksandr Veselov.
 */
public class SeriesQueryEntityExpressionTest extends SeriesMethod {

    private static final String METRIC_NAME = "m-test-entity-expression-asdef001";
    private static final String PROPERTY_TYPE = "test-entity-expression-asdef001";
    private static final String ENTITY_GROUP_NAME = "test-entity-expression-asdef001";

    private static final Metric METRIC = new Metric(METRIC_NAME);
    private static final List<Property> PROPERTIES = new LinkedList<>();
    private static final String[] ENTITIES_IN_GROUP;

    private static final int ALL_ENTITIES;
    private static final int NO_ENTITY = -1;
    private static final int ALL_IN_ENTITY_GROUP;

    static {
        METRIC.setDataType(DataType.INTEGER);

        Property property;

        property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity("e-test-entity-expression-asdef001");
        property.addTag("name", "asdef001");
        property.addTag("group", "hello");
        property.addTag("multitag", "[one, other]");
        property.addKey("testkey", "test");
        property.addKey("otherkey", "other");
        PROPERTIES.add(property);

        property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity("e-test-entity-expression-asdef002");
        property.addTag("name", "asdef002");
        property.addTag("group", "hell");
        property.addKey("testkey", "test");
        PROPERTIES.add(property);

        property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity("e-test-entity-expression-asdef003");
        property.addTag("name", "asdef003");
        PROPERTIES.add(property);

        property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity("e-test-entity-expression-asdef004");
        property.addTag("name", "asdef004");
        property.addTag("group", "main");
        PROPERTIES.add(property);

        property = new Property();
        property.setType(PROPERTY_TYPE);
        property.setEntity("e-test-entity-expression-asdef005");
        property.addTag("name", "asdef005");
        property.addTag("group", "foo");
        PROPERTIES.add(property);

        ALL_ENTITIES = PROPERTIES.size();

        ENTITIES_IN_GROUP = new String[] {
                "e-test-entity-expression-asdef001",
                "e-test-entity-expression-asdef002",
                "e-test-entity-expression-asdef003",
                "e-test-entity-expression-asdef004"
        };

        ALL_IN_ENTITY_GROUP = ENTITIES_IN_GROUP.length;
    }

    @BeforeClass
    public static void createTestData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(METRIC);
        for (Property property: PROPERTIES) {
            String entityName = property.getEntity();
            Entity entity = new Entity(entityName);
            if (Arrays.asList(ENTITIES_IN_GROUP).contains(entityName)) {
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



    @DataProvider(name = "entityExpressionProvider")
    public static Object[][] provideEntityExpression() {
        return new Object[][] {
            // Contains method
            {"property_values('"+ PROPERTY_TYPE +"::name').contains('asdef001')", 1},
            {"property_values('"+ PROPERTY_TYPE +"::name').contains('asdef002')", 1},
            {"property_values('"+ PROPERTY_TYPE +":testkey=test:name').contains('asdef003')", NO_ENTITY},
            {"property_values('"+ PROPERTY_TYPE +"::group').contains('main')", 1},
            {"property_values('"+ PROPERTY_TYPE +"::name').contains('lolololololololo002')", NO_ENTITY},
            // Matches method
            {"matches('asdef*', property_values('"+ PROPERTY_TYPE +"::name'))", ALL_ENTITIES},
            {"matches('*', property_values('"+ PROPERTY_TYPE +"::name'))", ALL_ENTITIES},
            {"matches('*def0*', property_values('"+ PROPERTY_TYPE +"::name'))", ALL_ENTITIES},
            {"matches('de', property_values('"+ PROPERTY_TYPE +"::name'))", NO_ENTITY},
            {"matches('*', property_values('"+ PROPERTY_TYPE +":testkey=test:name'))", 2},
            {"matches('*', property_values('"+ PROPERTY_TYPE +":testkey=test,otherkey=other:name'))", 1},
            {"matches('*', property_values('"+ PROPERTY_TYPE +":otherkey=other:name'))", 1},
            {"matches('hel*', property_values('"+ PROPERTY_TYPE +"::group'))", 2},
            // IsEmpty method
            {"property_values('"+ PROPERTY_TYPE +"::badtag').isEmpty()", ALL_ENTITIES},
            {"property_values('"+ PROPERTY_TYPE +"::name').isEmpty()", NO_ENTITY},
            {"property_values('"+ PROPERTY_TYPE +":testkey=test:name').isEmpty()", 3},
            {"property_values('"+ PROPERTY_TYPE +"::group').isEmpty()", 1},
            // Property method
            {"property('not-"+ PROPERTY_TYPE +"::badtag') = ''", ALL_ENTITIES},
            {"property('"+ PROPERTY_TYPE +"::badtag') = ''", ALL_ENTITIES},
            {"property('"+ PROPERTY_TYPE +":testkey=test:name') = ''", 3},
            {"property('"+ PROPERTY_TYPE +"::name') = 'asdef001'", 1},
            {"property('"+ PROPERTY_TYPE +"::multitag') = 'one'", 1},
            {"property('"+ PROPERTY_TYPE +"::group') LIKE 'hel*'", 2},
            {null, ALL_ENTITIES}
        };
    }


    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithWildcardEntity(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        if (nResults == NO_ENTITY) {
            assertEquals("dummy result is not present", 1, result.size());
            Series series = result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), series.getEntity());
            assertEquals("dummy result data size", 0, series.getData().size());
        } else {
            assertEquals("result count is wrong", nResults, result.size());
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithFixedEntity(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery("e-test-entity-expression-asdef001");
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        if (nResults == NO_ENTITY) {
            assertEquals("dummy result is not present", 1, result.size());
            Series series = result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), series.getEntity());
            assertEquals("dummy result data size", 0, series.getData().size());
        } else {
            assertEquals("result count is wrong", Math.min(nResults, 1), result.size());
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithEntityGroup(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery(null);
        query.setEntityGroup(ENTITY_GROUP_NAME);
        query.setEntityExpression(expression);
        List<Series> result = SeriesMethod.executeQueryReturnSeries(query);
        // No dummy series !!!
        if (nResults == NO_ENTITY) {
            nResults = 0;
        }
        assertEquals("result count is wrong", Math.min(nResults, ALL_IN_ENTITY_GROUP), result.size());
    }

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
        int status = SeriesMethod.executeQueryRaw(Collections.singletonList(query)).getStatus();
        if (status / 100 != 4) {
            fail("Wrong result status code, expected 4**, got " + status);
        }
    }


    private static SeriesQuery createDummyQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, METRIC_NAME);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }
}
