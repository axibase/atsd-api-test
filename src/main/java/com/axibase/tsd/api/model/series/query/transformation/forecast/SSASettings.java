package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSASettings {
    private List<Language> implementation;
    private SSADecompositionSettings decompose;
    private SSAGroupingSettings group;
    private SSAReconstructSettings reconstruct;
    private SSAForecastSettings forecast;
}
