package com.axibase.tsd.api.method.message.command;

import com.axibase.tsd.api.method.message.MessageMethod;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.method.message.MessageTest.assertMessageExisting;
import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class BackslashNoEscapeTest extends MessageMethod {

    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public BackslashNoEscapeTest(Transport transport) {
        this.transport = transport;
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testEntity() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", "\\\\"), "message-command-test-t7");
        message.setMessage("message7");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Test
    public void testType() throws Exception {
        Message message = new Message(Mocks.entity(), "message-command-\\test-t8");
        message.setMessage("message8");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Test
    public void testText() throws Exception {
        Message message = new Message(Mocks.entity(), "message-command-test-t9");
        message.setMessage("mess\\age9");
        message.setDate(getCurrentDate());
        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

}
