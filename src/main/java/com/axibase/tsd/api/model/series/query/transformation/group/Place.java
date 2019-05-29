package com.axibase.tsd.api.model.series.query.transformation.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place {
    private final int count;
    private final String constraint;
    private final String minimize;

    /**
     * Create a Place object with count, constraint, and minimize fields.
     *
     * @param count      - maximum count of subgroups.
     * @param constraint - expression that series from each subgroup must satisfy
     * @param minimize   - function calculated for each subgroup. Sum calculated values over all subgroups is minimised.
     *
     */
}
