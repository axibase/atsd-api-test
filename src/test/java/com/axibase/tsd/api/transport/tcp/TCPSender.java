package com.axibase.tsd.api.transport.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author Dmitry Korchagin.
 */
public class TCPSender {
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
        return response.equals("ok");
    }

    public void send() throws IOException {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command.append('\n').toString());
        requestStream.close();
    }

    public void send(String command, long sleepDuration) throws IOException, InterruptedException {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command + '\n');
        requestStream.close();
        Thread.sleep(sleepDuration);
    }

    public void send(String command) throws IOException, InterruptedException {
        send(command, 0);
    }
}
