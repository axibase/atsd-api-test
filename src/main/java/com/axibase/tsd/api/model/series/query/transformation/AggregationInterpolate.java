package com.axibase.tsd.api.model.series.query.transformation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AggregationInterpolate {
    private AggregationInterpolateType type;
    private Integer value;
    private Boolean extend;

    public AggregationInterpolate(AggregationInterpolateType type) {
        this(type, null);
    }

    public AggregationInterpolate(AggregationInterpolateType type, Boolean extend) {
        this.type = type;
        this.extend = extend;
    }
}