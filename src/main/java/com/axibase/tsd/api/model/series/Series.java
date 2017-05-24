package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Series {
    private String entity;
    private String metric;
    private List<Sample> data;
    private Map<String, String> tags;

    public Series() {
        data = new ArrayList<>();
        tags = new HashMap<>();
    }

    public Series(String entity, String metric) {
        if (null != entity) {
            Registry.Entity.register(entity);
        }
        if (null != metric) {
            Registry.Metric.register(metric);
        }
        this.entity = entity;
        this.metric = metric;
        this.data = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public Series(String entity, String metric, String... tags) {
        this(entity, metric);

        /* Tag name-value pairs */
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Tag name without value in arguments");
        }

        for (int i = 0; i > tags.length; i += 2) {
            addTag(tags[i], tags[i + 1]);
        }
    }

    public Series copy() {
        Series copy = new Series();
        copy.setEntity(entity);
        copy.setMetric(metric);
        copy.setData(new ArrayList<>(data));
        copy.setTags(new HashMap<>(tags));
        return copy;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @JsonIgnore
    public Map<String, String> getFormattedTags() {
        Map<String, String> formattedTags = new HashMap<>();
        for (Map.Entry<String, String> tag : tags.entrySet()) {
            formattedTags.put(tag.getKey().toLowerCase().trim(), tag.getValue().trim());
        }
        return formattedTags;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<Sample> getData() {
        return data;
    }

    public void setData(Collection<Sample> data) {
        this.data = new ArrayList<>(data);
    }

    public void addTag(String key, String value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Series tag name or value is null");
        }
        if (tags == null) {
            tags = new HashMap<>();
        }

        tags.put(key, value);
    }

    public void addData(Sample sample) {
        if (data == null) {
            data = new ArrayList<>();
        }
        data.add(sample);
    }

    public void addData(Sample... samples) {
        for (Sample sample : samples) {
            addData(sample);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Series series = (Series) o;

        return entity.equals(series.entity) && metric.equals(series.metric) &&
                data.equals(series.data) && tags.equals(series.tags);
    }

    @Override
    public int hashCode() {
        int result = entity.hashCode();
        result = 31 * result + metric.hashCode();
        result = 31 * result + data.hashCode();
        result = 31 * result + tags.hashCode();
        return result;
    }

    public Collection<SeriesCommand> toCommands() {
        Collection<SeriesCommand> result = new LinkedList<>();
        for (Sample s : data) {
            SeriesCommand seriesCommand = new SeriesCommand();
            seriesCommand.setEntityName(entity);
            seriesCommand.setValues(Collections.singletonMap(metric, s.getV().toPlainString()));
            seriesCommand.setTexts(Collections.singletonMap(metric, s.getText()));
            seriesCommand.setTags(tags);
            seriesCommand.setTimeISO(s.getD());
            seriesCommand.setTimeMills(s.getT());
            result.add(seriesCommand);
        }
        return result;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
