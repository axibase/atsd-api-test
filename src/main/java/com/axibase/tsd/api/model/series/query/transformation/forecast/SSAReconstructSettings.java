package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSAReconstructSettings {
    /**
     * Applied to java SSA implementation only.
     * true - use Fourier Transform in reconstruction stage of SSA.
     */
    private boolean fourier = true;

    /**
     * Optional setting.
     * Applicable only to java implementation of SSA.
     * Specifies how average anti-diagonals of reconstructed matrices.
     */
    private Averaging averagingFunction = Averaging.AVG;
    public enum Averaging {
        AVG, MEDIAN
    }
}
