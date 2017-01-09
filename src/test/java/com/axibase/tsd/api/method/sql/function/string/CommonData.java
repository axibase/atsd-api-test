package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util.TestNames;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


class CommonData {
    public final static List<String> POSSIBLE_FUNCTION_ARGS = Arrays.asList(
            "entity",
            "metric",
            "tags",
            "tags.a",
            "tags.'a'",
            "tags.\"a\"",
            "metric.tags",
            "metric.tags.a",
            "metric.tags.'a'",
            "metric.tags.\"a\"",
            "entity.tags",
            "entity.tags.a",
            "entity.tags.'a'",
            "entity.tags.\"a\"",
            "entity.groups",
            "entity.label",
            "metric.label",
            "metric.timezone",
            "metric.interpolate",
            "text",
            "'a'"
    );

    static void prepareApplyTestData(String testMetric) throws Exception {
        String entityName = TestNames.entity();
        Series series = new Series(entityName, testMetric);
        series.setEntity(entityName);
        series.addData(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }
}
