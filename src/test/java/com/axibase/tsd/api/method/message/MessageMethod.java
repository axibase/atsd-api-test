package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class MessageMethod extends Method {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    static final String METHOD_MESSAGE_QUERY = "/messages/query";
    private JSONParser jsonParser = new JSONParser();

    protected Boolean insertMessages(final Message message, long sleepDuration) throws IOException, ParseException, InterruptedException  {
        JSONArray request = new JSONArray() {{
            add(jsonParser.parse(jacksonMapper.writeValueAsString(message)));
        }};

        Response response = httpResource.path(METHOD_MESSAGE_INSERT).request().post(Entity.entity(request.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        Thread.sleep(sleepDuration);
        if (200 == response.getStatus()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        return 200 == response.getStatus();
    }

    protected Boolean insertMessages(final Message message) throws IOException, ParseException, InterruptedException  {
        return insertMessages(message,0);
    }

    protected String executeQuery(final MessageQuery messageQuery) throws IOException, ParseException{
        JSONObject request = (JSONObject) jsonParser.parse(jacksonMapper.writeValueAsString(messageQuery));

        Response response = httpResource.path(METHOD_MESSAGE_QUERY).request().post(Entity.entity(request.toJSONString(), MediaType.APPLICATION_JSON_TYPE));
        if (200 == response.getStatus()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute message query");
        }
        return response.readEntity(String.class);
    }

    protected String getField(String message, int index, String field) throws ParseException {
        if (message == null) {
            return "message is null";
        }
        return (((JSONObject) ((JSONArray) jsonParser.parse(message)).get(index)).get(field)).toString();
    }
}
