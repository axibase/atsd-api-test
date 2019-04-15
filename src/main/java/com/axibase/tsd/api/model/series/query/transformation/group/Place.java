package com.axibase.tsd.api.model.series.query.transformation.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Place {
    private int count;
    private String constraint;
    private String minimize;

    /**
     * Create a Place object with count, constraint, and minimize fields.
     *
     * @param count      - maximum count of subgroups.
     * @param constraint - expression that series from each subgroup must satisfy
     * @param minimize   - Function calculated for each subgroup. Sum calculated values over all subgroups is minimised.
     *
     */
    public Place(final int count, final String constraint, final PlaceFunction minimize) {
        this.count = count;
        this.constraint = constraint;
        this.minimize = minimize.toString();
    }
}
