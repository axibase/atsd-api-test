package com.axibase.tsd.api.method.extended;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.util.List;
import java.util.function.Function;


public class CommandMethod extends BaseMethod {
    private static final String METHOD_PATH = "command";

    public static Response executeCommandRequest(Function<WebTarget, Response> commandFunction) {
        return executeApiRequest(webTarget -> commandFunction.apply(webTarget.path(METHOD_PATH)));
    }

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
        Response response = executeCommandRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(payload, MediaType.TEXT_PLAIN)));
        response.bufferEntity();
        return response;
    }

    public static Response sendGzipCompressedBytes(byte[] gzipCompressedBytes) {
        Response response = executeCommandRequest(webTarget -> webTarget
                .request()
                .post(Entity.entity(gzipCompressedBytes, new Variant(MediaType.TEXT_PLAIN_TYPE, "en", "gzip"))));

        response.bufferEntity();
        return response;
    }
}
