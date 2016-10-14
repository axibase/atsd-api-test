package com.axibase.tsd.api.model.message;

public enum SeverityAlias {
    INFO(Severity.NORMAL),
    WARN(Severity.WARNING),
    ERROR(Severity.CRITICAL);

    private Severity severity;


    SeverityAlias(Severity severity) {
        this.severity = severity;
    }

    public Severity getSeverity() {
        return severity;
    }
}
