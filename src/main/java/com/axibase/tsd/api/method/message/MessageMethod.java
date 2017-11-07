package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class MessageMethod extends BaseMethod {
    private static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    private static final String METHOD_MESSAGE_QUERY = "/messages/query";
    private static final String METHOD_MESSAGE_STATS_QUERY = "/messages/stats/query";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Response insertMessageReturnResponse(final Message message) {
        return insertMessageReturnResponse(Collections.singletonList(message));
    }

    public static Response insertMessageReturnResponse(List<Message> messageList) {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_MESSAGE_INSERT)
                .request()
                .post(Entity.json(messageList)));
        response.bufferEntity();
        return response;
    }

    public static Boolean insertMessage(final Message message) throws Exception {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_MESSAGE_INSERT)
                .request()
                .post(Entity.json(Collections.singletonList(message))));
        response.bufferEntity();
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        return OK.getStatusCode() == response.getStatus();
    }

    public static void insertMessageCheck(final Message message, AbstractCheck check) throws Exception {
        Response response = insertMessageReturnResponse(message);
        response.bufferEntity();
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IllegalStateException(response.readEntity(String.class));
        }
        Checker.check(check);
    }

    public static void insertMessageCheck(final Message message) throws Exception {
        insertMessageCheck(message, new MessageCheck(message));
    }

    public static <T> Response queryMessageStats(T... query) throws Exception {
        Response response = executeApiRequest(webTarget -> webTarget
                .path(METHOD_MESSAGE_STATS_QUERY)
                .request()
                .post(Entity.json(query)));
        response.bufferEntity();
        return response;
    }

    public static <T> List<Series> queryMessageStatsReturnSeries(T... query) throws Exception {
        Response response = queryMessageStats(query);
        if (response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute queryMessageStats");
        }
        return response.readEntity(new GenericType<List<Series>>() {
        });
    }

    public static List<Message> queryMessage(MessageQuery... queries) {
        return queryMessageResponse(queries).readEntity(new GenericType<List<Message>>() {
        });
    }

    public static <T> Response queryMessageResponse(T... messageQuery) {
        Entity<T[]> json = Entity.json(messageQuery);
        Response response = executeApiRequest(webTarget -> webTarget.path(METHOD_MESSAGE_QUERY).request().post(json));
        response.bufferEntity();
        return response;
    }

    public static boolean messageExist(final Message message) throws Exception {
        MessageQuery query = new MessageQuery();
        query.setEntity(message.getEntity());
        query.setType(message.getType());
        if (message.getDate() != null) {
            query.setStartDate(message.getDate());
            query.setEndDate(Util.addOneMS(message.getDate()));
        }
        query.setSeverity(message.getSeverity());
        query.setSource(message.getSource());

        Response response = queryMessageResponse(query);
        if (response.getStatus() != OK.getStatusCode() && response.getStatus() != NOT_FOUND.getStatusCode()) {
            throw new IllegalStateException("Fail to execute queryMessageResponse request: " + response.readEntity(String.class));
        }

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(message));
        final String given = response.readEntity(String.class);
        return compareJsonString(expected, given);
    }
}
