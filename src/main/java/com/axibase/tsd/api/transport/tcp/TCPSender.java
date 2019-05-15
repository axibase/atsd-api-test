package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.model.command.PlainCommand;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class TCPSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TCPSender.class);
    private static final String DEBUG_PREFIX = "debug ";
    private static final String LINE_SEPARATOR = "\n";
    private static final Config config = Config.getInstance();

    private TCPSender() {
    }

    private static String send(String command, Boolean isDebugMode) throws IOException {
        final String request = isDebugMode ? DEBUG_PREFIX.concat(command) : command;
        final String host = config.getServerName();
        final int port = config.getTcpPort();
        try (Socket socket = new Socket(host, port);
             DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
             BufferedReader responseStream = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
             )) {

            IOUtils.write(request, requestStream);
            String response = isDebugMode ? responseStream.readLine() : null;
            LOGGER.debug(" > tcp://{}:{} \n\t{}\n< {}", host, port, request, response);
            return response;
        } catch (IOException e) {
            LOGGER.error("Unable to send command: {} \n Host: {}\n Port: {}", command, host, port);
            throw e;
        }
    }

    public static void send(String request) throws IOException {
        send(request, Boolean.FALSE);
    }

    public static String send(PlainCommand command, Boolean isDebugMode) throws IOException {
        return send(Collections.singleton(command), isDebugMode);
    }

    public static void send(PlainCommand... commands) throws IOException {
        send(Arrays.asList(commands), Boolean.FALSE);
    }

    public static String send(Collection<? extends PlainCommand> commands, Boolean isDebugMode) throws IOException {
        String request = buildRequest(commands);
        return send(request, isDebugMode);
    }

    private static String buildRequest(Collection<? extends PlainCommand> commands) {
        StringBuilder builder = new StringBuilder();
        for (PlainCommand command : commands) {
            builder.append(command).append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    public static void send(Collection<? extends PlainCommand> commands) throws IOException {
        send(commands, Boolean.FALSE);
    }

    public static String sendChecked(AbstractCheck check, String request) throws IOException {
        String response = send(request, Boolean.FALSE);
        Checker.check(check);
        return response;
    }

    public static String sendChecked(AbstractCheck check, Collection<? extends PlainCommand> commands) throws IOException {
        String request = buildRequest(commands);
        return sendChecked(check, request);
    }
}
