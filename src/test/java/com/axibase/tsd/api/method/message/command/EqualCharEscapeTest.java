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

public class EqualCharEscapeTest extends MessageMethod {

    private final Transport transport;

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public EqualCharEscapeTest(Transport transport) {
        this.transport = transport;
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testEntity() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", "=-"), "message-command-test-t4");
        message.setMessage("message4");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testType() throws Exception {
        Message message = new Message(Mocks.entity(), "message-command-=test-t5");
        message.setMessage("message5");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testEscapeText() throws Exception {
        Message message = new Message(Mocks.entity(), "message-command-test-t6");
        message.setMessage("mess=age6");
        message.setDate(getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        transport.send(command);
        assertMessageExisting("Inserted message can not be received", message);
    }

}
