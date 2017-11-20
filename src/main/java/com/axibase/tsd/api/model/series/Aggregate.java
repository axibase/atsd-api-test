package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Aggregate {
    AggregationType type;
    List<AggregationType> types;
    Period period;
    Interpolate interpolate;
    Integer order;

    public Aggregate() {
    }

    public Aggregate(AggregationType type) {
        this(type, null, null);
    }

    public Aggregate(AggregationType type, Period period) {
        this(type, period, null);
    }

    public Aggregate(AggregationType type, Period period, Integer order) {
        this.type = type;
        this.period = period;
        this.order = order;
    }

    public Integer getOrder() {
        return order;
    }

    public AggregationType getType() {
        return type;
    }

    public void setType(AggregationType type) {
        this.type = type;
    }

    public List<AggregationType> getTypes() {
        return types;
    }

    public void setTypes(List<AggregationType> types) {
        this.types = types;
    }

    public void addType(AggregationType type) {
        if (types == null) {
            types = new ArrayList<>();
        }
        types.add(type);
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
}