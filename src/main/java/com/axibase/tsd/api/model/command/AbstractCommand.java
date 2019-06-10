package com.axibase.tsd.api.model.command;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractCommand implements PlainCommand {
    private final String commandText;

    @Override
    public String toString() {
        return compose();
    }

    protected StringBuilder commandBuilder() {
        return new StringBuilder(commandText).append(' ');
    }
}
