package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class DoubleBackslashNoEscapeTest extends MessageMethod {

    @Issue("2854")
    @Test
    public void testEntity() throws Exception {
        Message message = new Message("message-command-test\\\\-e10", "message-command-test-t10");
        message.setMessage("message10");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Test
    public void testType() throws Exception {
        Message message = new Message("message-command-test-e11", "message-command-\\\\test-t11");
        message.setMessage("message11");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Test
    public void testText() throws Exception {
        Message message = new Message("message-command-test-e12", "message-command-test-t12");
        message.setMessage("mess\\\\age12");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        CommandMethod.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }


}
