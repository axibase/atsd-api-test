package com.axibase.tsd.api.model.command;

import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;

import java.util.Map;

public class EntityCommand extends AbstractCommand {
    private static final String ENTITY_COMMAND_TEXT = "entity";
    private String name;
    private String label;
    private String timeZoneID;
    private InterpolationMode interpolationMode;
    private Map<String, String> tags;

    public EntityCommand() {
        super(ENTITY_COMMAND_TEXT);
    }


    public EntityCommand(Entity entity) {
        this();
        setLabel(entity.getLabel());
        setName(entity.getName());
        setTimeZoneID(entity.getTimeZoneID());
        setTags(entity.getTags());
        setInterpolationMode(entity.getInterpolationMode());
    }

    public String getTimeZoneID() {
        return timeZoneID;
    }

    public void setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public InterpolationMode getInterpolationMode() {
        return interpolationMode;
    }

    public void setInterpolationMode(InterpolationMode interpolationMode) {
        this.interpolationMode = interpolationMode;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = commandBuilder();
        if (this.name != null) {
            stringBuilder.append(FieldFormat.quoted("e", name));
        }
        if (this.label != null) {
            stringBuilder.append(FieldFormat.quoted("l", label));
        }
        if (this.interpolationMode != null) {
            stringBuilder.append(FieldFormat.quoted("i", interpolationMode.name()));
        }
        if (this.tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                stringBuilder.append(FieldFormat.keyValue("t", entry.getKey(), entry.getValue()));
            }
        }
        if (this.timeZoneID != null) {
            stringBuilder.append(FieldFormat.quoted("z", timeZoneID));
        }

        return stringBuilder.toString();
    }
}
