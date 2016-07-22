package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.testng.Assert;


import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;

public class MessageQueryTest extends MessageMethod {
    private static final Message message;

    static {
        message = new Message("message-query-test-timezone");
        message.setMessage("hello");
        message.setDate("2016-05-21T00:00:00.000Z");
    }
    @BeforeMethod
    public void prepare() throws Exception {
        Assert.assertTrue(insertMessage(message, 1000), "Fail to insert message");
    }


    /* #2850 */
    @Test
    public void testISOTimezoneZ() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2016-05-21T00:00:00Z");

        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2016-05-21T01:23:00+01:23");

        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2016-05-20T22:37:00-01:23");

        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", message.getDate(), storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2016-07-21 00:00:00");

        Response response = executeQuery(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-21 00:00:00\"}", response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("2016-07-20T22:50:00-0110");

        Response response = executeQuery(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-20T22:50:00-0110\"}", response.readEntity(String.class), true);
    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        MessageQuery messageQuery = buildMessageQuery();
        messageQuery.setStartDate("1469059200000");

        Response response = executeQuery(messageQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1469059200000\"}", response.readEntity(String.class), true);
    }

    private MessageQuery buildMessageQuery() {
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        return messageQuery;
    }
}
