package com.axibase.tsd.api.transport;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.transport.tcp.TCPSender;

import java.io.IOException;

import static com.axibase.tsd.api.transport.tcp.TCPSenderTest.assertBadTcpResponse;
import static com.axibase.tsd.api.transport.tcp.TCPSenderTest.assertGoodTcpResponse;
import static org.testng.AssertJUnit.assertEquals;

public enum Transport {
    HTTP {
        @Override
        public void sendAndCompareToExpected(PlainCommand command, CommandSendingResult expected, String message) {
            assertEquals(message, expected, CommandMethod.send(command));
        }
    },
    TCP {
        @Override
        public void sendAndCompareToExpected(PlainCommand command, CommandSendingResult expected, String message) throws IOException {
            final String response = TCPSender.send(command, true);
            if (expected.getFail() > 0) {
                assertBadTcpResponse(message, response);
            } else {
                assertGoodTcpResponse(message, response);
            }
        }
    };

    public abstract void sendAndCompareToExpected(PlainCommand command, CommandSendingResult expected, String message) throws IOException;
}