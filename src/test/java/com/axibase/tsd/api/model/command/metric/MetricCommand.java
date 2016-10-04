package com.axibase.tsd.api.model.command.metric;


import com.axibase.tsd.api.model.command.AbstractCommand;
import com.axibase.tsd.api.model.metric.Interpolate;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;

import java.util.Map;

import static org.apache.commons.lang3.StringEscapeUtils.escapeCsv;

public class MetricCommand extends AbstractCommand {
    private static final String METRIC_COMMAND = "metric";
    private String metricName;
    private String label;
    private String description;
    private String filterExpression;
    private Boolean versioning;
    private DataType dataType;
    private Map<String, String> tags;
    private Interpolate interpolate;

    private MetricCommand(String metricName) {
        super(METRIC_COMMAND);
        this.metricName = metricName;
    }

    public MetricCommand(String metricName, DataType dataType) {
        this(metricName);
        this.dataType = dataType;
        build();
    }

    public MetricCommand(String metricName, String label) {
        this(metricName);
        this.label = label;
        build();
    }

    public MetricCommand(String metricName, Boolean versioning) {
        this(metricName);
        this.versioning = versioning;
        build();
    }

    public MetricCommand(String metricName, Map<String, String> tags) {
        this(metricName);
        this.tags = tags;
        build();
    }

    public MetricCommand(String metricName, Interpolate interpolate) {
        this(metricName);
        this.interpolate = interpolate;
        build();
    }

    public MetricCommand(Metric metric) {
        this(metric.getName());
        appendLabel(metric.getLabel());
        appendInterpolate(metric.getInterpolate());
        appendDescription(metric.getDescription());
        appendFilterExpression(metric.getFilter());
        appendVersioning(metric.getVersioned());
        appendDataType(metric.getDataType());
        appendTags(metric.getTags());
        build();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public MetricCommand setTags(Map<String, String> tags) {
        this.tags = tags;
        build();
        return this;
    }

    public DataType getDataType() {

        return dataType;
    }

    public MetricCommand setDataType(DataType dataType) {
        this.dataType = dataType;
        build();
        return this;
    }

    public String getLabel() {
        return label;
    }

    public MetricCommand setLabel(String label) {
        this.label = label;
        build();
        return this;
    }

    @Override
    protected void appendField(String field, String type) {
        super.appendField(field, escapeCsv(type));
    }

    public String getMetricName() {
        return metricName;
    }

    private void appendMetricName(String metricName) {
        appendField("m", metricName);
    }

    private void appendLabel(String metricName) {
        appendField("l", metricName);
    }

    private void appendDescription(String description) {
        appendField("d", description);
    }

    private void appendFilterExpression(String filterExpression) {
        appendField("f", filterExpression);
    }

    private void appendVersioning(Boolean versioning) {
        appendField("v", versioning.toString());
    }

    private void appendDataType(DataType dataType) {
        appendField("p", dataType.toString());
    }

    private void appendTag(Map.Entry<String, String> tag) {
        appendField("t", String.format("%s=%s", tag.getKey(), tag.getValue()));
    }

    private void appendTags(Map<String, String> tags) {
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            appendTag(tag);
        }
    }

    private void appendInterpolate(Interpolate interpolate) {
        appendField("i", interpolate.toString());
    }

    @Override
    protected void build() {
        clean();
        if (this.metricName != null) {
            appendMetricName(this.metricName);
        }

        if (this.label != null) {
            appendLabel(this.label);
        }

        if (this.description != null) {
            appendDescription(this.description);
        }
        if (this.filterExpression != null) {
            appendFilterExpression(this.filterExpression);
        }

        if (this.versioning != null) {
            appendVersioning(this.versioning);
        }
        if (this.dataType != null) {
            appendDataType(this.dataType);
        }
        if (this.tags != null) {
            appendTags(this.tags);
        }
        if (interpolate != null) {
            appendField("i", interpolate.toString());
        }
    }

    public String getDescription() {
        return description;
    }

    public MetricCommand setDescription(String description) {
        this.description = description;
        build();
        return this;
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public MetricCommand setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
        build();
        return this;
    }

    public Boolean getVersioning() {
        return versioning;
    }

    public MetricCommand setVersioning(Boolean versioning) {
        this.versioning = versioning;
        build();
        return this;
    }

    public Interpolate getInterpolate() {
        return interpolate;
    }

    public MetricCommand setInterpolate(Interpolate interpolate) {
        this.interpolate = interpolate;
        build();
        return this;
    }
}
