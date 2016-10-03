package com.axibase.tsd.api.model.command;


public abstract class AbstractCommand implements PlainCommand {
    private StringBuilder sb;
    private String commandText;

    public AbstractCommand(String commandText) {
        sb = new StringBuilder(commandText.concat(" "));
        this.commandText = commandText;
    }

    @Override
    public String compose() {
        return sb.toString();
    }

    protected void appendField(String field, String type) {
        sb.append(String.format("%s:%s ", field, type));
    }

    protected abstract void build();

    protected void clean() {
        sb.setLength(0);
        sb.append(commandText.concat(" "));
    }
}
