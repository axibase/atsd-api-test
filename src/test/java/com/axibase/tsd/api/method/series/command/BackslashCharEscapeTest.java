package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BackslashCharEscapeTest extends SeriesTest {
    private final static Map DEFAULT_PROPERTY_TAGS;

    static {
        DEFAULT_PROPERTY_TAGS = new HashMap();
        DEFAULT_PROPERTY_TAGS.put("t1", "tv1");
    }

    @Issue("2854")
    @Test
    public void testEntity() throws Exception {
        Series series = new Series("series-command-test\\-e5", "series-command-test-m5");
        Sample sample = Sample.ofDateInteger(Mocks.ISO_TIME, 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getRawDate());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getValue().toString()));

        CommandMethod.send(seriesCommand);
        assertSeriesExisting(series);
    }

    @Issue("2854")
    @Test
    public void testMetric() throws Exception {
        Series series = new Series("series-command-test-e6", "series-command-test\\-m6");
        Sample sample = Sample.ofDateInteger(Mocks.ISO_TIME, 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getRawDate());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getValue().toString()));

        CommandMethod.send(seriesCommand);
        assertSeriesExisting(series);
    }
}
