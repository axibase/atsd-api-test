package com.axibase.tsd.api.method;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.WebTarget;
import java.util.*;

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
                    target = target.queryParam(formatParameterName(path), value);
                } else if (value instanceof Map) {
                    appendMap(target, path, (Map<String, Object>) value);
                } else if (value instanceof Collection) {
                    target = target.queryParam(formatParameterName(path), formatCollection((Collection)value));
                } else if (value instanceof MethodParameters) {
                    appendMap(target, path, ((MethodParameters) value).toMap());
                } else {
                    target = target.queryParam(formatParameterName(path), value.toString());
                }
                path.pop();
            }
        }
        return target;
    }

    private static String formatParameterName(Iterable<String> parameterName) {
        return String.join(".", parameterName);
    }

    private static String formatCollection(Collection collection) {
        StringJoiner joiner = new StringJoiner(",");
        for (Object obj: collection) {
            if (obj == null) {
                continue;
            }

            joiner.add(obj.toString());
        }
        return joiner.toString();
    }

    WebTarget appendTo(WebTarget target) {
        return appendMap(target, new Stack<>(), toMap());
    }
}
