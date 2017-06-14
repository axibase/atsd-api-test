package com.axibase.tsd.api.method.extended;

import com.axibase.tsd.api.Config;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class CommandMethod extends BaseMethod {
    private static final String METHOD_PATH = "command";
    private static final WebTarget METHOD_RESOURCE = httpApiResource.path(METHOD_PATH);

    public static CommandSendingResult send(String payload) {
        return sendResponse(payload).readEntity(CommandSendingResult.class);
    }

    public static CommandSendingResult send(PlainCommand command) {
        return send(command.compose());
    }

    public static CommandSendingResult send(List<PlainCommand> commandList) {
        return send(buildPayload(commandList));
    }

    private static String buildPayload(List<PlainCommand> commandList) {
        StringBuilder queryBuilder = new StringBuilder();
        for (PlainCommand command : commandList) {
            queryBuilder
                    .append(String.format("%s%n", command.compose()));
        }
        return queryBuilder.toString();
    }

    private static Response sendResponse(String payload) {
        Response response = METHOD_RESOURCE.request().post(Entity.entity(payload, MediaType.TEXT_PLAIN));
        response.bufferEntity();
        return response;
    }

    public static HttpResponse sendGzipFileCommand(String filePath) {
        String targetURL = METHOD_RESOURCE.getUri().toString();

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = null;
        try {
            credentials = new UsernamePasswordCredentials(Config.getInstance().getLogin(),
                    Config.getInstance().getPassword());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        provider.setCredentials(AuthScope.ANY, credentials);

        HttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        try {
            HttpPost httppost = new HttpPost(targetURL);
            httppost.setHeader("Content-Encoding", "gzip");
            httppost.setHeader("Content-Type", "text/plain;charset=UTF-8");

            FileEntity entity = new FileEntity(new File(filePath), "binary/octet-stream");
            httppost.setEntity(entity);

            return httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return null;
    }
}
