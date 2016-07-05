package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.IntervalUnit;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;

public class MessageQueryTest extends MessageMethod {
    @Test
    public void testISOTimezoneZ() throws Exception {
        long startMillis = 1463788800000L;

        Message message = new Message("message-query-test-isoz");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "UTC"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-query-test-isoz");
        messageQuery.setStartDate(Util.ISOFormat(startMillis, false, "UTC"));
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals(message.getEntity(), storedMessage.getEntity());
        Assert.assertEquals(message.getMessage(), storedMessage.getMessage());
        Assert.assertEquals(Util.ISOFormat(startMillis, true, "UTC"), storedMessage.getDate());
    }

    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        long startMillis = 1463788800000L;

        Message message = new Message("message-query-test-iso+hm");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "UTC"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-query-test-iso+hm");
        messageQuery.setStartDate(Util.ISOFormat(startMillis, false, "GMT+01:23"));
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals(message.getEntity(), storedMessage.getEntity());
        Assert.assertEquals(message.getMessage(), storedMessage.getMessage());
        Assert.assertEquals(Util.ISOFormat(startMillis, true, "UTC"), storedMessage.getDate());
    }

    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        long startMillis = 1463788800000L;

        Message message = new Message("message-query-test-iso-hm");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "UTC"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-query-test-iso-hm");
        messageQuery.setStartDate(Util.ISOFormat(startMillis, false, "GMT-01:23"));
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals(message.getEntity(), storedMessage.getEntity());
        Assert.assertEquals(message.getMessage(), storedMessage.getMessage());
        Assert.assertEquals(Util.ISOFormat(startMillis, true, "UTC"), storedMessage.getDate());
    }
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "message-query-test-localtime";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2016-07-21T00:00:00Z");

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate("2016-07-21 00:00:00");
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        Response response = executeQuery(messageQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-21 00:00:00\"}", response.readEntity(String.class), true);

    }
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "message-query-test-xx-timezone";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2016-07-21T00:00:00Z");

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate("2016-07-20T22:50:00-0110");
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        Response response = executeQuery(messageQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-20T22:50:00-0110\"}", response.readEntity(String.class), true);
    }
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "message-query-test-millis";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2016-07-21T00:00:00Z");

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate("1469059200000");
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        Response response = executeQuery(messageQuery);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1469059200000\"}", response.readEntity(String.class), true);
    }

}
