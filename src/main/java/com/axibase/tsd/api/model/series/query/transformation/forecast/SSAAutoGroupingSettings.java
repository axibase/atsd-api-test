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
public class SSAAutoGroupingSettings {

    /*
     * Optional.
     * k >= 0 - return forecast/reconstruction for first k groups of eigentriples clustered based on w-correlation matrix.
     */
    private int count = 1;

    /* List groups which should be united to single group and produce forecast for that group.
     * Incompatible with 'stack' = true. */
    private List<Set<Character>> union;

    /**
     * Optional.
     * Is used alongside with the 'groupCount' setting.
     * This setting is not applied if the 'union' is specified.
     * If true then produced k groups will be joined:
     * 1, 1 + 2, 1 + 2+ 3, ... , 1 + 2 + ... + k before forecasting/reconstruction.
     */
    private Boolean stack;

    private Clustering clustering;
}
