package com.axibase.tsd.api.model.command;


import com.axibase.tsd.api.model.message.Message;

import java.util.HashMap;
import java.util.Map;

public class MessageCommand extends AbstractCommand {
    private static final String MESSAGE_COMMAND = "message ";

    private String entityName;
    private Map<String, String> tags;
    private String text;
    private Boolean isPersistened;
    private Integer timeMills;
    private Integer timeSeconds;
    private String timeISO;

    private MessageCommand() {
        super(MESSAGE_COMMAND);
    }

    public MessageCommand(String entityName) {
        this();
        setEntityName(entityName);
    }

    public MessageCommand(Message message) {
        this(message.getEntity());
        setTags(message.getTags());
        setPersistened(message.getPersist());
        setText(message.getMessage());
        setTags(new HashMap<>(message.getTags()));
        if (message.getSeverity() != null) {
            tags.put("severity", message.getSeverity());
        }
        if (message.getType() != null) {
            tags.put("type", message.getType());
        }
        setTimeISO(message.getDate());
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getPersistened() {
        return isPersistened;
    }

    public void setPersistened(Boolean persistened) {
        isPersistened = persistened;
    }

    public String getTimeISO() {
        return timeISO;
    }

    public void setTimeISO(String timeISO) {
        this.timeISO = timeISO;
    }

    public Integer getTimeSeconds() {
        return timeSeconds;
    }

    public void setTimeSeconds(Integer timeSeconds) {
        this.timeSeconds = timeSeconds;
    }

    public Integer getTimeMills() {
        return timeMills;
    }

    public void setTimeMills(Integer timeMills) {
        this.timeMills = timeMills;
    }

    @Override
    public String compose() {
        StringBuilder stringBuilder = commandBuilder();
        if (this.entityName != null) {
            stringBuilder.append(FieldFormat.quoted("e", entityName));
        }
        if (this.text != null) {
            stringBuilder.append(FieldFormat.quoted("m", text));
        }
        if (this.isPersistened != null) {
            stringBuilder.append(FieldFormat.quoted("d", this.isPersistened.toString()));
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
