package com.axibase.tsd.api.method.message.transport.tcp;

import com.axibase.tsd.api.method.message.MessageTest;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Date;

import static com.axibase.tsd.api.transport.tcp.TCPSenderTest.assertBadTcpResponse;
import static com.axibase.tsd.api.transport.tcp.TCPSenderTest.assertGoodTcpResponse;

public class MessageTCPTest extends MessageTest {

    private static Date TEST_DATE = Util.parseDate("2019-06-20T12:00:00.000Z");

    @Issue("6319")
    @Test
    public void testNormalWorkflow() throws Exception {
        Message message = new Message(Mocks.entity(), "logger");
        message.setMessage(Mocks.message());
        message.setDate(TEST_DATE);

        PlainCommand command = new MessageCommand(message);
        assertGoodTcpResponse("Message was not sent via TCP", TCPSender.send(command, true));
        assertMessageExisting("Message was not sent via TCP", message);
    }

    @Issue("6319")
    @Test (
            description = "Malformed parameter - entity with whitespaces"
    )
    public void testMalformedRequest() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", " "), "logger");
        message.setMessage(Mocks.message());
        message.setDate(TEST_DATE);

        PlainCommand command = new MessageCommand(message);
        assertBadTcpResponse("Request was malformed, but passed", TCPSender.send(command, true));
    }

    @Issue("6319")
    @Test
    public void testSpecialCharactersEscape() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", "\\=\\\\\"-"), "log\\=\\\\\"ger");
        message.setMessage(Mocks.message().replace("-","\\=\\\\\""));
        message.setDate(TEST_DATE);

        PlainCommand command= new MessageCommand(message);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertMessageExisting(message);
    }
}
