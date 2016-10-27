package com.axibase.tsd.api.model.sql;

import java.util.List;

public class Error {
    private String state;
    private List<StackTraceElement> exception;
    private List<StackTraceElement> cause;
    private String message;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<StackTraceElement> getException() {
        return exception;
    }

    public void setException(List<StackTraceElement> exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<StackTraceElement> getCause() {
        return cause;
    }

    public void setCause(List<StackTraceElement> cause) {
        this.cause = cause;
    }
}
