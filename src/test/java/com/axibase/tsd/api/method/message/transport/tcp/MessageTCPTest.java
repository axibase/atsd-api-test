package com.axibase.tsd.api.method.message.transport.tcp;

import com.axibase.tsd.api.method.message.MessageTest;
import com.axibase.tsd.api.model.command.MessageCommand;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.transport.tcp.TCPSender.assertBadTcpResponse;
import static com.axibase.tsd.api.transport.tcp.TCPSender.assertGoodTcpResponse;

public class MessageTCPTest extends MessageTest {

    @Issue("6319")
    @Test
    public void testNormalWorkflow() throws Exception {
        Message message = new Message(Mocks.entity(), "logger");
        message.setMessage(Mocks.message());
        message.setDate(TestUtil.getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        assertGoodTcpResponse("Message was not sent via TCP", TCPSender.send(command, true));
        assertMessageExisting("Message was not sent via TCP", message);
    }

    @Issue("6319")
    @Test
    public void testMalformedRequest() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", " "), "logger");
        message.setMessage(Mocks.message());
        message.setDate(TestUtil.getCurrentDate());

        PlainCommand command = new MessageCommand(message);
        assertBadTcpResponse("Request was malformed, but passed", TCPSender.send(command, true));
    }

    @Issue("6319")
    @Test
    public void testSpecialCharactersEscape() throws Exception {
        Message message = new Message(Mocks.entity().replaceAll("-", "\\=\\\\\"-"), "log\\=\\\\\"ger");
        message.setMessage(Mocks.message().replace("-","\\=\\\\\""));
        message.setDate(TestUtil.getCurrentDate());

        PlainCommand command= new MessageCommand(message);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertMessageExisting(message);
    }
}
