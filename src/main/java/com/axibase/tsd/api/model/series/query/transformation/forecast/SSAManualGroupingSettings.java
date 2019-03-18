package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class SSAManualGroupingSettings {

    private List<String> groups;

    /**
     * Build reconstructed series and forecast for each of specified group of indexes (indexes are 1-based).
     */
    private List<Set<Integer>> etGroups;
}
