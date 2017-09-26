package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;


public class CommonData {
    public final static List<String> POSSIBLE_STRING_FUNCTION_ARGS = Collections.unmodifiableList(
            Arrays.asList(
                    "entity",
                    "metric",
                    "tags",
                    "tags.a",
                    "tags.\"a\"",
                    "metric.tags",
                    "metric.tags.a",
                    "metric.tags.\"a\"",
                    "entity.tags",
                    "entity.tags.a",
                    "entity.tags.\"a\"",
                    "entity.groups",
                    "entity.label",
                    "metric.label",
                    "metric.timezone",
                    "metric.interpolate",
                    "text",
                    "'a'"
            )
    );

    static void insertSeriesWithMetric(String testMetric) throws Exception {
        String entityName = entity();
        Series series = new Series(entityName, testMetric);
        series.addSamples(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }
}
