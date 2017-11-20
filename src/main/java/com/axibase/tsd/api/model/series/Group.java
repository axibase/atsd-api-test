package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Group {
    GroupType type;
    Period period;
    Interpolate interpolate;
    Boolean truncate;
    Integer order;

    public Group(GroupType type) {
        this(type, null, null);
    }

    public Group(GroupType type, Period period) {
        this(type, period, null);
    }

    public Group(GroupType type, Period period, Integer order) {
        this.type = type;
        this.order = order;
        this.period = period;
    }

    public GroupType getType() {
        return type;
    }

    public void setType(GroupType type) {
        this.type = type;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Interpolate getInterpolate() {
        return interpolate;
    }

    public void setInterpolate(Interpolate interpolate) {
        this.interpolate = interpolate;
    }

    public Boolean getTruncate() {
        return truncate;
    }

    public void setTruncate(Boolean truncate) {
        this.truncate = truncate;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}