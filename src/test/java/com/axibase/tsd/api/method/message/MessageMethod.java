package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

public class MessageMethod extends BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    static final String METHOD_MESSAGE_QUERY = "/messages/query";

    public static Boolean insertMessage(final Message message, long sleepDuration) throws IOException, InterruptedException {
        return insertMessage(Collections.singletonList(message), sleepDuration);
    }

    public static Boolean insertMessage(List<Message> messageList, long sleepDuration) throws IOException, InterruptedException {
        Response response = httpApiResource.path(METHOD_MESSAGE_INSERT).request().post(Entity.entity(messageList, MediaType.APPLICATION_JSON_TYPE));
        response.close();
        Thread.sleep(sleepDuration);
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        return OK.getStatusCode() == response.getStatus();
    }

    public static Boolean insertMessage(final Message message) throws IOException, InterruptedException {
        return insertMessage(message, 0);
    }

    public static Response executeQuery(final MessageQuery messageQuery) throws IOException {
        Response response = httpApiResource.path(METHOD_MESSAGE_QUERY).request().post(Entity.entity(Collections.singletonList(messageQuery), MediaType.APPLICATION_JSON_TYPE));
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute message query");
        }
        return response;
    }
}
