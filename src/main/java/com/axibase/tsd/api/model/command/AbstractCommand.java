package com.axibase.tsd.api.model.command;


public abstract class AbstractCommand implements PlainCommand {
    private String commandText;

    public AbstractCommand(String commandText) {
        this.commandText = commandText;
    }

    @Override
    public String compose() {
        return toString();
    }

    protected StringBuilder commandBuilder() {
        return new StringBuilder(String.format("%s ", commandText));
    }
}
