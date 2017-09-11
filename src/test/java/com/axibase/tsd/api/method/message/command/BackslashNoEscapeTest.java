package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class BackslashNoEscapeTest extends MessageMethod {

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\\-e7", "message-command-test-t7");
        message.setMessage("message7");
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
        Message message = new Message("message-command-test-e8", "message-command-\\test-t8");
        message.setMessage("message8");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    /**
     * #2854
     */
    @Test
    public void testText() throws Exception {
        Message message = new Message("message-command-test-e9", "message-command-test-t9");
        message.setMessage("mess\\age9");
        message.setDate(getCurrentDate());
        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

}
