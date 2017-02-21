package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.metric.Metric;

public class MetricCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to insert metric!";
    private Metric metric;

    public MetricCheck(Metric metric) {
        this.metric = metric;
    }

    @Override
    public boolean isChecked() {
        try {
            return MetricMethod.metricExist(metric);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
