package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interpolate {
    private InterpolateType type;
    private Integer value;
    private Boolean extend;

    public Interpolate() {

    }

    public Interpolate(InterpolateType type) {
        this(type, null);
    }

    public Interpolate(InterpolateType type, Boolean extend) {
        this.type = type;
        this.extend = extend;
    }

    public InterpolateType getType() {
        return type;
    }

    public void setType(InterpolateType type) {
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Boolean getExtend() {
        return extend;
    }

    public void setExtend(Boolean extend) {
        this.extend = extend;
    }
}