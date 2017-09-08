package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.util.TestUtil;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.*;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class MessageQueryTest extends MessageMethod {
    private static final Message message;

    static {
        message = new Message("message-query-test-timezone");
        message.setMessage("hello");
        message.setDate("2017-05-21T00:00:00.000Z");
    }

    @BeforeMethod
    public void prepare() throws Exception {
        insertMessageCheck(message);
    }


    /**
     * #2850
     */
    @Test
    public void testISOTimezoneZ() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2017-05-21T00:00:00Z");

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2017-05-21T01:23:00+01:23");

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2017-05-20T22:37:00-01:23");

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /**
     * #2850
     */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2017-07-21 00:00:00");

        Response response = queryMessageResponse(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2017-07-21 00:00:00\"}", response.readEntity(String.class), true);

    }

    /**
     * #2850
     */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2017-07-20T22:50:00-0110");

        Response response = queryMessageResponse(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2017-07-20T22:50:00-0110\"}", response.readEntity(String.class), true);
    }

    /**
     * #2850
     */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("1500595200000");

        Response response = queryMessageResponse(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1500595200000\"}", response.readEntity(String.class), true);
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardStarChar() throws Exception {
        Message message = new Message("message-query-wildcard-2-1");
        message.setMessage("msgtext");
        message.setDate("2017-01-01T00:00:00.000Z");
        insertMessageCheck(message);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("message-query-wildcard-2*"));
        query.put("startDate", message.getDate());
        query.put("endDate", TestUtil.addOneMS(message.getDate()));

        final String given = queryMessageResponse(query).readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(message));
        assertTrue("Message in response does not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #2979
     */
    @Test
    public void testEntitiesWildcardQuestionChar() throws Exception {
        Message message = new Message("message-query-wildcard-3-1");
        message.setMessage("msgtext");
        message.setDate("2017-01-01T00:00:00.000Z");
        insertMessageCheck(message);

        Map<String, Object> query = new HashMap<>();
        query.put("entities", Arrays.asList("message-query-wildcard-3-?"));
        query.put("startDate", message.getDate());
        query.put("endDate", TestUtil.addOneMS(message.getDate()));

        final String given = queryMessageResponse(query).readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Arrays.asList(message));
        assertTrue("Message in response does not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #2979
     */
    @Test
    public void testEntityEntitiesWildcardSame() throws Exception {
        Message message = new Message("message-query-wildcard-4-1");
        message.setMessage("msgtext");
        message.setDate("2017-01-01T00:00:00.000Z");
        insertMessageCheck(message);
        message.setEntity("message-query-wildcard-4-2");
        insertMessageCheck(message);
        message.setEntity("message-query-wildcard-4-3");
        insertMessageCheck(message);

        final String pattern = "message-query-wildcard-4*";

        Map<String, Object> query = new HashMap<>();
        query.put("entity", pattern);
        query.put("startDate", message.getDate());
        query.put("endDate", TestUtil.addOneMS(message.getDate()));

        final String entitiesResponse = queryMessageResponse(query).readEntity(String.class);

        query.remove("entity");
        query.put("entities", Collections.singletonList(pattern));

        final String entityResponse = queryMessageResponse(query).readEntity(String.class);
        assertEquals("Message in response does not match to inserted", entitiesResponse, entityResponse);
    }

    private MessageQuery buildMessageQuery() {
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        return messageQuery;
    }
}
