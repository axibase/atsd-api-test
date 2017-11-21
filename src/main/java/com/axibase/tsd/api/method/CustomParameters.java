package com.axibase.tsd.api.method;

import java.util.HashMap;
import java.util.Map;

public class CustomParameters extends MethodParameters {
    private Map<String, Object> params = new HashMap<>();

    @Override
    protected Map<String, Object> toMap() {
        return params;
    }

    public CustomParameters addParameter(String name, Object value) {
        params.put(name, value);
        return this;
    }
}
