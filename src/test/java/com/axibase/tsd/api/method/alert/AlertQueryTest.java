package com.axibase.tsd.api.method.alert;


import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class AlertQueryTest extends AlertMethod {


    /**
     * #2991
     */
    @Test
    public void testEntityWildcardStarChar() throws Exception {
        final String entityName = "alert-query-entity-1";
        Registry.Entity.register(entityName);
        generateAlertForEntity(entityName);

        Map<String, String> query = new HashMap<>();
        query.put("entity", "alert-query-entity*");
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardStartChar() throws Exception {
        final String entityName = "alert-query-entity-2";
        Registry.Entity.register(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("alert-query-entity*"));
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        final String entityName = "alert-query-entity-3";
        Registry.Entity.register(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("alert-query-entity-?"));
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

    /**
     * #2981
     */
    @Test
    public void testEntityExpressionFilterExist() throws Exception {
        final String entityName = "alert-query-entity-4";
        Registry.Entity.register(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entityExpression", "name LIKE '*rt-query-entity-4'");
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

}
