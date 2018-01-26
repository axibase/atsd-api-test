package com.axibase.tsd.api.model.series.query.transformation.rate;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Rate {
    Period period;
    Boolean counter;
    Integer order;

    public Rate(Period period) {
        this.period = period;
    }

    public Rate(Period period, Integer order) {
        this.period = period;
        this.order = order;
    }
}
