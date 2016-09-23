package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.message.Message;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class DoubleBackslashNoEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\\\\-e10", "message-command-test-t10");
        message.setMessage("message10");
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
        Message message = new Message("message-command-test-e11", "message-command-\\\\test-t11");
        message.setMessage("message11");
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
        Message message = new Message("message-command-test-e12", "message-command-test-t12");
        message.setMessage("mess\\\\age12");
        message.setDate(Util.getCurrentDate());

        String command = buildMessageCommandFromMessage(message);
        tcpSender.send(command, MESSAGE_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted message can not be received", MessageMethod.messageExist(message));
    }


}
