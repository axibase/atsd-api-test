
package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "timeZone",
        "startDate",
        "currentTime",
        "localDate",
        "currentDate"
})
public class Date {

    @JsonProperty("timeZone")
    private TimeZone timeZone;
    @JsonProperty("startDate")
    private String startDate;
    @JsonProperty("currentTime")
    private Long currentTime;
    @JsonProperty("localDate")
    private String localDate;
    @JsonProperty("currentDate")
    private String currentDate;

    /**
     * @return The timeZone
     */
    @JsonProperty("timeZone")
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone The timeZone
     */
    @JsonProperty("timeZone")
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return The startDate
     */
    @JsonProperty("startDate")
    public String getStartDate() {
        return startDate;
    }

    /**
     * @param startDate The startDate
     */
    @JsonProperty("startDate")
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return The currentTime
     */
    @JsonProperty("currentTime")
    public Long getCurrentTime() {
        return currentTime;
    }

    /**
     * @param currentTime The currentTime
     */
    @JsonProperty("currentTime")
    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * @return The localDate
     */
    @JsonProperty("localDate")
    public String getLocalDate() {
        return localDate;
    }

    /**
     * @param localDate The localDate
     */
    @JsonProperty("localDate")
    public void setLocalDate(String localDate) {
        this.localDate = localDate;
    }

    /**
     * @return The currentDate
     */
    @JsonProperty("currentDate")
    public String getCurrentDate() {
        return currentDate;
    }

    /**
     * @param currentDate The currentDate
     */
    @JsonProperty("currentDate")
    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

}
