package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.Collections;

import static javax.ws.rs.core.Response.Status.OK;


public class MessageMethod extends BaseMethod {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    private static final String METHOD_MESSAGE_QUERY = "/messages/query";

    public static boolean insertMessages(final Message message, long sleepDuration) throws IOException, InterruptedException {

        Response response = httpApiResource.path(METHOD_MESSAGE_INSERT).request().post(Entity.entity(Collections.singletonList(message), MediaType.APPLICATION_JSON_TYPE));
        Thread.sleep(sleepDuration);
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        return OK.getStatusCode() == response.getStatus();
    }

    protected boolean insertMessages(final Message message) throws IOException, ParseException, InterruptedException {
        return insertMessages(message, 0);
    }

    public static String executeQuery(final MessageQuery messageQuery) throws IOException, ParseException {
        Response response = httpApiResource.path(METHOD_MESSAGE_QUERY).request().post(Entity.entity(Collections.singletonList(messageQuery), MediaType.APPLICATION_JSON_TYPE));
        if (OK.getStatusCode() == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute message query");
        }
        return response.readEntity(String.class);
    }

    public static String getField(String message, int index, String field) throws JSONException, IOException {
        if (message == null) {
            return "message is null";
        }
        logger.debug("Lookign for field: {} in block num: {}\nIn message: {}", field, index, message);
        return (((JSONObject) (new JSONArray(message)).get(index)).get(field)).toString();
    }
}
