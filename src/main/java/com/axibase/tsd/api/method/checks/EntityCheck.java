package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.model.entity.Entity;

public class EntityCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to create Entity!";
    private Entity entity;

    public EntityCheck(Entity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isChecked() {
        try {
            return EntityMethod.entityExist(entity);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE, e);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
