package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.*;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Series {
    private String entity;
    private String metric;
    private List<Sample> data;
    private Map<String, String> tags;
    private SeriesType type;
    private String forecastName;
    private SeriesMeta meta;

    public Series() {
        data = new ArrayList<>();
        tags = new HashMap<>();
        type = SeriesType.HISTORY;
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
        type = SeriesType.HISTORY;
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
        type = SeriesType.HISTORY;
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
        copy.setType(type);
        copy.setForecastName(forecastName);
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

    public void setSamples(Collection<Sample> samples) {
        setData(new ArrayList<>(samples));
    }

    public Series addTag(String key, String value) {
        if (tags == null) {
            tags = new HashMap<>();
        }

        tags.put(key, value);
        return this;
    }

    public Series addSamples(Sample... samples) {
        if (data == null) {
            data = new ArrayList<>();
        }
        Collections.addAll(data, samples);
        return this;
    }

    public Series addSamples(final List<Sample> samples) {
        if (data == null) {
            data = samples;
        } else {
            data.addAll(samples);
        }
        return this;
    }

    public List<SeriesCommand> toCommands() {
        if (type == SeriesType.FORECAST) {
            throw new IllegalArgumentException("Cannot convert FORECAST series to commands");
        }
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
