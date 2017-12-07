package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;

@Data
@Accessors(chain = true)
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
    private Period interval;
    private Map<String, String> tags;
    private Aggregate aggregate;
    private Group group;
    private String timeFormat;
    private Boolean exactMatch;
    private Integer limit;
    private Boolean cache;
    private String direction;
    private Integer seriesLimit;
    private Boolean versioned;
    private Boolean addMeta;
    private SeriesType type;

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
        type = series.getType();
    }

    public SeriesQuery(String entity, String metric) {
        this.entity = entity;
        this.metric = metric;
    }

    public SeriesQuery(String entity, String metric, long startTime, long endTime) {
        this(entity, metric, Util.ISOFormat(startTime), Util.ISOFormat(endTime), new HashMap<>());
    }

    public SeriesQuery(String entity, String metric, String startDate, String endDate) {
        this(entity, metric, startDate, endDate, new HashMap<>());
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

    public void addTag(String tag, String value) {
        tags.put(tag, value);
    }

    private void setIntervalBasedOnSeriesDate(final Series series) {
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

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
