package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.method.BaseMethod.compareJsonString;
import static com.axibase.tsd.api.method.series.SeriesMethod.querySeries;

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
            return seriesListIsInserted(seriesList);
        } catch (Exception e) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

    public boolean seriesListIsInserted(final List<Series> seriesList) throws Exception {
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
        return compareJsonString(expected, actual);
    }
}
