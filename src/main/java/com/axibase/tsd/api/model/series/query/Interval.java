package com.axibase.tsd.api.model.series.query;

import com.axibase.tsd.api.model.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interval {
    private int count;
    private TimeUnit unit;
}
