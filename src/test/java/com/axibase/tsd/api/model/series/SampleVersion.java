package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleVersion {
    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty(value = "d")
    protected void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SampleVersion other = (SampleVersion) o;
        return timestamp == null && other.timestamp == null || other.timestamp.equals(timestamp);
    }
}
