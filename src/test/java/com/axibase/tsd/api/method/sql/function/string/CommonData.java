package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.Util.TestNames.generateEntityName;
import static com.axibase.tsd.api.method.BaseMethod.DEFAULT_EXPECTED_PROCESSING_TIME;


class CommonData {
    public final static Sample DEFAULT_SAMPLE = new Sample("2016-06-03T09:23:00.000Z", 0);
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

    static void prepareApplyTestData(String testMetric) throws FileNotFoundException, InterruptedException {
        String entityName = generateEntityName();
        Series series = new Series(entityName, testMetric);
        series.setEntity(entityName);
        series.addData(DEFAULT_SAMPLE);
        SeriesMethod.insertSeries(Collections.singletonList(series));
        Thread.sleep(DEFAULT_EXPECTED_PROCESSING_TIME);//Check is not working here
    }
}
