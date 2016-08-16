package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

public class MessageMethod extends BaseMethod {
    static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    static final String METHOD_MESSAGE_QUERY = "/messages/query";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    public static void insertMessageCheck(final Message message) throws Exception {
        //todo: replace with normal check
        Response response = insertMessageReturnResponse(message);
        response.bufferEntity();
        if (OK.getStatusCode() != response.getStatus()) {
            throw new IllegalStateException(response.readEntity(String.class));
        }
        response.close();

        Thread.sleep(1000L); //wait for message to be inserted
    }

    public static <T> Response executeQuery(T... messageQuery) {
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
