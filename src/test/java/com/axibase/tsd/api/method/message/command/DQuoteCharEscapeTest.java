package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;

public class DQuoteCharEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\"-e1", "message-command-test-t1");
        message.setMessage("message1");
        message.setDate(Util.getCurrentDate());
        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    /**
     * #2854
     */
    @Test
    public void testType() throws Exception {
        Message message = new Message("message-command-test-e2", "message-command-\"test-t2");
        message.setMessage("message2");
        message.setDate(Util.getCurrentDate());
        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    /**
     * #2854
     */
    @Test
    public void testText() throws Exception {
        Message message = new Message("message-command-test-e3", "message-command-test-t3");
        message.setMessage("mess\"age3");
        message.setDate(Util.getCurrentDate());
        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

}
