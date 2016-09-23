package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class BackslashNoEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\\-e7", "message-command-test-t7");
        message.setMessage("message7");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

    /**
     * #2854
     */
    @Test
    public void testType() throws Exception {
        Message message = new Message("message-command-test-e8", "message-command-\\test-t8");
        message.setMessage("message8");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

    /**
     * #2854
     */
    @Test
    public void testText() throws Exception {
        Message message = new Message("message-command-test-e9", "message-command-test-t9");
        message.setMessage("mess\\age9");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

}
