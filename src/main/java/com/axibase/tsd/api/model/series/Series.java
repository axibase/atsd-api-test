package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
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
            Registry.Entity.checkExists(entity);
        }
        if (null != metric) {
            Registry.Metric.checkExists(metric);
        }
        this.entity = entity;
        this.metric = metric;
        this.data = new ArrayList<>();
        this.tags = new HashMap<>();
    }

    public Series(String entity, String metric, Map<String, String> tags) {
        if (null != entity) {
            Registry.Entity.checkExists(entity);
        }
        if (null != metric) {
            Registry.Metric.checkExists(metric);
        }
        this.entity = entity;
        this.metric = metric;
        this.data = new ArrayList<>();
        this.tags = tags;
    }

    public Series(String entity, String metric, String... tags) {
        this(entity, metric);

        /* Tag name-value pairs */
        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Tag name without value in arguments");
        }

        for (int i = 0; i < tags.length; i += 2) {
            String key = tags[i];
            String value = tags[i + 1];

            if (key == null || value == null || key.isEmpty() || value.isEmpty()) {
                throw new IllegalArgumentException("Series tag name or value is null or empty");
            }

            addTag(key, value);
        }
    }

    public Series copy() {
        Series copy = new Series();
        copy.setEntity(entity);
        copy.setMetric(metric);
        List<Sample> dataCopy = new ArrayList<>();
        for (Sample sample : data) {
            dataCopy.add(sample.copy());
        }
        copy.setSamples(dataCopy);
        copy.setTags(new HashMap<>(tags));
        return copy;
    }

    /**
     * Returns transformed series, with lowercase metric, entity and trimmed tags;
     * if {@code t} fields is not null it is replaced with {@code d} field.
     *
     * @return the transformed series like in ATSD response
     */
    public Series normalize() {
        Series transformedSeries = copy();
        transformedSeries.setEntity(getEntity().toLowerCase());
        transformedSeries.setMetric(getMetric().toLowerCase());

        Map<String, String> transformedTags = new HashMap<>();
        for (Map.Entry<String, String> tag : getTags().entrySet()) {
            transformedTags.put(tag.getKey().toLowerCase(), tag.getValue().trim());
        }

        transformedSeries.setTags(transformedTags);
        for (Sample sample : transformedSeries.getData()) {
            if (sample.getUnixTime() != null && sample.getRawDate() == null) {
                sample.setRawDate(Util.ISOFormat(sample.getUnixTime()));
                sample.setUnixTime(null);
            }
        }
        return transformedSeries;
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

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public List<Sample> getData() {
        return data;
    }

    public void setSamples(Collection<Sample> samples) {
        this.data = new ArrayList<>(samples);
    }

    public Series addTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }

        tags.put(key, value);
        return this;
    }

    public void addSamples(Sample... samples) {
        if (data == null) {
            data = new ArrayList<>();
        }
        Collections.addAll(data, samples);
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

    public List<SeriesCommand> toCommands() {
        List<SeriesCommand> result = new ArrayList<>();
        for (Sample s : data) {
            SeriesCommand seriesCommand = new SeriesCommand();
            seriesCommand.setEntityName(entity);
            BigDecimal value = s.getValue();
            if (value != null) {
                seriesCommand.setValues(Collections.singletonMap(metric, value.toPlainString()));
            }
            seriesCommand.setTexts(Collections.singletonMap(metric, s.getText()));
            seriesCommand.setTags(new HashMap<>(tags));
            seriesCommand.setTimeISO(s.getRawDate());
            seriesCommand.setTimeMills(s.getUnixTime());
            result.add(seriesCommand);
        }
        return result;
    }

    @Override
    public String toString() {
        return Util.prettyPrint(this);
    }
}
