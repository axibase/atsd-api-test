package com.axibase.tsd.api.method.export;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.export.ExportForm;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TimeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Insert several time series, and export them with and without aggregation.
 */
public class SeriesExportTest extends ExportMethod {
    private static final String startA = "2021-04-01T10:00:00Z";
    private static final String endA = "2021-04-01T11:00:00Z";
    private static final String startB = "2021-04-01T11:00:00Z";
    private static final String endB = "2021-04-01T12:00:00Z";
    private static final String metric = Mocks.metric();
    private static final String entity = Mocks.entity();
    private static final String tagName = "tag-name";
    private static final Set<String> tagValues = Arrays.stream(new String[]{
            "tag-value-a", "tag-value-b", "tag-value-c", "tag-value-d"
    }).collect(Collectors.toSet());

    @BeforeClass
    public static void insertSeries() throws Exception {
        List<Series> seriesList = tagValues
                .stream()
                .map(SeriesExportTest::series)
                .collect(Collectors.toList());

        seriesList.get(0).addSamples(everyMinute(startA, endA, 1));
        seriesList.get(1).addSamples(everyMinute(startA, endA, 1));
        seriesList.get(2).addSamples(everyMinute(startB, endB, 1));
        seriesList.get(3).addSamples(everyMinute(startB, endB, 1));

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    private static Series series(String tagValue) {
        return new Series(entity, metric, false, tagName, tagValue);
    }

    @DataProvider
    public Object[][] testCases() throws UnsupportedEncodingException, JsonProcessingException {
        Object[][] testCases = new Object[][]{
                {requestParameters(null), "value"},
                {requestParameters("COUNT"), "count"}
        };
        return testCases;
    }

    private Map<String, Object> requestParameters(String aggregationFunction) throws JsonProcessingException, UnsupportedEncodingException {
        ExportForm exportForm = new ExportForm()
                .setMetric(metric)
                .setEntity(entity)
                .setStartTime(startA)
                .setEndTime(endB)
                .setExportFormat("CSV")
                .setDateFormat("ISO8601_SECONDS");
        if (aggregationFunction != null) {
            exportForm.setAggregateInterval("1-MINUTE").setAggregations(new String[]{aggregationFunction});
        }
        String formString = jacksonMapper.writeValueAsString(exportForm);
        String encodedForm = URLEncoder.encode(formString, StandardCharsets.UTF_8.toString());
        return Collections.singletonMap("settings", encodedForm);
    }

    @Test(dataProvider = "testCases")
    public void testCSVExport(Map<String, Object> requestParameters, String valueColumnName) throws IOException {
        /* Parse response as CSV. */
        Response response = sendGetRequest(requestParameters);
        Assert.assertEquals(response.getStatusInfo().toEnum(), Response.Status.OK);
        String responseStr = response.readEntity(String.class);
        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        CSVParser parser = CSVParser.parse(responseStr, csvFormat);

        /* Check csv header. */
        Set<String> headerNames = parser.getHeaderNames().stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> expectedHeaderNames = Arrays.stream(new String[]{
                "timestamp", "metric", "entity", tagName, valueColumnName})
                .collect(Collectors.toSet());

        Assert.assertEquals(headerNames, expectedHeaderNames);

        /* Check that expected series records go in order. */
        String expectedMetric = metric.toLowerCase();
        String expectedEntity = entity.toLowerCase();
        Set<String> actualTagValues = new HashSet<>();
        String tagValue = null;
        int samplesCount = 0;
        for (CSVRecord record : parser) {
            Assert.assertEquals(record.get("Metric"), expectedMetric);
            Assert.assertEquals(record.get("Entity"), expectedEntity);
            String newTagValue = record.get(tagName);
            Assert.assertNotNull(newTagValue);
            if (!newTagValue.equals(tagValue)) {
                if (tagValue != null) {
                    Assert.assertEquals(samplesCount, 60);
                    samplesCount = 0;
                }
                tagValue = newTagValue;
                actualTagValues.add(newTagValue);
            }
            samplesCount++;
        }
        Assert.assertEquals(samplesCount, 60);
        Assert.assertEquals(actualTagValues, tagValues);
    }

    private static List<Sample> everyMinute(String startDate, String endDate, int value) {
        long minute = 60_000;
        List<Sample> samples = new ArrayList<>();
        long millis = TimeUtil.epochMillis(startDate);
        long endMillis = TimeUtil.epochMillis(endDate);
        while (millis < endMillis) {
            samples.add(Sample.ofTimeInteger(millis, value));
            millis += minute;
        }
        return samples;
    }
}
