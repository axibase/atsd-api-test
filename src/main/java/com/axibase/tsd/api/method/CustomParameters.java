package com.axibase.tsd.api.method;

import java.util.HashMap;
import java.util.Map;


public class CustomParameters extends MethodParameters {
    private Map<String, Object> params = new HashMap<>();

    @Override
    protected Map<String, Object> toMap() {
        return params;
    }

    public static <T> CustomParameters of(final Map<String, T> params) {
        CustomParameters parameters = new CustomParameters();
        params.forEach(parameters::addParameter);
        return parameters;
    }

    public CustomParameters addParameter(final String name, final Object value) {
        params.put(name, value);
        return this;
    }
}
