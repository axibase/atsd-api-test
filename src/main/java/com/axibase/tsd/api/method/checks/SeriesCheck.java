package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.method.BaseMethod.compareJsonString;
import static com.axibase.tsd.api.method.series.SeriesMethod.querySeries;

@Slf4j
public class SeriesCheck extends AbstractCheck {
    private static final String ERROR_MESSAGE = "Failed to check series list insert.";
    private List<Series> seriesList;

    public SeriesCheck(List<Series> seriesList) {
        this.seriesList = seriesList;
    }

    @Override
    public String getErrorMessage() {
        return ERROR_MESSAGE;
    }

    @Override
    public boolean isChecked() {
        try {
            return seriesListIsInserted(seriesList, false);
        } catch (Exception e) {
            log.error("Unexpected error on series check. Reason: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean isChecked(boolean enableLogging) {
        try {
            return seriesListIsInserted(seriesList, enableLogging);
        } catch (Exception e) {
            log.error("Unexpected error on series check. Reason: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    public boolean seriesListIsInserted(final List<Series> seriesList, boolean enableLogging) throws Exception {
        List<SeriesQuery> seriesQueryList = new ArrayList<>();
        List<Series> formattedSeriesList = new ArrayList<>();
        for (final Series series : seriesList) {
            seriesQueryList.add(new SeriesQuery(series));
            Series formattedSeries = series.copy();
            formattedSeries.setTags(series.getFormattedTags());
            formattedSeriesList.add(formattedSeries);
        }
        Response response = querySeries(seriesQueryList);
        String expected = BaseMethod.getJacksonMapper().writeValueAsString(formattedSeriesList);
        String actual = response.readEntity(String.class);
        final boolean areEqual = compareJsonString(expected, actual);
        if (enableLogging && !areEqual) {
            log.warn("SeriesCheck#seriesListIsInserted result is false. Expected: {}. Actual: {}",
                    expected, actual);
        }
        return areEqual;
    }
}
