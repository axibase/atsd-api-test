package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.model.command.PlainCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;


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
        try (Socket socket = createSocket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream());
             BufferedReader responseStream = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
             )) {
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
}
