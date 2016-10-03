package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.model.command.PlainCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import static com.axibase.tsd.api.method.BaseMethod.DEFAULT_EXPECTED_PROCESSING_TIME;

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

    public boolean sendDebugMode() throws Exception {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestStream.writeBytes(command.insert(0, "debug ").append('\n').toString());
        String response = responseStream.readLine();
        if (response == null) return false;
        return response.equals("ok");
    }

    public boolean sendDebugMode(long sleepDuration) throws Exception {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        BufferedReader responseStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        requestStream.writeBytes(command.insert(0, "debug ").append('\n').toString());
        String response = responseStream.readLine();
        Thread.sleep(sleepDuration);
        return response.equals("ok");
    }

    public void send() throws Exception {
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command.append('\n').toString());
        requestStream.close();
    }

    public void send(String command, long sleepDuration) throws Exception {
        logger.debug(" > =====TCP=====\n > Sending via tcp://{}:{}\n > {}", url, port, command);
        Socket socket = new Socket(url, port);
        DataOutputStream requestStream = new DataOutputStream(socket.getOutputStream());
        requestStream.writeBytes(command + '\n');
        requestStream.close();
        Thread.sleep(sleepDuration);
    }

    public void send(String command) throws Exception {
        send(command, 0);
    }

    public void sendCheck(String command) throws Exception {
        setCommand(command);
        boolean successed = sendDebugMode();
        if (!successed)
            throw new Exception("Fail to check inserted command");
    }

    public void send(PlainCommand command) throws Exception {
        send(command.compose(), DEFAULT_EXPECTED_PROCESSING_TIME);
    }

    public void sendCheck(String command, long sleepDuration) throws Exception {
        setCommand(command);
        boolean successed = sendDebugMode(sleepDuration);
        if (!successed)
            throw new Exception("Fail to check inserted command");
    }
}
