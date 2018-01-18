package com.axibase.tsd.api.model.series.query.transformation.aggregate;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class Aggregate {
    AggregationType type;
    List<AggregationType> types;
    Period period;
    AggregationInterpolate interpolate;
    Integer order;
    Threshold threshold;

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

    public void addType(AggregationType type) {
        if (types == null) {
            types = new ArrayList<>();
        }
        types.add(type);
    }
}