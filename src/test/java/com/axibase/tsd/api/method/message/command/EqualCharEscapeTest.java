package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class EqualCharEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test=-e4", "message-command-test-t4");
        message.setMessage("message4");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    /**
     * #2854
     */
    @Test
    public void testType() throws Exception {
        Message message = new Message("message-command-test-e5", "message-command-=test-t5");
        message.setMessage("message5");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    /**
     * #2854
     */
    @Test
    public void testEscapeText() throws Exception {
        Message message = new Message("message-command-test-e6", "message-command-test-t6");
        message.setMessage("mess=age6");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

}
