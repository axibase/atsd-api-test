package com.axibase.tsd.api.transport.tcp;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Issue;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TcpParsingTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String COMMANDS_FILE = "commands.txt";
    private static final String METRICS_DIR = "metrics";
    private static final String SERIES_DIR = "series";

    private static File getFile(String name) {
        URL resourceUrl = TcpParsingTest.class.getResource(name);
        if (resourceUrl != null) {
            try {
                return new File(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                /* Shouldn't happen */
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private static File getFile(Path path) {
        return getFile(path.toString());
    }

    private String readFile(Path filePath) throws IOException {
        return FileUtils.readFileToString(getFile(filePath));
    }

    @DataProvider
    public Object[][] provideTestDirectories() {
        Path root = Paths.get("test_set");
        File rootFile = getFile(root);
        File[] list = rootFile.listFiles();
        List<Object[]> resultList = new ArrayList<>();
        if (list != null) {
            for (File testDir : list) {
                resultList.add(new Object[]{root.resolve(testDir.getName())});
            }
        }
        Object[][] result = new Object[resultList.size()][];
        resultList.toArray(result);
        return result;
    }

    private void checkMetrics(Path testDir) throws IOException, JSONException {
        Path metricsPath = testDir.resolve(METRICS_DIR);
        File metricsDir = getFile(metricsPath);
        if (metricsDir == null) {
            return;
        }
        File[] metricFiles = metricsDir.listFiles();
        if (metricFiles != null) {
            for (File metricFile : metricFiles) {
                String expectedJson = FileUtils.readFileToString(metricFile);
                Metric expectedMetric = MAPPER.readValue(expectedJson, Metric.class);
                String metricName = expectedMetric.getName();
                Checker.check(new MetricCheck(expectedMetric));
                String actualJson = MetricMethod.queryMetric(metricName).readEntity(String.class);
                JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
            }
        }
    }

    private void checkSeries(Path testDir) throws IOException, JSONException {
        Path seriesPath = testDir.resolve(SERIES_DIR);
        File seriesDir = getFile(seriesPath);
        if (seriesDir == null) {
            return;
        }
        File[] seriesFiles = seriesDir.listFiles();
        if (seriesFiles != null) {
            for (File seriesFile : seriesFiles) {
                String expectedJson = FileUtils.readFileToString(seriesFile);
                Series expectedSeries = MAPPER.readValue(expectedJson, Series[].class)[0];
                SeriesQuery query = new SeriesQuery(
                        expectedSeries.getEntity(),
                        expectedSeries.getMetric(),
                        Util.MIN_QUERYABLE_DATE,
                        Util.MAX_QUERYABLE_DATE
                );
                Checker.check(new SeriesCheck(Collections.singletonList(expectedSeries)));
                String actualJson = SeriesMethod.querySeries(query).readEntity(String.class);
                JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
            }
        }
    }

    @Issue("4411")
    @Test(dataProvider = "provideTestDirectories")
    public void testNetworkCommandParser(Path testDir) throws Exception {
        String fileContents = readFile(testDir.resolve(COMMANDS_FILE));
        TCPSender.send(fileContents);

        checkMetrics(testDir);
        checkSeries(testDir);
    }
}
