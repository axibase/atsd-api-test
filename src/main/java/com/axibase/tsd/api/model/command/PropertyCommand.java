package com.axibase.tsd.api.model.command;

import com.axibase.tsd.api.model.property.Property;

import java.util.Map;


public class PropertyCommand extends AbstractCommand {
    private static final String PROPERTY_COMMAND = "property";
    private String entityName;
    private String propertyType;
    private Map<String, String> keys;
    private Map<String, String> tags;
    private Integer timeMills;
    private Integer timeSeconds;
    private String timeISO;

    public PropertyCommand() {
        super(PROPERTY_COMMAND);
    }

    public PropertyCommand(Property property) {
        this();
        setEntityName(property.getEntity());
        setPropertyType(property.getType());
        setKeys(property.getKey());
        setTags(property.getTags());
        setTimeISO(property.getDate());
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Map<String, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, String> keys) {
        this.keys = keys;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = commandBuilder();
        if (this.entityName != null) {
            stringBuilder.append(FieldFormat.quoted("e", entityName));
        }
        if (this.propertyType != null) {
            stringBuilder.append(FieldFormat.quoted("t", propertyType));
        }
        if (this.keys != null) {
            for (Map.Entry<String, String> entry : keys.entrySet()) {
                stringBuilder.append(FieldFormat.keyValue("k", entry.getKey(), entry.getValue()));
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
                stringBuilder.append(FieldFormat.keyValue("v", entry.getKey(), entry.getValue()));
            }
        }
        return stringBuilder.toString();
    }
}
