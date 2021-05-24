package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.model.financial.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


@Slf4j
public class TCPTradesSender extends TCPSenderBase {

    private TCPTradesSender() {
    }

    public static void send(String command) throws IOException {
        final Config config = Config.getInstance();
        final int port = config.getTradesTcpPort();
        sendHelper(command, port, log);
    }

    public static void send(String... commands) throws IOException {
        send(StringUtils.join(commands, LINE_SEPARATOR));
    }

    public static void send(Trade... trades) throws IOException {
        send(Arrays.asList(trades));
    }

    public static void send(Collection<Trade> trades) throws IOException {
        send(trades.stream().map(Trade::toCsvLine).toArray(String[]::new));
    }
}
