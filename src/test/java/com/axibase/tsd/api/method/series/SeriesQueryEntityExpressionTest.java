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
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Aleksandr Veselov.
 */
public class SeriesQueryEntityExpressionTest extends SeriesMethod {

    public static final String metricName = "m-test-entity-expression-asdef001";
    public static final String propertyType = "test-entity-expression-asdef001";
    public static final String entityGroupName = "test-entity-expression-asdef001";
    public static final Metric metric = new Metric(metricName);
    public static final Map<String, Map<String, String>> entitiesWithPropertyTags = new HashMap<>();
    public static final String[] entitiesInGroup;

    private static final int ALL_ENTITIES;
    private static final int NO_ENTITY = -1;
    private static final int ALL_IN_ENTITY_GROUP;

    static {
        metric.setDataType(DataType.INTEGER);

        Map<String, String> propertyTags1 = new HashMap<>();
        propertyTags1.put("name", "asdef001");
        propertyTags1.put("group", "hello");
        entitiesWithPropertyTags.put("e-test-entity-expression-asdef001", propertyTags1);

        Map<String, String> propertyTags2 = new HashMap<>();
        propertyTags2.put("name", "asdef002");
        propertyTags2.put("group", "hell");
        entitiesWithPropertyTags.put("e-test-entity-expression-asdef002", propertyTags2);

        Map<String, String> propertyTags3 = new HashMap<>();
        propertyTags3.put("name", "asdef003");
        entitiesWithPropertyTags.put("e-test-entity-expression-asdef003", propertyTags3);

        Map<String, String> propertyTags4 = new HashMap<>();
        propertyTags4.put("name", "asdef004");
        propertyTags4.put("group", "main");
        entitiesWithPropertyTags.put("e-test-entity-expression-asdef004", propertyTags4);

        Map<String, String> propertyTags5 = new HashMap<>();
        propertyTags5.put("name", "asdef005");
        propertyTags5.put("group", "foo");
        entitiesWithPropertyTags.put("e-test-entity-expression-asdef005", propertyTags5);

        ALL_ENTITIES = entitiesWithPropertyTags.size();

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
        for (String entityName: entitiesWithPropertyTags.keySet()) {
            Entity entity = new Entity(entityName);
            if (Arrays.asList(entitiesInGroup).contains(entityName)) {
                entity.addTag("group", "append");
            }
            EntityMethod.createOrReplaceEntityCheck(entity);

            Property property = new Property();
            property.setEntity(entityName);
            property.setType(propertyType);
            property.setTags(entitiesWithPropertyTags.get(entityName));
            PropertyMethod.insertPropertyCheck(property);

            Series series = new Series();
            series.setEntity(entityName);
            series.setMetric(metricName);
            series.addData(new Sample("2016-11-15T12:23:49.520Z", 1));
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
            {"property_values('"+propertyType+"::group').contains('main')", 1},
            {"property_values('"+propertyType+"::name').contains('lolololololololo002')", NO_ENTITY},
            // Matches method
            {"matches('asdef*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('*def0*', property_values('"+propertyType+"::name'))", ALL_ENTITIES},
            {"matches('de', property_values('"+propertyType+"::name'))", NO_ENTITY},
            {"matches('hel*', property_values('"+propertyType+"::group'))", 2},
            // Is empty method
            {"property_values('"+propertyType+"::badtag').isEmpty()", ALL_ENTITIES},
            {"property_values('"+propertyType+"::name').isEmpty()", NO_ENTITY},
            {"property_values('"+propertyType+"::group').isEmpty()", 1},
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



    private static SeriesQuery createDummyQuery(String entityName) {
        SeriesQuery query = new SeriesQuery(entityName, metricName);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }
}
