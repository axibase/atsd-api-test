package com.axibase.tsd.api.method.alert;


import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class AlertQueryAcknowledgedTest extends AlertTest {
    private static final String ENTITY_NAME = "alert-query-ack-entity-1";
    private static final String ENTITY_NAME_ACK = "alert-query-ack-entity-1-ack";

    @BeforeClass
    public void prepareAlertData() throws Exception {
        Registry.Entity.checkExists(ENTITY_NAME);
        generateAlertForEntity(ENTITY_NAME);

        Registry.Entity.checkExists(ENTITY_NAME_ACK);
        generateAlertForEntity(ENTITY_NAME_ACK);

        markAlertAcknowledged(ENTITY_NAME_ACK);
        checkAllAcknowledgedTypesExist();
    }

    private void markAlertAcknowledged(final String entityName) {
        Map<String, String> alertQuery = new HashMap<>();
        alertQuery.put("entity", entityName);
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });
        List<Map<String, Object>> updateAlertsCommand = new ArrayList<>();

        Map<String, Object> item;
        for (final Alert alert : alertList) {
            item = new HashMap<>();
            item.put("acknowledged", true);
            item.put("id", alert.getId());
            updateAlertsCommand.add(item);
        }
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(updateAlerts(updateAlertsCommand.toArray()))) {
            throw new IllegalStateException("Fail to set alert acknowledged");
        }
    }

    private void checkAllAcknowledgedTypesExist() {
        Map<String, Object> alertQuery = new HashMap<>();
        alertQuery.put("entities", Arrays.asList(ENTITY_NAME, ENTITY_NAME_ACK));
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });

        Boolean acknowledgedFalseExist = false;
        Boolean acknowledgedTrueExist = false;
        for (Alert alert : alertList) {
            if (!alert.getAcknowledged()) {
                acknowledgedFalseExist = true;
            } else {
                acknowledgedTrueExist = true;
            }
        }
        if (!acknowledgedFalseExist || !acknowledgedTrueExist) {
            throw new IllegalStateException("Both acknowledged types should exist to run test.");
        }
    }


    @Issue("2976")
    @Test
    public void testAcknowledgedFilterTrue() {
        Map<String, Object> alertQuery = new HashMap<>();
        alertQuery.put("entities", Arrays.asList(ENTITY_NAME, ENTITY_NAME_ACK));
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);
        alertQuery.put("acknowledged", true);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });
        for (Alert alert : alertList) {
            assertTrue("Response should not contain acknowledged=false alerts", alert.getAcknowledged());
        }
    }

    @Issue("2976")
    @Test
    public void testAcknowledgedFilterFalse() {
        Map<String, Object> alertQuery = new HashMap<>();
        alertQuery.put("entities", Arrays.asList(ENTITY_NAME, ENTITY_NAME_ACK));
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);
        alertQuery.put("acknowledged", false);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });

        for (Alert alert : alertList) {
            assertFalse("Response should not contain acknowledged=true alerts", alert.getAcknowledged());
        }
    }


}
