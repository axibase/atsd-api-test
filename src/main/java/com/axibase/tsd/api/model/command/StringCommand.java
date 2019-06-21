package com.axibase.tsd.api.model.command;

import com.sun.org.glassfish.gmbal.Description;

@Description("Class that transforms string command to PlainCommand")
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
