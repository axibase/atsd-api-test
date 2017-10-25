package com.axibase.tsd.api.method.series;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.NotCheckedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.fail;

public class SeriesTest extends SeriesMethod {
    public static void assertSeriesExisting(String assertMessage, List<Series> seriesList) {
        try {
            Checker.check(new SeriesCheck(seriesList));
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }

    public static void assertSeriesExisting(List<Series> seriesList) {
        assertSeriesExisting(DefaultAssertMessages.SERIES_INSERTING, seriesList);
    }

    public static void assertSeriesExisting(Series... series) {
        assertSeriesExisting(Arrays.asList(series));
    }

    public static void assertSeriesExisting(String assertMessage, Series series) {
        assertSeriesExisting(assertMessage, Collections.singletonList(series));
    }

    public static void assertSeriesExisting(Series series) {
        assertSeriesExisting(DefaultAssertMessages.SERIES_INSERTING, series);
    }

    public static void assertSeriesQueryDataSize(String assertMessage, final SeriesQuery query, final Integer size) {
        try {
            Checker.check(new AbstractCheck() {
                @Override
                public boolean isChecked() {
                    try {
                        List<Series> seriesList = querySeriesAsList(query);
                        if (seriesList.size() != 1) {
                            return false;
                        } else {
                            return seriesList.get(0).getData().size() == size;
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException("Failed to get series list");
                    }
                }
            });
        } catch (NotCheckedException e) {
            fail(assertMessage);
        }
    }

    public static void assertSeriesQueryDataSize(SeriesQuery seriesQuery, Integer size) {
        assertSeriesQueryDataSize(DefaultAssertMessages.SERIES_QUERY_DATA_SIZE, seriesQuery, size);
    }

    private static class DefaultAssertMessages {
        private static final String SERIES_INSERTING = "Inserted series can not be received";
        private static final String SERIES_QUERY_DATA_SIZE = "Incorrect data size of returned series";
    }
}
