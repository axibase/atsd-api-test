package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.model.financial.Trade;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;


@Slf4j
public class TCPTradesSender {
    private static final String LINE_SEPARATOR = "\n";
    private static final int TIMEOUT_MILLIS = 30_000;

    private TCPTradesSender() {
    }

    private static Socket createSocket(String host, int port) throws IOException {
        final Socket socket = new Socket();
        socket.setSoTimeout(TIMEOUT_MILLIS);
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_MILLIS);
        return socket;
    }

    public static void send(String command) throws IOException {
        final Config config = Config.getInstance();
        final String host = config.getServerName();
        final int port = config.getTradesTcpPort();
        try (Socket socket = createSocket(host, port)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            log.debug(" > tcp://{}:{}\n\t{}", host, port, command);
            writer.println(command);
            writer.flush();
        } catch (IOException e) {
            log.error("Unable to send command: {} \n Host: {}\n Port: {}", command, host, port);
            throw e;
        }
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
