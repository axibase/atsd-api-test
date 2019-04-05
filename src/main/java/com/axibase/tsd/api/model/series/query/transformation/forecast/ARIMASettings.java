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
public class ARIMASettings {
    /**
     * If true then parameters p, d will be selected automatically.
     */
    private boolean auto = true;

    /**
     * Another way to choose number of members in autoregressive part of the ARIMA.
     * So previous samples within this interval participate in calculation.
     */
    private Interval autoRegressionInterval;

    /* Length of autoregressive part of ARIMA model. */
    private Integer p;

    /* Number of differentiations in ARIMA model. */
    private Integer d;
}
