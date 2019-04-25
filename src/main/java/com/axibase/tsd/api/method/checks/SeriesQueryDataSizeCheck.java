package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.List;

@RequiredArgsConstructor
public class SeriesQueryDataSizeCheck extends AbstractCheck {
    private final SeriesQuery query;
    private final Integer size;

    @Override
    public boolean isChecked() {
        Response response = SeriesMethod.querySeries(query);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            return false;
        }
        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });
        int n = seriesList.stream().mapToInt(s -> s.getData().size()).sum();
        return (n == size);
    }
}
