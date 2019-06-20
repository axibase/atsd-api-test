package com.axibase.tsd.api.method.series.transport.tcp;

import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.tcp.TCPSender;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.transport.tcp.TCPSender.assertBadTcpResponse;
import static com.axibase.tsd.api.transport.tcp.TCPSender.assertGoodTcpResponse;

public class SeriesTCPTest extends SeriesTest {

    @Issue ("6319")
    @Test
    public void testNormalWorkflow() throws Exception {
        Series series = new Series(Mocks.entity(), Mocks.metric());
        series.addSamples(Sample.ofTimeInteger(System.currentTimeMillis(), 22));
        PlainCommand command = new SeriesCommand(series);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertSeriesExisting(series);
    }

    @Issue("6319")
    @Test
    public void testMalformedResponse() throws Exception {
        Series series = new Series(Mocks.entity().replaceAll("-", " "), Mocks.metric());
        series.addSamples(Sample.ofTimeInteger(System.currentTimeMillis(), 22));
        PlainCommand command = new SeriesCommand(series);
        assertBadTcpResponse(TCPSender.send(command, true));
    }

    @Issue("6319")
    @Test
    public void testSpecialCharactersEscape() throws Exception {
        Series series = new Series(Mocks.entity().replaceAll("-", "\\=\\\\\"-"), Mocks.metric().replaceAll("-", "\\=\\\\\"-"));
        series.addSamples(Sample.ofTimeInteger(System.currentTimeMillis(), 22));
        PlainCommand command = new SeriesCommand(series);
        assertGoodTcpResponse(TCPSender.send(command, true));
        assertSeriesExisting(series);
    }

}
