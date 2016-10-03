package com.axibase.tsd.api.model.command;


public class SimpleCommand extends AbstractCommand {
    public SimpleCommand(String commandText) {
        super(commandText);
    }

    @Override
    protected void build() {
        clean();
    }

    @Override
    public void appendField(String field, String type) {
        super.appendField(field, type);
    }
}
