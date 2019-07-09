package com.axibase.tsd.api.model.command;

/**
 * Class that transforms string command to PlainCommand
 */
public class StringCommand extends AbstractCommand {

    public StringCommand(String command) {
        super(command);
    }

    @Override
    public String compose() {
        return commandBuilder().toString();
    }
}
