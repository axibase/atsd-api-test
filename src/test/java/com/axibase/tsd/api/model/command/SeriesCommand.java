package com.axibase.tsd.api.model.command;


import java.util.Map;

public class SeriesCommand extends AbstractCommand {
    private static final String SERIES_COMMAND = "series";
    private Map<String, String> texts;
    private Map<String, String> values;
    private String entityName;
    private Map<String, String> tags;
    private Integer timeMills;
    private Integer timeSeconds;
    private String timeISO;


    public SeriesCommand() {
        super(SERIES_COMMAND);
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Integer getTimeMills() {
        return timeMills;
    }

    public void setTimeMills(Integer timeMills) {
        this.timeMills = timeMills;
    }

    public Integer getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(Integer timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public String getTimeISO() {
        return timeISO;
    }

    public void setTimeISO(String timeISO) {
        this.timeISO = timeISO;
    }

    public Map<String, String> getTexts() {
        return texts;
    }

    public void setTexts(Map<String, String> texts) {
        this.texts = texts;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public String toString() {
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
        return stringBuilder.toString();
    }
}
