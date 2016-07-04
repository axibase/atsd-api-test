package com.axibase.tsd.api.method.sql;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import java.util.Arrays;

/**
 * @author Igor Shmagrinskiy
 */
public abstract class SqlTest extends SqlMethod {


    protected static void sendSamplesToSeries(Series series, Sample... samples) {
        boolean isSuccessInsert;
        series.setData(Arrays.asList(samples));
        try {
            isSuccessInsert = SeriesMethod.insertSeries(series, 1000);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
        if (!isSuccessInsert) {
            throw new IllegalStateException("Failed to insert series: " + series);
        }
    }
}
