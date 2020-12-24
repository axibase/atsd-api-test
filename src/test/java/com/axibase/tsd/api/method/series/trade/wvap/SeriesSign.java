package com.axibase.tsd.api.method.series.trade.wvap;

import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesGroupInfo;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Data
class SeriesSign {
    @Nullable
    private final String entity;
    @Nullable
    private final String side;
    @Nullable
    private final AggregationType aggregationType;
    @Nullable
    private final GroupType groupType;

    public static SeriesSign of(Series series) {
        String entity = series.getEntity();
        Map<String, String> tags = series.getTags();
        String side = tags == null ? null : tags.get("side");
        Aggregate aggregate = series.getAggregate();
        AggregationType aggregationType = aggregate == null ? null : aggregate.getType();
        SeriesGroupInfo group = series.getGroup();
        GroupType groupType = group == null ? null : group.getType();
        return new SeriesSign(entity, side, aggregationType, groupType);
    }
}
