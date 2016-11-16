package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class DQuoteCharEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\"\"-e1", "message-command-test-t1");
        message.setMessage("message1");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        message.setEntity(message.getEntity().replace("\"\"", "\""));
        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

    /**
     * #2854
     */
    @Test
    public void testType() throws Exception {
        Message message = new Message("message-command-test-e2", "message-command-\"\"test-t2");
        message.setMessage("message2");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        message.setType(message.getType().replace("\"\"", "\""));
        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

    /**
     * #2854
     */
    @Test
    public void testText() throws Exception {
        Message message = new Message("message-command-test-e3", "message-command-test-t3");
        message.setMessage("mess\"\"age3");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        message.setMessage(message.getMessage().replace("\"\"", "\""));
        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

}
