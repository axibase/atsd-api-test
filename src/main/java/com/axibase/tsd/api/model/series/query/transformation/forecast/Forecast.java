package com.axibase.tsd.api.model.series.query.transformation.forecast;

import com.axibase.tsd.api.model.series.SeriesType;
import com.axibase.tsd.api.model.series.query.Interval;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Forecast {
    /* Forecast length. Required. */
    private Horizon horizon;

    /* Interval used to score forecast. Optional. If null then default score interval will be calculated. */
    private Interval scoreInterval;

    /* Optional. Include input series, forecast, and reconstructed series into response? Default - include forecast. */
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
}
