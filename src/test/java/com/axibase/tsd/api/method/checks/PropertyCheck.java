package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.property.Property;

public class PropertyCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to insert property!";
    private Property property;

    public PropertyCheck(Property property) {
        this.property = property;
    }

    @Override
    public boolean isChecked() {
        try {
            return PropertyMethod.propertyExist(property);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }
}
