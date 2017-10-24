package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeriesQuery {
    private String entity;
    private String entityGroup;
    private String entityExpression;
    private String tagExpression;
    private List<String> entities;
    private String metric;
    private String startDate;
    private String endDate;
    private Interval interval;
    private Map<String, String> tags = new HashMap<>();
    private Aggregate aggregate;
    private Group group;
    private String timeFormat;
    private Boolean exactMatch;
    private Integer limit;
    private Integer seriesLimit;
    private Boolean versioned;

    public SeriesQuery() {
    }

    public SeriesQuery(Series series) {
        entity = escapeExpression(series.getEntity());
        metric = series.getMetric();
        tags = new HashMap<>();
        for (Map.Entry<String, String> keyValue : series.getTags().entrySet()) {
            tags.put(keyValue.getKey(), escapeExpression(keyValue.getValue()));
        }
        exactMatch = true;
        if (series.getData().size() == 0) {
            startDate = MIN_QUERYABLE_DATE;
            endDate = MAX_QUERYABLE_DATE;
        } else {
            setIntervalBasedOnSeriesDate(series);
        }
    }

    public SeriesQuery(String entity, String metric) {
        this.entity = entity;
        this.metric = metric;
    }

    public SeriesQuery(String entity, String metric, long startTime, long endTime) {
        this(entity, metric, Util.ISOFormat(startTime), Util.ISOFormat(endTime), new HashMap<String, String>());
    }

    public SeriesQuery(String entity, String metric, String startDate, String endDate) {
        this(entity, metric, startDate, endDate, new HashMap<String, String>());
    }

    public SeriesQuery(String entity, String metric, String startDate, String endDate, Map<String, String> tags) {
        this.entity = entity;
        this.metric = metric;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tags = tags;
    }

    private String escapeExpression(String expression) {
        StringBuilder escapedName = new StringBuilder();
        for (char c : expression.toCharArray()) {
            if (c == '*' || c == '?' || c == '\\') {
                escapedName.append('\\');
            }
            escapedName.append(c);
        }
        return escapedName.toString();
    }

    private void setIntervalBasedOnSeriesDate(final Series series) throws IllegalArgumentException {
        Long minDate = Util.getUnixTime(MAX_QUERYABLE_DATE);
        Long maxDate = Util.getUnixTime(MIN_QUERYABLE_DATE);

        Long curDate;
        for (Sample sample : series.getData()) {
            curDate = sample.getUnixTime();
            if (curDate == null) {
                curDate = Util.getUnixTime(sample.getRawDate());
            }
            minDate = Math.min(curDate, minDate);
            maxDate = Math.max(curDate, maxDate);
        }

        setStartDate(Util.ISOFormat(minDate));
        setEndDate(Util.ISOFormat(maxDate + 1));
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getSeriesLimit() {
        return seriesLimit;
    }

    public void setSeriesLimit(Integer seriesLimit) {
        this.seriesLimit = seriesLimit;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public Aggregate getAggregate() {
        return aggregate;
    }

    public void setAggregate(Aggregate aggregate) {
        this.aggregate = aggregate;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }


    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag, String value) {
        tags.put(tag, value);
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Boolean getExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(Boolean exactMatch) {
        this.exactMatch = exactMatch;
    }

    public String getEntityExpression() {
        return entityExpression;
    }

    public void setEntityExpression(String entityExpression) {
        this.entityExpression = entityExpression;
    }

    public String getEntityGroup() {
        return entityGroup;
    }

    public void setEntityGroup(String entityGroup) {
        this.entityGroup = entityGroup;
    }

    public Boolean getVersioned() {
        return versioned;
    }

    public void setVersioned(Boolean versioned) {
        this.versioned = versioned;
    }

    public String getTagExpression() {
        return tagExpression;
    }

    public void setTagExpression(String tagExpression) {
        this.tagExpression = tagExpression;
    }
}
