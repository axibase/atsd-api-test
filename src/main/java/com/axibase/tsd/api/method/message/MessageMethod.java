package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.util.authorization.RequestSenderWithAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBasicAuthorization;
import com.axibase.tsd.api.util.authorization.RequestSenderWithBearerAuthorization;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class MessageMethod extends BaseMethod {
    private static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    private static final String METHOD_MESSAGE_QUERY = "/messages/query";
    private static final String METHOD_MESSAGE_STATS_QUERY = "/messages/stats/query";

    private static<T> Response executeRequest(String path, List<T> query, RequestSenderWithAuthorization sender) { //Message's endpoints all don't need templates and have POST method
        Response response = sender.executeApiRequest(path, HttpMethod.POST, Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static Response insertMessageReturnResponse(final Message message) {
        return insertMessageReturnResponse(Collections.singletonList(message));
    }

    public static Response insertMessageReturnResponse(final Message message, String token) {
        return insertMessageReturnResponse(Collections.singletonList(message), new RequestSenderWithBearerAuthorization(token));
    }

    public static Response insertMessageReturnResponse(List<Message> messageList) {
        return insertMessageReturnResponse(messageList, RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static Response insertMessageReturnResponse(List<Message> messageList, RequestSenderWithAuthorization sender) {
        return executeRequest(METHOD_MESSAGE_INSERT, messageList, sender);
    }

    public static void insertMessageCheck(final Message message, AbstractCheck check) {
        Response response = insertMessageReturnResponse(message);
        response.bufferEntity();
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException(response.readEntity(String.class));
        }
        Checker.check(check);
    }

    public static void insertMessageCheck(final Message message) {
        insertMessageCheck(message, new MessageCheck(message));
    }

    public static <T> Response queryMessageStats(List<T> query, String token) {
        return queryMessageStats(query, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response queryMessageStats(T... query) {
        return queryMessageStats(Arrays.asList(query), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response queryMessageStats(List<T> query, RequestSenderWithAuthorization sender) {
        return executeRequest(METHOD_MESSAGE_STATS_QUERY,query, sender);
    }

    public static <T> List<Series> queryMessageStatsReturnSeries(T... query) {
        Response response = queryMessageStats(query);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            throw new IllegalStateException("Fail to execute queryMessageStats");
        }
        return response.readEntity(ResponseAsList.ofSeries());
    }

    public static List<Message> queryMessage(MessageQuery... queries) {
        return queryMessageResponse(queries).readEntity(ResponseAsList.ofMessages());
    }

    public static<T> Response queryMessageResponse(List<T> query, String token) {
        return queryMessageResponse(query, new RequestSenderWithBearerAuthorization(token));
    }

    public static <T> Response queryMessageResponse(T... messageQuery) {
        return queryMessageResponse(Arrays.asList(messageQuery), RequestSenderWithBasicAuthorization.DEFAULT_BASIC_SENDER);
    }

    public static <T> Response queryMessageResponse(List<T> query, RequestSenderWithAuthorization sender) {
        return executeRequest(METHOD_MESSAGE_QUERY, query, sender);
    }

    /**
     * Build message query for provided message, execute the query,
     * and check that response json is the same as the message json.
     * Although the lenient checking (may be) performed,
     * the method will return false if
     * some message fields (type, severity, date, etc.) are not specified,
     * but are set to defaults when stored in ATSD.
     */
    public static boolean messageExist(final Message message) throws Exception {
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(message));
        final String given = queryMessageAsString(message);
        return compareJsonString(expected, given);
    }

    /**
     * Build message query for provided message, execute the query, parse response as a Message,
     * and check that parsed message and provided message have the same values of specified fields.
     */
    public static boolean messageExistWithSameFields(final Message message, String... fieldNames) {
        final Message[] actualMessages = queryMessage(message);
        if (actualMessages.length != 1) return false;
        Message actualMessage = actualMessages[0];
        return Arrays.stream(fieldNames)
                .allMatch(field -> message.getStringField(field).equals(actualMessage.getStringField(field)));
    }

    /**
     * Build message query for provided message, execute the query, and read response as a Message[].
     */
    public static Message[] queryMessage(final Message message) {
        return queryMessage(message, Message[].class);
    }

    /**
     * Build message query for provided message, execute the query, and read response as a String.
     */
    public static String queryMessageAsString(final Message message) {
        return queryMessage(message, String.class);
    }

    private static <T> T queryMessage(final Message message, Class<T> returnType) {
        MessageQuery query = buildQuery(message);
        Response response = queryMessageResponse(query);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response) && response.getStatus() != NOT_FOUND.getStatusCode()) {
            throw new IllegalStateException("Fail to execute queryMessageResponse request: " + response.readEntity(String.class));
        }
        return response.readEntity(returnType);
    }

    private static MessageQuery buildQuery(final Message message) {
        MessageQuery query = new MessageQuery();
        query.setEntity(message.getEntity());
        query.setType(message.getType());
        if (message.getDate() != null) {
            query.setStartDate(message.getDate());
            query.setEndDate(Util.addOneMS(message.getDate()));
        }
        query.setSeverity(message.getSeverity());
        query.setSource(message.getSource());
        return query;
    }
}
