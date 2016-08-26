package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class AlertHistoryQueryTest extends AlertMethod {

    /**
     * #2991
     */
    @Test
    public void testEntityWildcardStarChar() throws Exception {
        final String entityName = "alert-historyquery-entity-1";
        Registry.Entity.register(entityName);
        generateAlertHistoryForEntity(entityName);

        Map<String, String> query = new HashMap<>();
        query.put("entity", "alert-historyquery-entity*");
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardStarChar() throws Exception {
        final String entityName = "alert-historyquery-entity-2";
        Registry.Entity.register(entityName);
        generateAlertHistoryForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Collections.singletonList("alert-historyquery-entity*"));
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get any alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        final String entityName = "alert-historyquery-entity-3";
        Registry.Entity.register(entityName);
        generateAlertHistoryForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Collections.singletonList("alert-historyquery-entity-?"));
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get any alerts by entity expression");
    }

    /**
     * #2981
     */
    @Test
    public void testEntityExpressionFilterExist() throws Exception {
        final String entityName = "alert-history-query-entity-4";
        Registry.Entity.register(entityName);
        generateAlertHistoryForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entityExpression", "name LIKE '*rt-history-query-entity-4'");
        query.put("startDate", Util.MIN_QUERYABLE_DATE);
        query.put("endDate", Util.MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(formatToJsonString(response)) > 0, "Fail to get alerts by entity expression");
    }

}
