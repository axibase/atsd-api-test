package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSAForecastSettings {
    /**
     * Applied to R SSA implementation only.
     */
    private ForecastMethod method = ForecastMethod.RECURRENT;
    public enum ForecastMethod {
        RECURRENT, VECTOR
    }

    /**
     * Applied to R SSA implementation only, and if recurrent forecast is used.
     */
    private ForecastBase base = ForecastBase.RECONSTRUCTED;
    public enum ForecastBase {
        ORIGINAL, RECONSTRUCTED
    }
}
