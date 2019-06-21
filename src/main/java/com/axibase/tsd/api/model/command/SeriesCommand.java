package com.axibase.tsd.api.model.command;



import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import lombok.Data;
import lombok.experimental.Accessors;


import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class SeriesCommand extends AbstractCommand {
    private static final String SERIES_COMMAND = "series";
    private Map<String, String> texts;
    private Map<String, String> values;
    private String entityName;
    private Map<String, String> tags;
    private Long timeMills;
    private Long timeSeconds;
    private String timeISO;
    private Boolean append;


    public SeriesCommand() {
        super(SERIES_COMMAND);
    }

    public SeriesCommand(Map<String, String> texts, Map<String, String> values, String entityName,
                         Map<String, String> tags, Long timeMills, Long timeSeconds,
                         String timeISO, Boolean append) {
        super(SERIES_COMMAND);
        this.texts = texts;
        this.values = values;
        this.entityName = entityName;
        this.tags = tags;
        this.timeMills = timeMills;
        this.timeSeconds = timeSeconds;
        this.timeISO = timeISO;
        this.append = append;
    }

    public SeriesCommand(Series series) {
        super(SERIES_COMMAND);
        if(!series.getData().isEmpty()) {
            Sample sample = series.getData().get(0);
            if(sample.getText() != null) {
                addText(series.getMetric(), sample.getText());
            }

            if(sample.getValue() != null) {
                addValue(series.getMetric(), sample.getValue().toString());
            }
            this.timeISO = sample.getRawDate();
        }
        this.entityName = series.getEntity();
        this.tags = series.getTags();
    }

    public void addTag(String key, String value) {
        if(this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(key, value);
    }

    public void addValue(String metric, String value) {
        if(this.values == null) {
            this.values = new HashMap<>();
        }
        this.values.put(metric, value);
    }

    public void addText(String metric, String text) {
        if(this.texts == null) {
            this.texts = new HashMap<>();
        }
        this.texts.put(metric, text);
    }

    @Override
    public String compose() {
        StringBuilder stringBuilder = commandBuilder();
        if (this.entityName != null) {
            stringBuilder.append(FieldFormat.quoted("e", entityName));
        }
        if (this.texts != null) {
            for (Map.Entry<String, String> entry : texts.entrySet()) {
                stringBuilder.append(FieldFormat.keyValue("x", entry.getKey(), entry.getValue()));
            }
        }
        if (this.values != null) {
            for (Map.Entry<String, String> entry : values.entrySet()) {
                stringBuilder.append(FieldFormat.keyValue("m", entry.getKey(), entry.getValue()));
            }
        }
        if (this.timeSeconds != null) {
            stringBuilder.append(FieldFormat.quoted("s", timeSeconds.toString()));
        }
        if (this.timeMills != null) {
            stringBuilder.append(FieldFormat.quoted("ms", timeMills.toString()));
        }
        if (this.timeISO != null) {
            stringBuilder.append(FieldFormat.quoted("d", timeISO));
        }
        if (this.tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                stringBuilder.append(FieldFormat.keyValue("t", entry.getKey(), entry.getValue()));
            }
        }
        if (this.append != null) {
            stringBuilder.append(FieldFormat.quoted("a", append.toString()));
        }
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return compose();
    }
}
