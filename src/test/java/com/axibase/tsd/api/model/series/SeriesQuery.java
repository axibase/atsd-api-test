package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.Interval;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SeriesQuery {
    private String entity;
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

    public SeriesQuery() {
    }

    public SeriesQuery(Series series)  {
        setEntity(series.getEntity());
        setTags(new HashMap<>(series.getTags()));
        setExactMatch(true);
        setMetric(series.getMetric());
        if (series.getData().size() == 0) {
            setStartDate(BaseMethod.MIN_QUERYABLE_DATE);
            setEndDate(BaseMethod.MAX_QUERYABLE_DATE);
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

    private void setIntervalBasedOnSeriesDate(final Series series) throws  IllegalArgumentException {
        try {
            Long minDate = Util.getMillis(BaseMethod.MAX_QUERYABLE_DATE);
            Long maxDate = Util.getMillis(BaseMethod.MIN_QUERYABLE_DATE);

            Long curDate;
            for (Sample sample : series.getData()) {
                curDate = sample.getT();
                if (curDate == null) {
                    curDate = Util.getMillis(sample.getD());
                }
                minDate = Math.min(curDate, minDate);
                maxDate = Math.max(curDate, maxDate);
            }

            setStartDate(Util.ISOFormat(minDate));
            setEndDate(Util.ISOFormat(maxDate + 1));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Fail to parse date  string to millis");
        }

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

    public void addTags(String tag, String value) {
        tags.put(tag, value);
    }

    @Override
    public String toString() {
        return "Query{" +
                "entity='" + entity + '\'' +
                ", metric='" + metric + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
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
}
