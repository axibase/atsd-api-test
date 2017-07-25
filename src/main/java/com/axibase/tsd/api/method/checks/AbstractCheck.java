package com.axibase.tsd.api.method.checks;


public abstract class AbstractCheck {
    private static final String DEFAULT_CHECK_MESSAGE = "Failed to check!";

    public abstract boolean isChecked();

    public String getErrorMessage() {
        return DEFAULT_CHECK_MESSAGE;
    }
}
