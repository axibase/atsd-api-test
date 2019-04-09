package com.axibase.tsd.api.model.series.query.transformation.group;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Group {
    GroupType type;
    List<GroupType> types;
    Period period;
    AggregationInterpolate interpolate;
    Boolean truncate;
    Integer order;
    List<String> groupByEntityAndTags;

    public Group(GroupType type) {
        this(type, null, null);
    }

    public Group(GroupType type, Period period) {
        this(type, period, null);
    }

    public Group(GroupType type, Period period, Integer order) {
        this.type = type;
        this.order = order;
        this.period = period;
    }
}