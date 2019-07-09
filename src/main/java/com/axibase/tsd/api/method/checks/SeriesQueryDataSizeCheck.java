package com.axibase.tsd.api.method.checks;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;
import lombok.RequiredArgsConstructor;

import javax.ws.rs.core.Response;

@RequiredArgsConstructor
public class SeriesQueryDataSizeCheck extends AbstractCheck {
    private final SeriesQuery query;
    private final int expectedSize;

    @Override
    public boolean isChecked() {
        Response response = SeriesMethod.querySeries(query);
        if (Response.Status.Family.SUCCESSFUL != Util.responseFamily(response)) {
            return false;
        }
        int actualSize = response.readEntity(ResponseAsList.ofSeries()).stream().mapToInt(s -> s.getData().size()).sum();
        return (actualSize == expectedSize);
    }
}
