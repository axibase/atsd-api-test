package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import org.json.JSONException;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.util.Collections;

public class MessageCommandTest extends MessageMethod {
    /* #2412 */
    @Test
    public void testMaxLength() throws IOException, ParseException, JSONException, InterruptedException {
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

        Assert.assertEquals("Command length is not maximal", MAX_LENGTH, sb.length());
        tcpSender.send(sb.toString(), 1000);

        MessageQuery messageQuery = new MessageQuery(message.getEntity(), startDate, endDate);
        messageQuery.setType(message.getType());
        messageQuery.setSource(message.getSource());
        messageQuery.setSeverity(message.getSeverity());
        String storedMessage = executeQuery(messageQuery);

        String sentMessage = jacksonMapper.writeValueAsString(Collections.singletonList(message));

        JSONAssert.assertEquals(sentMessage, storedMessage, JSONCompareMode.NON_EXTENSIBLE);
    }

    /* #2412 */
    @Test
    public void testMaxLengthOverflow() throws IOException, ParseException, JSONException, InterruptedException {
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

        MessageQuery messageQuery = new MessageQuery(message.getEntity(), startDate, endDate);
        messageQuery.setType(message.getType());
        messageQuery.setSource(message.getSource());
        messageQuery.setSeverity(message.getSeverity());
        String storedMessage = executeQuery(messageQuery);

        JSONAssert.assertEquals("{\"error\":\"com.axibase.tsd.service.DictionaryNotFoundException: " +
                "ENTITY not found for name: 'e-message-max-len-overflow'\"}",
                storedMessage, JSONCompareMode.NON_EXTENSIBLE);
    }
}
