package com.axibase.tsd.api.transport.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author Dmitry Korchagin.
 */
public class TCPSender {

    private static final Logger logger = LoggerFactory.getLogger(TCPSender.class);

    private String url;
    private Integer port;
    private StringBuilder command;

    public TCPSender(String url, Integer port) {
        this.url = url;
        this.port = port;
        command = new StringBuilder("debug ");
    }

    public void setCommand(String command) {
        this.command = new StringBuilder(command);
    }

    public void appendCommand(String commandPart) {
        command.append(commandPart);
    }

    public boolean sendDebugMode() throws IOException {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestStream.writeBytes(command.insert(0, "debug ").append('\n').toString());
        String response = responseStream.readLine();
        if (response == null ) return false;
        return response.equals("ok");
    }

    public boolean sendDebugMode(long sleepDuration) throws IOException, InterruptedException {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestStream.writeBytes(command.insert(0, "debug ").append('\n').toString());
        String response = responseStream.readLine();
        Thread.sleep(sleepDuration);
        return response.equals("ok");
    }

    public void send() throws IOException {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command.append('\n').toString());
        requestStream.close();
    }

    public void send(String command, long sleepDuration) throws IOException, InterruptedException {
        logger.debug(" > =====TCP=====\n > Sending via tcp://{}:{}\n > {}", url, port, command);
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command + '\n');
        requestStream.close();
        Thread.sleep(sleepDuration);
    }

    public void send(String command) throws IOException, InterruptedException {
        send(command, 0);
    }

    public void sendCheck (String command) throws IOException, InterruptedException {
        setCommand(command);
        boolean successed = sendDebugMode();
        if (!successed)
            throw new IOException("Fail to check inserted command");
    }

    public void sendCheck (String command, long sleepDuration) throws IOException, InterruptedException {
        setCommand(command);
        boolean successed = sendDebugMode(sleepDuration);
        if (!successed)
            throw new IOException("Fail to check inserted command");
    }
}
