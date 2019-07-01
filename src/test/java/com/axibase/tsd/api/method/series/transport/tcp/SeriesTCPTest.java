package com.axibase.tsd.api.method.series.transport.tcp;

import com.axibase.tsd.api.method.series.SeriesTest;
import com.axibase.tsd.api.model.command.PlainCommand;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import java.util.Date;

import static org.testng.AssertJUnit.*;

public class SeriesTCPTest extends SeriesTest {

    private static Date TEST_DATE = Util.parseDate("2019-06-20T12:00:00.000Z");

    @Issue ("6319")
    @Test
    public void testNormalWorkflow() throws Exception {
        Series series = new Series(Mocks.entity(), Mocks.metric());
        series.addSamples(Sample.ofJavaDateInteger(TEST_DATE, 22));
        PlainCommand command = new SeriesCommand(series);
        assertTrue(Transport.TCP.send(command));
        assertSeriesExisting(series);
    }

    @Issue("6319")
    @Test(
            description = "Malformed parameter - entity with whitespaces"
    )
    public void testMalformedRequest() throws Exception {
        Series series = new Series(Mocks.entity().replaceAll("-", " "), Mocks.metric());
        series.addSamples(Sample.ofJavaDateInteger(TEST_DATE, 22));
        PlainCommand command = new SeriesCommand(series);
        assertFalse(Transport.TCP.send(command));
    }

    @Issue("6319")
    @Test
    public void testSpecialCharactersEscape() throws Exception {
        Series series = new Series(Mocks.entity().replaceAll("-", "\\=\\\\\"-"), Mocks.metric().replaceAll("-", "\\=\\\\\"-"));
        series.addSamples(Sample.ofJavaDateInteger(TEST_DATE, 22));
        PlainCommand command = new SeriesCommand(series);
        assertTrue(Transport.TCP.send(command));
        assertSeriesExisting(series);
    }

}
