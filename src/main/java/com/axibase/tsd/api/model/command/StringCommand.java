package com.axibase.tsd.api.model.command;

/**
 * Class that transforms string command to PlainCommand
 */
public class StringCommand extends AbstractCommand {
    private final String command;

    public StringCommand(String command) {
        super("");
        this.command = command;
    }

    @Override
    public String compose() {
        return command;
    }
}
