package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

public class AlertMethod extends BaseMethod {
    private static final String METHOD_ALERTS_QUERY = "/alerts/query";
    private static final String METHOD_ALERTS_UPDATE = "/alerts/update";
    private static final String METHOD_ALERTS_DELETE = "/alerts/delete";
    private static final String METHOD_ALERTS_HISTORY_QUERY = "/alerts/history/query";

    public static <T> Response queryAlerts(T... queries) {
        Entity<List<T>> json = Entity.json(Arrays.asList(queries));
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_QUERY)
                .request()
                .post(json));
        response.bufferEntity();
        return response;
    }

    public static <T> Response updateAlerts(T... queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_UPDATE)
                .request()
                .post(Entity.json(Arrays.asList(queries))));
        response.bufferEntity();
        return response;
    }

    public static <T> Response deleteAlerts(T... queries) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_DELETE)
                .request()
                .post(Entity.json(Arrays.asList(queries))));
        response.bufferEntity();
        return response;
    }

    public static Response queryHistoryResponseRawJSON(String json) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_HISTORY_QUERY)
                .request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON)));
        response.bufferEntity();
        return response;
    }

    public static List<Alert> queryHistory(List<AlertHistoryQuery> queryList) {
        Response response = queryHistoryResponse(queryList);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            String errorMessage = String.format(
                    "Failed to execute alert history query. Query: %s",
                    queryList
            );
            throw new IllegalStateException(errorMessage);
        } else {
            return response.readEntity(ResponseAsList.ofAlerts());
        }
    }

    public static List<Alert> queryHistory(AlertHistoryQuery... queries) {
        return queryHistory(Arrays.asList(queries));
    }

    private static Response queryHistoryResponse(List<AlertHistoryQuery> queryList) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_HISTORY_QUERY)
                .request()
                .post(Entity.json(queryList)));
        response.bufferEntity();
        return response;
    }
}
