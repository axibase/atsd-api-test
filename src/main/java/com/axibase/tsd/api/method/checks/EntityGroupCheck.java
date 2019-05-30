package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.entitygroup.EntityGroupMethod;
import com.axibase.tsd.api.model.entitygroup.EntityGroup;

public class EntityGroupCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to create Entity Group!";
    private EntityGroup entityGroup;

    public EntityGroupCheck(EntityGroup entityGroup) {
        this.entityGroup = entityGroup;
    }

    @Override
    public boolean isChecked() {
        try {
            return EntityGroupMethod.entityGroupExist(entityGroup);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
