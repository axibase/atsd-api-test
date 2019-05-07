package com.axibase.tsd.api.method.alert;


import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.json.JSONArray;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.TestUtil.extractJSONObjectFieldFromJSONArrayByIndex;
import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.*;

public class AlertQueryTest extends AlertTest {


    @Issue("2991")
    @Test
    public void testEntityWildcardStarChar() throws Exception {
        final String entityName = "alert-query-entity-1";
        Registry.Entity.checkExists(entityName);
        generateAlertForEntity(entityName);

        Map<String, String> query = new HashMap<>();
        query.put("entity", "alert-query-entity*");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to get alerts by entity expression", calculateJsonArraySize(response.readEntity(String.class)) > 0);
    }

    @Issue("2979")
    @Test
    public void testEntitiesWildcardStartChar() throws Exception {
        final String entityName = "alert-query-entity-2";
        Registry.Entity.checkExists(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("alert-query-entity*"));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to get alerts by entity expression", calculateJsonArraySize(response.readEntity(String.class)) > 0);
    }

    @Issue("2979")
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        final String entityName = "alert-query-entity-3";
        Registry.Entity.checkExists(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("alert-query-entity-?"));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to get alerts by entity expression", calculateJsonArraySize(response.readEntity(String.class)) > 0);
    }

    @Issue("2981")
    @Test
    public void testEntityExpressionFilterExist() throws Exception {
        final String entityName = "alert-query-entity-4";
        Registry.Entity.checkExists(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> query = new HashMap<>();
        query.put("entityExpression", "name LIKE '*rt-query-entity-4'");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        Response response = queryAlerts(query);

        assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to get alerts by entity expression", calculateJsonArraySize(response.readEntity(String.class)) > 0);
    }

    @Issue("2993")
    @Test
    public void testUnknownEntityNotAffectProcessingOthers() throws Exception {
        final String entityName = "alert-query-entity-5";
        Registry.Entity.checkExists(entityName);
        generateAlertForEntity(entityName);

        Map<String, Object> qExist = new HashMap<>();
        qExist.put("entity", entityName);
        qExist.put("startDate", MIN_QUERYABLE_DATE);
        qExist.put("endDate", MAX_QUERYABLE_DATE);

        Map<String, Object> qUnknown = new HashMap<>();
        qUnknown.put("entity", "UNKNOWN");
        qUnknown.put("startDate", MIN_QUERYABLE_DATE);
        qUnknown.put("endDate", MAX_QUERYABLE_DATE);

        Response response = queryAlerts(qExist, qUnknown);
        JSONArray jsonResponse = new JSONArray(response.readEntity(String.class));

        assertSame(Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertTrue("Fail to get alerts by queries with unknown entity", jsonResponse.length() == 2);
        assertEquals("Unexpected warning message", "ENTITY not found for name: 'unknown'",
                extractJSONObjectFieldFromJSONArrayByIndex(1, "warning", jsonResponse));
    }
}
