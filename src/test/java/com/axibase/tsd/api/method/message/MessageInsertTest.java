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

import static com.axibase.tsd.api.Util.*;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.junit.Assert.assertEquals;

public class MessageInsertTest extends MessageMethod {

    /* #2903 */
    @Test
    public void testTrimmedMessages() throws Exception {
        String entityName = "          nurswgvml022    \n    ";
        String messageText = "          NURSWGVML007 ssh: error: connect_to localhost port 8881: failed.     \n     ";
        String type = "      application    \n      ";
        String date = "2016-05-21T00:00:00Z";
        String endDate = "2016-05-21T00:00:01Z";

        Message message = new Message(entityName, type);
        message.setMessage(messageText);
        message.setDate(date);

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("nurswgvml022");
        messageQuery.setStartDate(date);
        messageQuery.setEndDate(endDate);
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){
        });
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals("nurswgvml022", storedMessage.getEntity());
        Assert.assertEquals("NURSWGVML007 ssh: error: connect_to localhost port 8881: failed.", storedMessage.getMessage());
        Assert.assertEquals("application", storedMessage.getType());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinSaved() throws Exception {
        Message message = new Message("e-time-range-msg-1");
        message.setMessage("msg-time-range-msg-1");
        message.setDate(MIN_STORABLE_DATE);

        Boolean success = insertMessage(message);
        // wait for message availability
        Thread.sleep(1000L);

        if (!success)
            Assert.fail("Failed to insert message");
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(MAX_QUERYABLE_DATE);

        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>() {});

        Message msgResponse = storedMessageList.get(0);
        Assert.assertEquals("Incorrect stored date", message.getDate(), msgResponse.getDate());
        Assert.assertEquals("Incorrect stored message", message.getMessage(), msgResponse.getMessage());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeSaved() throws Exception {
        Message message = new Message("e-time-range-msg-3");
        message.setMessage("msg-time-range-msg-3");
        message.setDate(MAX_STORABLE_DATE);

        Boolean success = insertMessage(message);
        // wait for message availability
        Thread.sleep(1000L);

        if (!success)
            Assert.fail("Failed to insert message");
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(MAX_QUERYABLE_DATE);

        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>() {});

        Message msgResponse = storedMessageList.get(0);
        Assert.assertEquals("Max storable date failed to save", message.getDate(), msgResponse.getDate());
        Assert.assertEquals("Incorrect stored message", message.getMessage(), msgResponse.getMessage());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeOverflow() throws Exception {
        Message message = new Message("e-time-range-msg-4");
        message.setMessage("msg-time-range-msg-4");
        message.setDate(addOneMS(MAX_STORABLE_DATE));

        Boolean success = insertMessage(message);
        // wait for message availability
        Thread.sleep(1000L);

        if (success)
            Assert.fail("Managed to insert message with date out of range");
    }

    @Test
    public void testISOTimezoneZ() throws Exception {
        long startMillis = 1463788800000L;

        Message message = new Message("message-insert-test-isoz");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "UTC"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-insert-test-isoz");
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

        Message message = new Message("message-insert-test-iso+hm");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "GMT+01:23"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-insert-test-iso+hm");
        messageQuery.setStartDate(Util.ISOFormat(startMillis, false, "UTC"));
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

        Message message = new Message("message-insert-test-iso-hm");
        message.setMessage("hello");
        message.setDate(Util.ISOFormat(startMillis, false, "GMT-01:23"));

        Assert.assertTrue("Fail to insert message", insertMessage(message, 1000));

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("message-insert-test-iso-hm");
        messageQuery.setStartDate(Util.ISOFormat(startMillis, false, "UTC"));
        messageQuery.setInterval(new Interval(1, IntervalUnit.SECOND));
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals(message.getEntity(), storedMessage.getEntity());
        Assert.assertEquals(message.getMessage(), storedMessage.getMessage());
        Assert.assertEquals(Util.ISOFormat(startMillis, true, "UTC"), storedMessage.getDate());
    }

    @Test
    public void testLocalTimeUnsupported() throws Exception {
        String entityName = "message-insert-test-localtime";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2016-07-21 00:00:00");

        Response response = insertMessageReturnResponse(message);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 2016-07-21 00:00:00\"}", response.readEntity(String.class), true);

    }
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        String entityName = "message-insert-test-xxtimezone";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2016-07-20T22:50:00-0110");

        Response response = insertMessageReturnResponse(message);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 2016-07-20T22:50:00-0110\"}", response.readEntity(String.class), true);
    }
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        String entityName = "message-insert-test-milliseconds";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("1469059200000");

        Response response = insertMessageReturnResponse(message);

        assertEquals(BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 1469059200000\"}", response.readEntity(String.class), true);
    }

}