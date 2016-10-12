package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.apache.commons.codec.binary.StringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import java.util.*;


public class MessageSeverityQueryTest extends MessageMethod {
    private static Message message;
    private static MessageQuery messageQuery;

    private static Set<String> stringSeverities = severities.keySet();

    @BeforeClass
    public void insertMessages() throws Exception {
        message = new Message("message-query-test-severity");
        message.setMessage("message-text");
        message.setDate(MIN_STORABLE_DATE);
        for (String severity : stringSeverities) {
            message.setSeverity(severity);
            insertMessageCheck(message);
            message.setDate(Util.addOneMS(message.getDate()));
        }
    }

    @BeforeMethod
    public void prepareQuery() {
        messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(MAX_QUERYABLE_DATE);

    }

    /*
         #2917
         unknown severity name or code raise error
    */
    @Test
    public void testUnknownSeverityRaiseError() throws Exception {
        Object unknownSeverities[] = new Object[]{"HELLO", 32};
        for (Object o : unknownSeverities) {
            messageQuery.setSeverity(String.valueOf(o));
            String response = queryMessage(messageQuery).readEntity(String.class);
            JSONObject error = new JSONObject(response);
            Assert.assertTrue(error.has("error"), "Error ir not raised");
        }
    }
    /*
         #2917
         alias processed correctly
    */
    @Test
    public void testAliasProcessedCorrectly() throws Exception {

        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            messageQuery.setSeverity(properCase(entry.getKey()));
            List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
            });
            String severity = messages.get(0).getSeverity();
            Assert.assertEquals(entry.getValue(), severity, "Alias processed wrong");

        }
    }
    /*
         #2917
         minSeverity is case insensitive
    */
    @Test
    public void testMinSeverityCaseInsensitive() throws Exception {
        for (Map.Entry<String, Integer> entry : severities.entrySet()) {
            messageQuery.setMinSeverity(properCase(entry.getKey()));
            List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
            });
            Integer minimumSeverity = entry.getValue();
            for (Message m : messages) {
                int actualSeverity = MessageMethod.severities.get(m.getSeverity());
                Assert.assertTrue(actualSeverity >= minimumSeverity, "Received severity (" + actualSeverity + ") should be greater than minSeverity (" + minimumSeverity + ")");
            }
        }
    }
    /*
         #2917
         severity is case insensitive
    */
    @Test
    public void testSeverityCaseInsensitive() throws Exception {
        for (String s : severities.keySet()) {
            messageQuery.setSeverity(properCase(s));
            List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
            });
            String severity = messages.get(0).getSeverity();
            Assert.assertEquals(s, severity, "Severity is case sensitive");
        }
    }
    /*
         #2917
         response contains severity as name (text) not as numeric code
    */
    @Test
    public void testResponseSeverityNotNumeric() throws Exception {
        for (String s : severities.keySet()) {
            messageQuery.setSeverity(s);
            List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
            });
            String severity = messages.get(0).getSeverity();
//            str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
            Assert.assertTrue(!severity.matches("-?\\d+(\\.\\d+)?"), "Received severity (" + severity + ") should not be numeric");
        }
    }
    /*
         #2917
         minSeverity is >= filter
    */
    @Test
    public void testMinSeverityFilter() throws Exception {
        for (Map.Entry<String, Integer> entry : MessageMethod.severities.entrySet()) {
            String key = entry.getKey();
            Integer minimumSeverity = entry.getValue();
            messageQuery.setMinSeverity(key);
            List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
            });
            for (Message m : messages) {
                int actualSeverity = MessageMethod.severities.get(m.getSeverity());
                Assert.assertTrue(actualSeverity >= minimumSeverity, "Received severity (" + actualSeverity + ") should be greater than minSeverity (" + minimumSeverity + ")");
            }
        }
    }
    /*
         #2917
         severities should return messages with the same severities names as in the request
    */
    @Test
    public void testActualSeveritiesCorrespondRequired() throws Exception {
        messageQuery.setSeverities(stringSeverities.toArray(new String[stringSeverities.size()]));
        List<Message> messages = queryMessage(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Assert.assertEquals(messages.size(), stringSeverities.size());
    }

    private String properCase(String inputVal) {
        if (inputVal.length() == 0) return "";
        if (inputVal.length() == 1) return inputVal.toUpperCase();
        return inputVal.substring(0, 1).toUpperCase()
                + inputVal.substring(1).toLowerCase();
    }
}