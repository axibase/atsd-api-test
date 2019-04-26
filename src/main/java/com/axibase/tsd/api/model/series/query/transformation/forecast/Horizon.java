package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.axibase.tsd.api.model.series.query.Interval;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Horizon {

    /** Number points in horizon. */
    private int length;

    /** Duration of horizon. */
    private Interval interval;

    /** End time of horizon. */
    private String endDate;

    /** Requested horizon start time. Actual start time will be set to min(startDate, input series end time). */
    private String startDate;

}
