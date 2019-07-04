package com.axibase.tsd.api.model.series.query.transformation.aggregate;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@Accessors(chain = true)
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

    public Aggregate addType(AggregationType type) {
        if (types == null) {
            types = new ArrayList<>();
            if (this.type != null) {
                types.add(this.type);
            }
        }
        types.add(type);
        return this;
    }
}