package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.checks.SeriesCheck;
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

import static java.util.Collections.singletonList;

public class EqualCharEscapeTest extends SeriesTest {
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
        Series series = new Series("series-command-test=-e3", "series-command-test-m3");
        Sample sample = new Sample(Util.getCurrentDate(), "1");
        series.addData(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getD());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getV().toString()));

        CommandMethod.sendChecked(new SeriesCheck(singletonList(series)), seriesCommand);
    }

    /**
     * #2854
     */
    @Test
    public void testMetric() throws Exception {
        Series series = new Series("series-command-test-e4", "series-command-test=-m4");
        Sample sample = new Sample(Util.getCurrentDate(), "1");
        series.addData(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getD());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getV().toString()));

        CommandMethod.sendChecked(new SeriesCheck(singletonList(series)), seriesCommand);
    }
}
