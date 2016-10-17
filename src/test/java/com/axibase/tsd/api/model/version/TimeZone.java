
package com.axibase.tsd.api.model.version;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TimeZone {

    private String name;
    private Integer offsetMinutes;


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public Integer getOffsetMinutes() {
        return offsetMinutes;
    }


    public void setOffsetMinutes(Integer offsetMinutes) {
        this.offsetMinutes = offsetMinutes;
    }
}
