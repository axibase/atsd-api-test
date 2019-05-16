package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.alert.AlertMethod;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.util.Util;

import javax.ws.rs.core.Response;
import java.util.Collection;

public class AlertsCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to create Alerts!";
    private Collection<AlertQuery> alerts;

    public AlertsCheck(Collection<AlertQuery> alerts) {
        this.alerts = alerts;
    }

    @Override
    public boolean isChecked() {
        try {
            if(Response.Status.Family.CLIENT_ERROR.equals(Util.responseFamily(AlertMethod.queryAlerts(alerts)))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
