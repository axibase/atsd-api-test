package com.axibase.tsd.api.method.extended;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.extended.CommandSendingResult;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
}
