package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import java.util.List;

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
        List<Message> storedMessageList = executeQuery(messageQuery).readEntity(new GenericType<List<Message>>(){});
        Message storedMessage = storedMessageList.get(0);

        Assert.assertEquals("nurswgvml022", storedMessage.getEntity());
        Assert.assertEquals("NURSWGVML007 ssh: error: connect_to localhost port 8881: failed.", storedMessage.getMessage());
        Assert.assertEquals("application", storedMessage.getType());
    }
}