package com.axibase.tsd.api.transport;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.DataProvider;

import java.io.IOException;

public enum Transport {
    HTTP {
        @Override
        public boolean send(final PlainCommand command) throws IOException {
            final CommandSendingResult result = CommandMethod.send(command);
            return result.getFail() == 0;
        }
    },
    TCP {
        @Override
        public boolean send(final PlainCommand command) throws IOException {
            final String response = TCPSender.send(command, true);
            return "ok".equals(response);
        }

        @Override
        public boolean sendNoDebug(PlainCommand command) throws IOException {
            final String response = TCPSender.send(command, false);
            return "ok".equals(response);
        }
    };

    @DataProvider
    private static Object[][] transport() {
        return TestUtil.convertTo2DimArray(Transport.values());
    }

    public abstract boolean send(PlainCommand command) throws IOException;

    public boolean sendNoDebug(PlainCommand command) throws IOException {
        return send(command);
    }
}