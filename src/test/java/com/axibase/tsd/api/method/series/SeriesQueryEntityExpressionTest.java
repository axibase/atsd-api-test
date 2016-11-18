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
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Aleksandr Veselov.
 */
public class SeriesQueryEntityExpressionTest extends SeriesMethod {

    public static final String metricName = "m-test-entity-expression-asdef001";
    public static final String propertyType = "test-entity-expression-asdef001";
    public static final String entityGroupName = "test-entity-expression-asdef001";
    public static final Metric metric = new Metric(metricName);
    public static final List<Property> properties = new LinkedList<>();
    public static final String[] entitiesInGroup;

    private static final int ALL_ENTITIES;
    private static final int NO_ENTITY = -1;
    private static final int ALL_IN_ENTITY_GROUP;

    static {
        metric.setDataType(DataType.INTEGER);

        Property property;

        property = new Property();
        property.setType(propertyType);
        property.setEntity("e-test-entity-expression-asdef001");
        property.addTag("name", "asdef001");
        property.addTag("group", "hello");
        property.addTag("multitag", "[one, other]");
        property.addKey("testkey", "test");
        property.addKey("otherkey", "other");
        properties.add(property);

        property = new Property();
        property.setType(propertyType);
        property.setEntity("e-test-entity-expression-asdef002");
        property.addTag("name", "asdef002");
        property.addTag("group", "hell");
        property.addKey("testkey", "test");
        properties.add(property);

        property = new Property();
        property.setType(propertyType);
        property.setEntity("e-test-entity-expression-asdef003");
        property.addTag("name", "asdef003");
        properties.add(property);

        property = new Property();
        property.setType(propertyType);
        property.setEntity("e-test-entity-expression-asdef004");
        property.addTag("name", "asdef004");
        property.addTag("group", "main");
        properties.add(property);

        property = new Property();
        property.setType(propertyType);
        property.setEntity("e-test-entity-expression-asdef005");
        property.addTag("name", "asdef005");
        property.addTag("group", "foo");
        properties.add(property);

        ALL_ENTITIES = properties.size();

        entitiesInGroup = new String[] {
                "e-test-entity-expression-asdef001",
                "e-test-entity-expression-asdef002",
                "e-test-entity-expression-asdef003",
                "e-test-entity-expression-asdef004"
        };

        ALL_IN_ENTITY_GROUP = entitiesInGroup.length;
    }

    @BeforeClass
    public static void createTestData() throws Exception {
        MetricMethod.createOrReplaceMetricCheck(metric);
        for (Property property: properties) {
            String entityName = property.getEntity();
            Entity entity = new Entity(entityName);
            if (Arrays.asList(entitiesInGroup).contains(entityName)) {
                entity.addTag("group", "append");
            }
            EntityMethod.createOrReplaceEntityCheck(entity);
            PropertyMethod.insertPropertyCheck(property);

            Series series = new Series();
            series.setEntity(entityName);
            series.setMetric(metricName);
            series.addData(new Sample("2026-11-15T12:23:49.520Z", 1));
            SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        }

        EntityGroup group = new EntityGroup(entityGroupName);
        group.setExpression("tags.group = 'append'");
        EntityGroupMethod.createOrReplaceEntityGroupCheck(group);
    }



    @DataProvider(name = "entityExpressionProvider")
    public static Object[][] provideEntityExpression() {
        return new Object[][] {
            // Contains method
            {"property_values('"+propertyType+"::name').contains('asdef001')", 1},
            {"property_values('"+propertyType+"::name').contains('asdef002')", 1},
            {"property_values('"+propertyType+":testkey=test:name').contains('asdef003')", NO_ENTITY},
            {"property_values('"+propertyType+"::group').contains('main')", 1},
            {"property_values('"+propertyType+"::name').contains('lolololololololo002')", NO_ENTITY},
            // Matches method
            {"matches('asdef*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('*def0*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('de', property_values('"+propertyType+"::name'))", NO_ENTITY},
            {"matches('*', property_values('"+propertyType+":testkey=test:name'))", 2},
            {"matches('*', property_values('"+propertyType+":testkey=test,otherkey=other:name'))", 1},
            {"matches('*', property_values('"+propertyType+":otherkey=other:name'))", 1},
            {"matches('hel*', property_values('"+propertyType+"::group'))", 2},
            // IsEmpty method
            {"property_values('"+propertyType+"::badtag').isEmpty()", ALL_ENTITIES},
            {"property_values('"+propertyType+"::name').isEmpty()", NO_ENTITY},
            {"property_values('"+propertyType+":testkey=test:name').isEmpty()", 3},
            {"property_values('"+propertyType+"::group').isEmpty()", 1},
            // Property method
            {"property('not-"+propertyType+"::badtag') = ''", ALL_ENTITIES},
            {"property('"+propertyType+"::badtag') = ''", ALL_ENTITIES},
            {"property('"+propertyType+":testkey=test:name') = ''", 3},
            {"property('"+propertyType+"::name') = 'asdef001'", 1},
            {"property('"+propertyType+"::multitag') = 'one'", 1},
            {"property('"+propertyType+"::group') LIKE 'hel*'", 2},
            {null, ALL_ENTITIES}
        };
    }


    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithWildcardEntity(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        JSONArray result = SeriesMethod.executeQuery(query);
        if (nResults == NO_ENTITY) {
            assertEquals("dummy result is not present", 1, result.length());
            JSONObject seriesJSON = (JSONObject) result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), seriesJSON.get("entity"));
            assertEquals("dummy result data size", 0, seriesJSON.getJSONArray("data").length());
        } else {
            assertEquals("result count is wrong", nResults, result.length());
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithFixedEntity(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery("e-test-entity-expression-asdef001");
        query.setEntityExpression(expression);
        JSONArray result = SeriesMethod.executeQuery(query);
        if (nResults == NO_ENTITY) {
            assertEquals("dummy result is not present", 1, result.length());
            JSONObject seriesJSON = (JSONObject) result.get(0);
            assertEquals("dummy result entity name", query.getEntity(), seriesJSON.get("entity"));
            assertEquals("dummy result data size", 0, seriesJSON.getJSONArray("data").length());
        } else {
            assertEquals("result count is wrong", Math.min(nResults, 1), result.length());
        }
    }

    @Test(dataProvider = "entityExpressionProvider")
    public static void testEntityExpressionWithEntityGroup(String expression, int nResults) throws Exception {
        SeriesQuery query = createDummyQuery(null);
        query.setEntityGroup(entityGroupName);
        query.setEntityExpression(expression);
        JSONArray result = SeriesMethod.executeQuery(query);
        // No dummy series !!!
        if (nResults == NO_ENTITY) {
            nResults = 0;
        }
        assertEquals("result count is wrong", Math.min(nResults, ALL_IN_ENTITY_GROUP), result.length());
    }

    @DataProvider(name = "errorEntityExpressionProvider")
    public static Object[][] provideErrorEntityExpression() {
        return new Object[][] {
            {"foo"},

            {"property_values(foo).isEmpty()"},
            {"property_values('"+propertyType+"::name').foo()"},

            {"matches(foo).isEmpty()"},
            {"matches(foo).foo()"},
            {"matches(foo, '"+propertyType+"::name').isEmpty()"},
            {"matches(foo, '"+propertyType+"::name').foo()"},

            {"property(foo) = ''"},
            {"property('"+propertyType+"::name') = foo"},
        };
    }

    @Test(dataProvider = "errorEntityExpressionProvider")
    public static void testErrorOnBadEntityExpression(String expression) throws Exception {
        SeriesQuery query = createDummyQuery("*");
        query.setEntityExpression(expression);
        int status = SeriesMethod.executeQueryRaw(Collections.singletonList(query)).getStatus();
        assertEquals("result count is wrong", 404, status);
    }


    private static SeriesQuery createDummyQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, metricName);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }
}
