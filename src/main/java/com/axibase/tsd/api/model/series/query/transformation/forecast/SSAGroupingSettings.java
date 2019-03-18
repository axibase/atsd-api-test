package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSAGroupingSettings {
    private SSAAutoGroupingSettings auto;
    private SSAManualGroupingSettings manual;
}
