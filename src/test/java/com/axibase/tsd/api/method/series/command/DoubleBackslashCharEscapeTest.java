package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.util.TestUtil.getCurrentDate;

public class DoubleBackslashCharEscapeTest extends SeriesTest {
    private final static Map DEFAULT_PROPERTY_TAGS;

    static {
        DEFAULT_PROPERTY_TAGS = new HashMap();
        DEFAULT_PROPERTY_TAGS.put("t1", "tv1");
    }

    /**
     * #2854
     */
    @Test
    public void testEntity() throws Exception {
        Series series = new Series("series-command-test\\\\-e7", "series-command-test-m7");
        Sample sample = new Sample(getCurrentDate(), 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getD());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getV().toString()));

        CommandMethod.send(seriesCommand);
        assertSeriesExisting(series);
    }

    /**
     * #2854
     */
    @Test
    public void testMetric() throws Exception {
        Series series = new Series("series-command-test-e8", "series-command-test\\\\-m8");
        Sample sample = new Sample(getCurrentDate(), 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getD());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getV().toString()));

        CommandMethod.send(seriesCommand);
        assertSeriesExisting(series);
    }
}
