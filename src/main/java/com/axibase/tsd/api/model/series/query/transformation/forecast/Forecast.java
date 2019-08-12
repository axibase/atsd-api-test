package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
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
public class Forecast {
    /** Enable input series aggregation by specified aggregationFunction before the forecast */
    private boolean autoAggregate;

    /** Aggregation function used if autoAggregate is true. Default: "avg" */
    private AggregationType aggregationFunction;

    /** Forecast length. Required. */
    private Horizon horizon;

    /** Interval used to score forecast. Optional. If null then default score interval will be calculated. */
    private Interval scoreInterval;

    /** Optional. Include input series, forecast, and reconstructed series into response? Default - include forecast. */
    private List<SeriesType> include;

    /* Optional. Order in sequence of other transformations. */
    private int order = 0;

    SSASettings ssa;
    ARIMASettings arima;
    HoltWintersSettings hw;

    public boolean includeForecast() {
        return include.contains(SeriesType.FORECAST);
    }

    public boolean includeReconstructed() {
        return  include.contains(SeriesType.RECONSTRUCTED);
    }

    public boolean includeHistory() {
        return include.contains(SeriesType.HISTORY);
    }

    public int algorithmsCount() {
        int count = 0;
        if (ssa != null) count++;
        if (arima != null) count++;
        if (hw != null) count++;
        return count;
    }

    /** Add specified {@link SeriesType} to this Forecast {@link #include} list. */
    public Forecast include(SeriesType type) {
        if (include == null) {
            include = new ArrayList<>();
        }
        if (!include.contains(type)) {
            include.add(type);
        }
        return this;
    }
}
