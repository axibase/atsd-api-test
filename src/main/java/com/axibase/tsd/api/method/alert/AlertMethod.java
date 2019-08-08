package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;

import javax.ws.rs.HttpMethod;
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

    private static <T> Response executeRequest(String path, List<T> query, RequestSenderWithAuthorization sender) { //Alert's endpoints all don't need templates and have POST method
        Response response = sender.executeApiRequest(path, HttpMethod.POST, Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryAlerts(T... queries) {
        return executeRequest(METHOD_ALERTS_QUERY, Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response queryAlerts(List<T> queries, String token) {
        return executeRequest(METHOD_ALERTS_QUERY, queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response updateAlerts(T... queries) {
        return executeRequest(METHOD_ALERTS_UPDATE, Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response updateAlerts(List<T> queries, String token) {
        return executeRequest(METHOD_ALERTS_UPDATE, queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response deleteAlerts(T... queries) {
        return executeRequest(METHOD_ALERTS_DELETE, Arrays.asList(queries), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response deleteAlerts(List<T> queries, String token) {
        return executeRequest(METHOD_ALERTS_DELETE, queries, new RequestSenderWithBearerAuthorization(token));
    }

    public static Response queryHistoryResponseRawJSON(String json) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_ALERTS_HISTORY_QUERY)
                .request()
                .post(Entity.entity(json, MediaType.APPLICATION_JSON)));
        response.bufferEntity();
        return response;
    }

    public static <T> Response queryHisttoryResponse(List<T> queries, String token) {
        return executeRequest(METHOD_ALERTS_HISTORY_QUERY, queries, new RequestSenderWithBearerAuthorization(token));
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
