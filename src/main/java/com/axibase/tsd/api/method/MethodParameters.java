package com.axibase.tsd.api.method;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.WebTarget;
import java.util.Map;
import java.util.Stack;

public abstract class MethodParameters {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap() {
        return MAPPER.convertValue(this, Map.class);
    }

    @SuppressWarnings("unchecked")
    private static WebTarget appendMap(WebTarget target, Stack<String> path, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value != null) {
                path.push(entry.getKey());
                if (value instanceof String) {
                    target = target.queryParam(String.join(".", path), value);
                } else if (value instanceof Map) {
                    appendMap(target, path, (Map<String, Object>) value);
                } else if (value instanceof MethodParameters) {
                    appendMap(target, path, ((MethodParameters) value).toMap());
                }
                path.pop();
            }
        }
        return target;
    }

    WebTarget appendTo(WebTarget target) {
        return appendMap(target, new Stack<>(), toMap());
    }
}
