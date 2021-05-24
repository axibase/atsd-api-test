package com.axibase.tsd.api.model.financial;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class InstrumentStatistics {
    private String clazz;
    private String symbol;
    private long timestamp;
    private int micros;
    private Map<String, String> values = new LinkedHashMap<>();

    public InstrumentStatistics addValue(String key, String value) {
        values.put(key, value);
        return this;
    }

    public void validate() {
        checkRequired(clazz != null, "Class");
        checkRequired(symbol != null, "Symbol");
        checkRequired(timestamp > 0, "Timestamp");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required");
        }
    }

    private void checkRequired(boolean condition, String field) {
        if (!condition) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    public String toCsvLine() {
        validate();
        Stream<String> valuesStream = values.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue());
        return Stream.concat(Stream.of(clazz, symbol, timestamp, micros), valuesStream)
                .map(Objects::toString)
                .collect(Collectors.joining(","));
    }

    public long epochNano() {
        return (timestamp * 1000 + micros) * 1000;
    }
}