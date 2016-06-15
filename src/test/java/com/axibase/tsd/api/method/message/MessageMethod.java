package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.Method;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.transport.http.AtsdHttpResponse;
import com.axibase.tsd.api.transport.http.HTTPMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class MessageMethod extends Method {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static final String METHOD_MESSAGE_INSERT = "/messages/insert";
    static final String METHOD_MESSAGE_QUERY = "/messages/query";
    private JSONArray returnedMessages;
    private JSONParser jsonParser = new JSONParser();

    protected Boolean insertMessages(final Message message) throws IOException, ParseException {

        JSONArray request = new JSONArray() {{
            add(jsonParser.parse(jacksonMapper.writeValueAsString(message)));
        }};

        AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_MESSAGE_INSERT, request.toJSONString());
        if (200 == response.getCode()) {
            logger.debug("Message looks inserted");
        } else {
            logger.error("Fail to insert message");
        }
        return 200 == response.getCode();
    }

    protected Boolean executeQuery(final MessageQuery messageQuery) throws Exception {
        Thread.sleep(500);

        JSONObject request = (JSONObject) jsonParser.parse(jacksonMapper.writeValueAsString(messageQuery));

        final AtsdHttpResponse response = httpSender.send(HTTPMethod.POST, METHOD_MESSAGE_QUERY, request.toJSONString());
        if (200 == response.getCode()) {
            logger.debug("Query looks succeeded");
        } else {
            logger.error("Failed to execute message query");
        }
        returnedMessages = (JSONArray) jsonParser.parse(response.getBody());
        return 200 == response.getCode();
    }

    protected String getDataField(int index, String field) {
        if (returnedMessages == null) {
            return "returnedMessages is null";
        }
        return (((JSONObject) returnedMessages.get(index)).get(field)).toString();
    }

}
