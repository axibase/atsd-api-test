package com.axibase.tsd.api.method.alert;


import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.util.Registry;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import java.util.*;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static javax.ws.rs.core.Response.Status.OK;

public class AlertQueryAcknowledgedTest extends AlertTest {
    private static final String ENTITY_NAME = "alert-query-ack-entity-1";
    private static final String ENTITY_NAME_ACK = "alert-query-ack-entity-1-ack";

    @BeforeClass
    public void prepareAlertData() throws Exception {
        Registry.Entity.register(ENTITY_NAME);
        generateAlertForEntity(ENTITY_NAME);

        Registry.Entity.register(ENTITY_NAME_ACK);
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
        if (updateAlerts(updateAlertsCommand.toArray()).getStatus() != OK.getStatusCode()) {
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


    /**
     * #2976
     */
    @Test
    public void testAcknowledgedFilterTrue() throws Exception {
        Map<String, Object> alertQuery = new HashMap<>();
        alertQuery.put("entities", Arrays.asList(ENTITY_NAME, ENTITY_NAME_ACK));
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);
        alertQuery.put("acknowledged", true);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });
        for (Alert alert : alertList) {
            Assert.assertTrue(alert.getAcknowledged(), "Response should not contain acknowledged=false alerts");
        }
    }

    /**
     * #2976
     */
    @Test
    public void testAcknowledgedFilterFalse() throws Exception {
        Map<String, Object> alertQuery = new HashMap<>();
        alertQuery.put("entities", Arrays.asList(ENTITY_NAME, ENTITY_NAME_ACK));
        alertQuery.put("startDate", MIN_QUERYABLE_DATE);
        alertQuery.put("endDate", MAX_QUERYABLE_DATE);
        alertQuery.put("acknowledged", false);

        List<Alert> alertList = queryAlerts(alertQuery).readEntity(new GenericType<List<Alert>>() {
        });

        for (Alert alert : alertList) {
            Assert.assertFalse(alert.getAcknowledged(), "Response should not contain acknowledged=true alerts");
        }
    }


}
