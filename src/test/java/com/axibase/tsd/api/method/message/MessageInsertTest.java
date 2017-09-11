package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;

public class MessageInsertTest extends MessageMethod {
    private Calendar calendar = Calendar.getInstance();


    /* #2903 */
    @Test
    public void testTrimmedMessages() throws Exception {
        String entityName = "          nurswgvml022    \n    ";
        String messageText = "          NURSWGVML007 ssh: error: connect_to localhost port 8881: failed.     \n     ";
        String type = "      application    \n      ";
        String date = "2017-05-21T00:00:00Z";
        String endDate = "2017-05-21T00:00:01Z";

        Message message = new Message(entityName, type);
        message.setMessage(messageText);
        message.setDate(date);

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity("nurswgvml022");
        messageQuery.setStartDate(date);
        messageQuery.setEndDate(endDate);

        insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("nurswgvml022", storedMessage.getEntity());
        assertEquals("NURSWGVML007 ssh: error: connect_to localhost port 8881: failed.", storedMessage.getMessage());
        assertEquals("application", storedMessage.getType());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMinSaved() throws Exception {
        Message message = new Message("e-time-range-msg-1");
        message.setMessage("msg-time-range-msg-1");
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        message.setDate(calendar.getTime());

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(MAX_QUERYABLE_DATE);

        insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });

        Message msgResponse = storedMessageList.get(0);
        assertEquals("Incorrect stored date", message.getDate(), msgResponse.getDate());
        assertEquals("Incorrect stored message", message.getMessage(), msgResponse.getMessage());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeSaved() throws Exception {
        Message message = new Message("e-time-range-msg-3");
        message.setMessage("msg-time-range-msg-3");
        message.setDate(MAX_STORABLE_DATE);

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(MIN_QUERYABLE_DATE);
        messageQuery.setEndDate(MAX_QUERYABLE_DATE);

        insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });

        Message msgResponse = storedMessageList.get(0);
        assertEquals("Max storable date failed to save", message.getDate(), msgResponse.getDate());
        assertEquals("Incorrect stored message", message.getMessage(), msgResponse.getMessage());
    }

    /* #2957 */
    @Test
    public void testTimeRangeMaxTimeOverflow() throws Exception {
        Message message = new Message("e-time-range-msg-4");
        message.setMessage("msg-time-range-msg-4");
        message.setDate(addOneMS(MAX_STORABLE_DATE));

        Boolean success = insertMessage(message);

        if (success) {
            fail("Managed to insert message with date out of range");
        }
    }

    /* #2850 */
    @Test
    public void testISOTimezoneZ() throws Exception {
        String entityName = "message-insert-test-isoz";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2017-05-21T00:00:00Z");

        String date = "2017-05-21T00:00:00.000Z";
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate(date);
        messageQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        MessageMethod.insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        GenericType<List<Message>> generic = new GenericType<List<Message>>() {
        };
        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(generic);
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", date, storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezonePlusHourMinute() throws Exception {
        String entityName = "message-insert-test-iso+hm";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2017-05-21T01:23:00+01:23");


        String date = "2017-05-21T00:00:00.000Z";
        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate(date);
        messageQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        MessageMethod.insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", date, storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testISOTimezoneMinusHourMinute() throws Exception {
        String entityName = "message-insert-test-iso-hm";
        Message message = new Message(entityName);
        message.setMessage("hello");
        message.setDate("2017-05-20T22:37:00-01:23");


        String date = "2017-05-21T00:00:00.000Z";
        final MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(entityName);
        messageQuery.setStartDate(date);
        messageQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));

        insertMessageCheck(message, new MessageQuerySizeCheck(messageQuery, 1));

        List<Message> storedMessageList = queryMessageResponse(messageQuery).readEntity(new GenericType<List<Message>>() {
        });
        Message storedMessage = storedMessageList.get(0);

        assertEquals("Incorrect message entity", message.getEntity(), storedMessage.getEntity());
        assertEquals("Incorrect message text", message.getMessage(), storedMessage.getMessage());
        assertEquals("Incorrect message date", date, storedMessage.getDate());
    }

    /* #2850 */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        Message message = new Message("message-insert-test-localtime");
        message.setMessage("hello");
        message.setDate("2017-07-21 00:00:00");

        Response response = insertMessageReturnResponse(message);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 2017-07-21 00:00:00\"}",
                response.readEntity(String.class), true);

    }

    /* #2850 */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        Message message = new Message("message-insert-test-xxtimezone");
        message.setMessage("hello");
        message.setDate("2017-07-20T22:50:00-0110");

        Response response = insertMessageReturnResponse(message);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 2017-07-20T22:50:00-0110\"}",
                response.readEntity(String.class), true);
    }

    /* #2850 */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        Message message = new Message("message-insert-test-milliseconds");
        message.setMessage("hello");
        message.setDate("1469059200000");

        Response response = insertMessageReturnResponse(message);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Failed to parse date 1469059200000\"}",
                response.readEntity(String.class), true);
    }

    private static class MessageQuerySizeCheck extends AbstractCheck {
        private MessageQuery query;
        private Integer size;

        private MessageQuerySizeCheck(MessageQuery query, Integer size) {
            this.query = query;
            this.size = size;
        }

        @Override
        public boolean isChecked() {
            Response response = queryMessageResponse(query);
            if (response.getStatus() != OK.getStatusCode()) {
                return false;
            }
            List<Message> storedMessageList = response.readEntity(new GenericType<List<Message>>() {
            });
            return storedMessageList.size() == size;
        }
    }
}
