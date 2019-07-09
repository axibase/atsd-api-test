package com.axibase.tsd.api.model.command;


import java.util.List;


public class ListCommand extends AbstractCommand {

    public ListCommand(List<PlainCommand> commands) {
        super(buildPayload(commands));
    }

    @Override
    public String compose() {
        return commandBuilder().toString();
    }

    private static String buildPayload(List<PlainCommand> commandList) {
        StringBuilder queryBuilder = new StringBuilder();
        for (PlainCommand command : commandList) {
            queryBuilder
                    .append(String.format("%s%n", command.compose()));
        }
        return queryBuilder.toString();
    }
}
