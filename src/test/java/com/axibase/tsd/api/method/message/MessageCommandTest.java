package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;


import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class MessageCommandTest extends MessageMethod {
    /* #2412 */
    @Test
    public void testMaxLength() throws IOException, JSONException, InterruptedException, ParseException {
        final int MAX_LENGTH = 128 * 1024;

        String startDate = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:01.000Z";

        final Message message = new Message("e-message-max-cmd-length", "t-message-max-cmd-length");
        message.setDate(startDate);
        message.setSeverity("MAJOR");
        message.setSource("atsd");

        StringBuilder sb = new StringBuilder("message");
        sb.append(" e:").append(message.getEntity());
        sb.append(" t:").append("type").append("=").append(message.getType());
        sb.append(" t:").append("severity").append("=").append(message.getSeverity());
        sb.append(" t:").append("source").append("=").append(message.getSource());
        sb.append(" d:").append(message.getDate());
        sb.append(" m:");

        StringBuilder m = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH - sb.length(); i++) {
            m.append('m');
        }

        message.setMessage(m.toString());
        sb.append(message.getMessage());
        Assert.assertEquals(MAX_LENGTH, sb.length(), "Command length is not maximal");
        tcpSender.send(sb.toString(), 1000);

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(startDate);
        messageQuery.setEndDate(endDate);
        messageQuery.setType(message.getType());
        messageQuery.setSource(message.getSource());
        messageQuery.setSeverity(message.getSeverity());
        String storedMessage = executeQuery(messageQuery).readEntity(String.class);

        String sentMessage = jacksonMapper.writeValueAsString(Collections.singletonList(message));

        assertTrue(compareJsonString(sentMessage, storedMessage, true));
    }

    /* #2412 */
    @Test
    public void testMaxLengthOverflow() throws IOException, JSONException, InterruptedException, ParseException {
        final int MAX_LENGTH = 128 * 1024;

        String startDate = "2016-05-21T00:00:00.000Z";
        String endDate = "2016-05-21T00:00:01.000Z";

        final Message message = new Message("e-message-max-len-overflow", "t-message-max-len-overflow");
        message.setDate(startDate);
        message.setSeverity("MAJOR");
        message.setSource("atsd");

        StringBuilder sb = new StringBuilder("message");
        sb.append(" e:").append(message.getEntity());
        sb.append(" t:").append("type").append("=").append(message.getType());
        sb.append(" t:").append("severity").append("=").append(message.getSeverity());
        sb.append(" t:").append("source").append("=").append(message.getSource());
        sb.append(" d:").append(message.getDate());
        sb.append(" m:");

        StringBuilder m = new StringBuilder();
        for (int i = 0; i < MAX_LENGTH - sb.length() + 1; i++) {
            m.append('m');
        }

        message.setMessage(m.toString());
        sb.append(message.getMessage());

        if (MAX_LENGTH + 1 != sb.length()) {
            Assert.fail("Command length is not maximal");
        }
        tcpSender.send(sb.toString(), 1000);

        MessageQuery messageQuery = new MessageQuery();
        messageQuery.setEntity(message.getEntity());
        messageQuery.setStartDate(startDate);
        messageQuery.setEndDate(endDate);
        messageQuery.setType(message.getType());
        messageQuery.setSource(message.getSource());
        messageQuery.setSeverity(message.getSeverity());

        String response = executeQuery(messageQuery).readEntity(String.class);
        String expected = "{\"error\":\"com.axibase.tsd.service.DictionaryNotFoundException: " +
                "ENTITY not found for name: 'e-message-max-len-overflow'\"}";

        assertTrue(compareJsonString(expected, response, true));
    }
}
