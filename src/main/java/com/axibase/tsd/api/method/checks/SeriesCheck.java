package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.method.BaseMethod.compareJsonString;
import static com.axibase.tsd.api.method.series.SeriesMethod.querySeries;

public class SeriesCheck extends AbstractCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(SeriesCheck.class);
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
            LOGGER.error("Unexpected error on series check. Reason: {}", e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private boolean seriesListIsInserted(final List<Series> seriesList) throws Exception {
        List<SeriesQuery> seriesQueryList = new ArrayList<>();
        List<Series> transformedSeriesList = new ArrayList<>();
        for (final Series series : seriesList) {
            seriesQueryList.add(new SeriesQuery(series));
            transformedSeriesList.add(series.normalize());
        }
        Response response = querySeries(seriesQueryList);
        String expected = BaseMethod.getJacksonMapper().writeValueAsString(transformedSeriesList);
        String actual = response.readEntity(String.class);
        return compareJsonString(expected, actual);
    }
}
