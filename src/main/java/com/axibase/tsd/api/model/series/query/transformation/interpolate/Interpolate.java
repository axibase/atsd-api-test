package com.axibase.tsd.api.model.series.query.transformation.interpolate;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interpolate {
    InterpolateFunction function;
    Period period;
    Boundary boundary;
    String fill;

    public Interpolate(InterpolateFunction function, Period period) {
        this.function = function;
        this.period = period;
    }
}
