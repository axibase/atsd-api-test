package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.BaseMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.OK;

public class AlertHistoryQueryTest extends AlertMethod {
    private final static String ALERTHISTORY_ENTITY_NAME = "alert-historyquery-entity-1";

    @BeforeClass
    public void generateAlertHistory() throws Exception {
        Registry.Entity.register(ALERTHISTORY_ENTITY_NAME);
        generateAlertForEntity(ALERTHISTORY_ENTITY_NAME);
        Thread.sleep(EXPECTED_PROCESSING_TIME);
    }

    /**
     * #2991
     */
    @Test
    public void testEntityWildcardStarChar() throws Exception {
        Map<String, String> query = new HashMap<>();
        query.put("entity", "alert-historyquery-entity*");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);


        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(response.readEntity(String.class)) > 0, "Fail to get alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardStarChar() throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("entities", Collections.singletonList("alert-historyquery-entity*"));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(response.readEntity(String.class)) > 0, "Fail to get any alerts by entity expression");
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("entities", Collections.singletonList("alert-historyquery-entity-?"));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);


        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(response.readEntity(String.class)) > 0, "Fail to get any alerts by entity expression");
    }

    /**
     * #2981
     */
    @Test
    public void testEntityExpressionFilterExist() throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("entityExpression", "name LIKE '*rt-historyquery-entity-1'");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlertsHistory(query);

        Assert.assertEquals(response.getStatus(), OK.getStatusCode());
        Assert.assertTrue(calculateJsonArraySize(response.readEntity(String.class)) > 0, "Fail to get alerts by entity expression");
    }

}
