package com.axibase.tsd.api.model.series.query.transformation.group;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
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
    Place place;

    public Group(GroupType type) {
        this(type, null, null);
    }

    public Group addType(GroupType type) {
        if (types == null) {
            types = new ArrayList<>();
            if (this.type != null) {
                types.add(this.type);
            }
        }
        types.add(type);
        return this;
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