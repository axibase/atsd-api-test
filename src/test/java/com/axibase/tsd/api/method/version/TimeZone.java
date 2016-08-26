
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeZone {

    @JsonProperty("name")
    private String name;
    @JsonProperty("offsetMinutes")
    private Integer offsetMinutes;

    /**
     * @return The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The offsetMinutes
     */
    @JsonProperty("offsetMinutes")
    public Integer getOffsetMinutes() {
        return offsetMinutes;
    }

    /**
     * @param offsetMinutes The offsetMinutes
     */
    @JsonProperty("offsetMinutes")
    public void setOffsetMinutes(Integer offsetMinutes) {
        this.offsetMinutes = offsetMinutes;
    }

}
