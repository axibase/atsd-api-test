package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSADecompositionSettings {
    /**
     * Algorithm used for SVD.
     */
    private SvdMethod method = SvdMethod.AUTO;

    /** Window length specified as percentage of the sample count in the input series. Default: 50%. Possible values: (0%, 50%]. */
    int windowLength = 50;

    /**
     The maximum number of eigentriples to be derived from the trajectory matrix.
     The returned number of eigentriples is constrained by window length.
     The limit applies to eigentriples sorted by singular value in descending order.
     */
    int eigentripleLimit = 0;

    /** Discard eigentriples if its singular value is below the specified percentage of the maximum singular value.
     * If set to 0, no eigentriples are discarded. If set to 100, only the eigentriples with the largest singular value are included. */
    double singularValueThreshold = -1;

}
