package com.axibase.tsd.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @see <a href="https://github.com/axibase/atsd-docs/blob/master/api/data/filter-date.md#date-filter-fields">api docs</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DateFilter {
    private String startDate;
    private String endDate;
    private Interval interval;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }
}
