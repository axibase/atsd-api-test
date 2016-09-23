package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class EqualCharEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test=-e4", "message-command-test-t4");
        message.setMessage("message4");
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
        Message message = new Message("message-command-test-e5", "message-command-=test-t5");
        message.setMessage("message5");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

    /**
     * #2854
     */
    @Test
    public void testEscapeText() throws Exception {
        Message message = new Message("message-command-test-e6", "message-command-test-t6");
        message.setMessage("mess=age6");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }

}
