package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.alert.Alert;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.*;

public class AlertMethod extends BaseMethod {
    private static final String METHOD_ALERTS_QUERY = "/alerts/query";
    private static final String METHOD_ALERTS_UPDATE = "/alerts/update";
    private static final String METHOD_ALERTS_DELETE = "/alerts/delete";
    private static final String METHOD_ALERTS_HISTORY_QUERY = "/alerts/history/query";

    public static <T> Response queryAlerts(T... queries) {
        Entity<List<T>> json = Entity.json(Arrays.asList(queries));
        System.out.println(json);
        Response response = httpApiResource
                .path(METHOD_ALERTS_QUERY)
                .request()
                .post(json);
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateAlerts(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_UPDATE)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static boolean alertExist(String entityName, String metricName) throws Exception {
        Map<String, Object> query = new HashMap<>();
        query.put("entity", entityName);
        query.put("metrics", Collections.singletonList(metricName));
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        Alert alert = new Alert();
        alert.setEntity(entityName);
        alert.setMetric(metricName);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(alert));
        final String given = queryAlerts(query).readEntity(String.class);
        return compareJsonString(expected, given);
    }


    public static <T> Response deleteAlerts(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_DELETE)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryAlertsHistory(T... queries) {
        Response response = httpApiResource
                .path(METHOD_ALERTS_HISTORY_QUERY)
                .request()
                .post(Entity.json(Arrays.asList(queries)));
        response.bufferEntity();
        return response;
    }
}
