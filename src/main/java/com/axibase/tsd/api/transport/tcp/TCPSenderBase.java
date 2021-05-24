package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Config;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

public abstract class TCPSenderBase {
    protected static final String LINE_SEPARATOR = "\n";
    protected static final int TIMEOUT_MILLIS = 30_000;

    protected static Socket createSocket(String host, int port) throws IOException {
        final Socket socket = new Socket();
        socket.setSoTimeout(TIMEOUT_MILLIS);
        socket.connect(new InetSocketAddress(host, port), TIMEOUT_MILLIS);
        return socket;
    }

    protected static void sendHelper(String command, int port, Logger log) throws IOException {
        sendHelper(command, port, log, false);
    }

    protected static void sendHelper(String command, int port, Logger log, boolean gzip) throws IOException {
        final Config config = Config.getInstance();
        final String host = config.getServerName();
        try (Socket socket = createSocket(host, port)) {
            OutputStream outputStream = socket.getOutputStream();
            if (gzip) {
                outputStream = new GZIPOutputStream(outputStream);
            }
            PrintWriter writer = new PrintWriter(outputStream);
            log.debug(" > tcp://{}:{}\n\t{}", host, port, command);
            writer.println(command);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            log.error("Unable to send command: {} \n Host: {}\n Port: {}", command, host, port);
            throw e;
        }
    }

}