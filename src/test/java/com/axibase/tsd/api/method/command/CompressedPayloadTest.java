package com.axibase.tsd.api.method.command;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.extended.CommandMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.command.SeriesCommand;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.NotCheckedException;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertTrue;

public class CompressedPayloadTest extends SqlTest {
    private static String outputGzipFilePath;

    private static final String entityName = entity();
    private static final String metricName = metric();

    private static boolean checkResult(Series series) {
        boolean checked = true;
        try {
            Checker.check(new SeriesCheck(Collections.singletonList(series)));
        }
        catch (NotCheckedException e) {
            checked = false;
        }
        return checked;
    }

    private void createGzipFile() throws IOException {
        SeriesCommand command = new SeriesCommand();
        command.setTimeISO(Mocks.ISO_TIME);
        command.setEntityName(entityName);
        command.setValues(singletonMap(metricName, "1"));

        File file = File.createTempFile("payload", ".txt");
        try(PrintWriter out = new PrintWriter(file.getAbsolutePath())){
            out.println(command.toString());
        }

        File outputFile = File.createTempFile("payload", ".txt.gz");
        outputGzipFilePath = outputFile.getAbsolutePath();

        byte[] buffer = new byte[1024];

        GZIPOutputStream gzos =
                new GZIPOutputStream(new FileOutputStream(outputGzipFilePath));

        FileInputStream in =
                new FileInputStream(file.getAbsolutePath());

        int len;
        while ((len = in.read(buffer)) > 0) {
            gzos.write(buffer, 0, len);
        }

        in.close();
        gzos.finish();
        gzos.close();
    }

    @Test
    public void gzipPayloadTest() {
        try {
            createGzipFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Series series = new Series(entityName, metricName);
        series.addSamples(new Sample(Mocks.ISO_TIME, 1));
        CommandMethod.sendGzipFileCommand(outputGzipFilePath);
        checkResult(series);

        assertTrue(checkResult(series), "User was't able to send compressed data to ATSD");
    }
}
