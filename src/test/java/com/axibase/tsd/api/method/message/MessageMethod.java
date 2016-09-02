package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.series.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

public class MessageMethod extends BaseMethod {
    static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    static final String METHOD_MESSAGE_QUERY = "/messages/query";
    static final String METHOD_MESSAGE_STATS_QUERY = "/messages/stats/query";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static <T> Response queryMessageStats(T... query) throws Exception {
        Response response = httpApiResource.path(METHOD_MESSAGE_STATS_QUERY).request().post(Entity.json(query));
        response.bufferEntity();
        return response;
    }

    public static <T> List<Series> queryMessageStatsReturnSeries(T... query) throws Exception {
        Response response = queryMessageStats(query);
        if(response.getStatus() != OK.getStatusCode()) {
            throw new IllegalStateException("Fail to execute queryMessageStats");
        }
        List<Series> serieses = response.readEntity(new GenericType<List<Series>>() {
        });
        return serieses;
    }

    public static Response insertMessageReturnResponse(final Message message) {
        return insertMessageReturnResponse(Collections.singletonList(message));
    }

    public static Response insertMessageReturnResponse(List<Message> messageList) {
        Response response = httpApiResource.path(METHOD_MESSAGE_INSERT).request().post(Entity.json(messageList));
        response.bufferEntity();
        return response;
    }

    public static Boolean insertMessage(final Message message, long sleepDuration) throws Exception {
        return insertMessage(Collections.singletonList(message), sleepDuration);
    }

    public static Boolean insertMessage(List<Message> messageList, long sleepDuration) throws Exception {
        Response response = httpApiResource.path(METHOD_MESSAGE_INSERT).request().post(Entity.json(messageList));
        response.bufferEntity();
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        Thread.sleep(sleepDuration); //give ATSD a few time to handle message
        return OK.getStatusCode() == response.getStatus();
    }

    public static Boolean insertMessage(final Message message) throws Exception {
        return insertMessage(message, 0);
    }

    public static boolean messageExist(final Message message) throws Exception {
        MessageQuery query = new MessageQuery();
        query.setEntity(message.getEntity());
        query.setType(message.getType());
        query.setStartDate(message.getDate());
        query.setEndDate(Util.addOneMS(message.getDate()));
        query.setSeverity(message.getSeverity());
        query.setSource(message.getSource());

        Response response = queryMessage(query);
        if(response.getStatus() != OK.getStatusCode() && response.getStatus() != NOT_FOUND.getStatusCode()) {
            throw new IllegalStateException("Fail to execute queryMessage request: " + response.readEntity(String.class));
        }

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(message));
        final String given = response.readEntity(String.class);
        return compareJsonString(expected, given);
    }


    public static void insertMessageCheck(final Message message) throws Exception {
        Response response = insertMessageReturnResponse(message);
        response.bufferEntity();
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IllegalStateException(response.readEntity(String.class));
        }
        response.close();

        final long startCheckTimeMillis = System.currentTimeMillis();
        do {
            if ((messageExist(message))) {
                return;
            }
            Thread.sleep(BaseMethod.REQUEST_INTERVAL);
        } while (System.currentTimeMillis() <= startCheckTimeMillis + BaseMethod.EXPECTED_PROCESSING_TIME);
        if (!messageExist(message)) {
            throw new Exception("Fail to check inserted messages");
        }
    }

    public static <T> Response queryMessage(T... messageQuery) {
        Response response = httpApiResource.path(METHOD_MESSAGE_QUERY).request().post(Entity.json(messageQuery));
        response.bufferEntity();
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute message query");
        }
        return response;
    }
}
