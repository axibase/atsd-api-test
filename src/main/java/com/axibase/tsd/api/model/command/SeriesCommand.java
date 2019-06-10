package com.axibase.tsd.api.model.command;


import java.util.Map;

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

    public Long getTimeMills() {
        return timeMills;
    }

    public void setTimeMills(Long timeMills) {
        this.timeMills = timeMills;
    }

    public Long getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(Long timeSeconds) {
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

    public Boolean getAppend() {
        return append;
    }

    public void setAppend(Boolean append) {
        this.append = append;
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
}
