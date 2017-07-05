package com.axibase.tsd.api.method.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.TestUtil.getGzipBytes;
import static java.util.Collections.singletonMap;

public class CompressedPayloadTest extends SqlTest {
    private static byte[] gzipOutput;

    private static final String entityName = entity();
    private static final String metricName = metric();

    @BeforeClass
    public static void getGzipCompressedCommand() {
        SeriesCommand command = new SeriesCommand();
        command.setTimeISO(Mocks.ISO_TIME);
        command.setEntityName(entityName);
        command.setValues(singletonMap(metricName, "1"));

        gzipOutput = getGzipBytes(command.toString());
    }

    @Test
    public void gzipPayloadTest() {
        Series series = new Series(entityName, metricName);
        series.addSamples(new Sample(Mocks.ISO_TIME, 1));
        CommandMethod.sendGzipCompressedBytes(gzipOutput);

        Checker.check(new SeriesCheck(Collections.singletonList(series)));
    }
}
