package com.axibase.tsd.api.model.series.query.transformation.smooth;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Smooth {
    private SmoothingType type;
    private Integer count;           // set sliding window size in samples count
    private Period interval;         // set sliding window size as time interval
    private int minimumCount = 1;    // minimal number of samples required to calculate smoothing function
    private int order;
    private boolean generateNaNs;
    private String incompleteValue;  // Value generated for incomplete window. Default: "null".
    private double range;            // EMA parameter for irregular series smoothing.
    private double factor;           // EMA parameter for regular series smoothing. Default: 0.25.

}
