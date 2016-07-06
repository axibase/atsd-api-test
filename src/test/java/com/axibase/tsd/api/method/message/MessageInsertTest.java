package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static com.axibase.tsd.api.Util.*;

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

        MessageQuery messageQuery = new MessageQuery("nurswgvml022", date, endDate);
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>() {
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
        MessageQuery messageQuery = new MessageQuery(message.getEntity(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

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
        MessageQuery messageQuery = new MessageQuery(message.getEntity(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

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
}