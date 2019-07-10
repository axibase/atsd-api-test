package com.axibase.tsd.api.method.series.command;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.method.series.SeriesTest.assertSeriesExisting;

public class DQuoteCharEscapeTest extends SeriesMethod {
    private final static Map DEFAULT_PROPERTY_TAGS;
    private final Transport transport;

    static {
        DEFAULT_PROPERTY_TAGS = new HashMap();
        DEFAULT_PROPERTY_TAGS.put("t1", "tv1");
    }

    @Factory(dataProvider = "transport", dataProviderClass = Transport.class)
    public DQuoteCharEscapeTest(Transport transport) {
        this.transport = transport;
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testEntity() throws Exception {
        Series series = new Series(Mocks.entity().replaceAll("-", "\""), Mocks.metric());
        Sample sample = Sample.ofDateInteger(Mocks.ISO_TIME, 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getRawDate());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getValue().toString()));

        transport.send(seriesCommand);
        assertSeriesExisting(series);
    }

    @Issue("2854")
    @Issue("6319")
    @Test
    public void testMetric() throws Exception {
        Series series = new Series(Mocks.entity(), Mocks.metric().replaceAll("-", "\""));
        Sample sample = Sample.ofDateInteger(Mocks.ISO_TIME, 1);
        series.addSamples(sample);

        SeriesCommand seriesCommand = new SeriesCommand();
        seriesCommand.setTimeISO(sample.getRawDate());
        seriesCommand.setEntityName(series.getEntity());
        seriesCommand.setValues(Collections.singletonMap(series.getMetric(), sample.getValue().toString()));

        transport.send(seriesCommand);
        assertSeriesExisting(series);
    }
}
