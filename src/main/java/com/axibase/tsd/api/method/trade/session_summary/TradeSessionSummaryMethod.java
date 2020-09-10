package com.axibase.tsd.api.method.trade.session_summary;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

public class TradeSessionSummaryMethod extends BaseMethod {
    public static final String PATH = "/trade-session-summary";
    private static final String METHOD_SESSION_SUMMARY_IMPORT = PATH + "/import";
    private static final String METHOD_SESSION_SUMMARY_EXPORT = PATH + "/export";

    public static Response importStatistics(List<TradeSessionSummary> queries, RequestSenderWithAuthorization sender) {
        Response response = sender.executeApiRequest(METHOD_SESSION_SUMMARY_IMPORT, HttpMethod.POST, Entity.json(queries));
        response.bufferEntity();
        return response;
    }

    public static Response importStatistics(List<TradeSessionSummary> queries) {
        return importStatistics(queries, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response importStatistics(TradeSessionSummary... queries) {
        return importStatistics(Arrays.asList(queries));
    }
}