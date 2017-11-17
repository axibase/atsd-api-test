package com.axibase.tsd.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Interval {
    private int count;
    private TimeUnit unit;
    private PeriodAlignment align;

    public Interval(int count, TimeUnit unit) {
        this.count = count;
        this.unit = unit;
    }

    @Override
    public String toString() {
        if (align != null) {
            return String.format("%d %s %s", count, unit.toString(), align.toString());
        }
        return String.format("%d %s", count, unit.toString());
    }
}
