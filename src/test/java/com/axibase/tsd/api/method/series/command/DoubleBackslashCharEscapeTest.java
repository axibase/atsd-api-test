package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.util.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertTrue;

public class DoubleBackslashCharEscapeTest extends SeriesMethod {
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
        Sample sample = new Sample(Util.getCurrentDate(), "1");
        series.addData(sample);

        String command = buildSeriesCommandFromSeriesAndSample(series, sample);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted series can not be received", SeriesMethod.seriesListIsInserted(Collections.singletonList(series)));
    }

    /**
     * #2854
     */
    @Test
    public void testMetric() throws Exception {
        Series series = new Series("series-command-test-e8", "series-command-test\\\\-m8");
        Sample sample = new Sample(Util.getCurrentDate(), "1");
        series.addData(sample);

        String command = buildSeriesCommandFromSeriesAndSample(series, sample);
        tcpSender.send(command, DEFAULT_EXPECTED_PROCESSING_TIME);

        assertTrue("Inserted series can not be received", SeriesMethod.seriesListIsInserted(Collections.singletonList(series)));
    }
}
