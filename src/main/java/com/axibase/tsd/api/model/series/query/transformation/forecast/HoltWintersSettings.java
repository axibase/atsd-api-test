package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class HoltWintersSettings {
    /**
     * Optional. Will be selected automatically if not specified.
     * Period (seasonality at Holt-Winters parlance) of incoming series.
     */
    private Period period;

    /** Holt-Winters settings. */
    private boolean auto = true;
    private Double alpha;
    private Double beta;
    private Double gamma;

}
