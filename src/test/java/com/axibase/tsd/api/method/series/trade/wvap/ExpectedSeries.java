package com.axibase.tsd.api.method.series.trade.wvap;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.getUnixTime;

@Getter
class ExpectedSeries {
    @NotNull
    private final SeriesSign sign;
    @NotNull
    private final List<Sample> samples = new LinkedList<>();

    public ExpectedSeries(String entity, String side, AggregationType aggregationType, GroupType groupType) {
        this.sign = new SeriesSign(entity, side, aggregationType, groupType);
    }

    public ExpectedSeries sample(String isoTime, double value) {
        BigDecimal decimalValue = BigDecimal.valueOf(value);
        Sample sample = Sample.ofTimeDecimal(getUnixTime(isoTime), decimalValue);
        samples.add(sample);
        return this;
    }
}
