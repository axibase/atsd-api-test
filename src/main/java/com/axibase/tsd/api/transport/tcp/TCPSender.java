package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.model.command.PlainCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;


@Slf4j
public class TCPSender extends TCPSenderBase {
    private static final String DEBUG_PREFIX = "debug ";

    private TCPSender() {
    }

    private static String send(String command, boolean isDebugMode) throws IOException {
        final String request = isDebugMode ? DEBUG_PREFIX.concat(command) : command;
        final Config config = Config.getInstance();
        final String host = config.getServerName();
        final int port = config.getTcpPort();
        try (Socket socket = createSocket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream());
             BufferedReader responseStream = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
             )) {
            log.debug(" > tcp://{}:{}\n\t{}", host, port, request);
            writer.println(request);
            writer.flush();
            String response = isDebugMode ? responseStream.readLine() : null;
            log.debug(" < tcp://{}:{} \n\t {}", host, port, response);
            return response;
        } catch (IOException e) {
            log.error("Unable to send command: {} \n Host: {}\n Port: {}", command, host, port);
            throw e;
        }
    }

    public static void send(String request) throws IOException {
        send(request, false);
    }

    public static String send(PlainCommand command, boolean isDebugMode) throws IOException {
        return send(Collections.singleton(command), isDebugMode);
    }

    public static String send(Collection<? extends PlainCommand> commands, boolean isDebugMode) throws IOException {
        String request = buildRequest(commands);
        return send(request, isDebugMode);
    }

    private static String buildRequest(Collection<? extends PlainCommand> commands) {
        return StringUtils.join(commands, LINE_SEPARATOR);
    }

    public static void send(Collection<? extends PlainCommand> commands) throws IOException {
        send(commands, false);
    }

}
