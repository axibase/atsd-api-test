package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.alert.AlertMethod;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.util.ResponseAsList;

public class AlertCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to create Alerts!";
    private AlertQuery alertQuery;

    public AlertCheck(AlertQuery alertQuery) {
        this.alertQuery = alertQuery;
    }

    @Override
    public boolean isChecked() {
        try {
            return !(AlertMethod.queryAlerts(alertQuery).readEntity(ResponseAsList.ofAlerts()).isEmpty());
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
