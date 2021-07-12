package com.axibase.tsd.api.method.export;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.command.MetricCommand;
import com.axibase.tsd.api.model.export.ExportForm;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.transport.Transport;
import com.axibase.tsd.api.util.Mocks;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

public class VersionedSeriesExportTest extends ExportMethod {
    private final LocalDate date = LocalDate.parse("2021-04-01");
    private final ZoneId zoneId = ZoneId.of("UTC");
    private final String metric = Mocks.metric();
    private final String entity = Mocks.entity();
    private final String tagName = "tag-name";
    private final String tagA = "tag-value-a";
    private final String tagB = "tag-value-b";
    private final String tagC = "tag-value-c";
    private final List<String> basicColNames = Arrays.asList(
            "Timestamp", "Value", "Metric", "Entity", tagName
    );
    private final List<String> versionColNames = Arrays.asList(
            "Version Source", "Version Status", "Version Time"
    );

    /**
     * Insert following 3 versioned series with text annotations of samples.
     * They have the same metric and entity, but different values of the tag.
     *
     Timestamp	          Value	Text	        tag-name
     2021-04-01 10:00:00	1	single version	tag-value-a
     2021-04-01 10:01:00	1		            tag-value-a
     2021-04-01 10:01:00	2	last version	tag-value-a

     2021-04-01 10:00:00	1	first version	tag-value-b
     2021-04-01 10:00:00	2		            tag-value-b
     2021-04-01 10:00:00	3	last version	tag-value-b
     2021-04-01 10:01:00	1	single version	tag-value-b
     2021-04-01 10:02:00	1	first version	tag-value-b
     2021-04-01 10:02:00	2	last version	tag-value-b

     2021-04-01 10:01:00	1	first version	tag-value-c
     2021-04-01 10:01:00	2	last version	tag-value-c
     2021-04-01 10:02:00	1		            tag-value-c
     2021-04-01 10:02:00	2		            tag-value-c
     2021-04-01 10:03:00	1	single version	tag-value-c
     */
    @BeforeTest
    public void insertSeries() throws Exception {
        // turn on versioning for the metric
        Metric metricObj = new Metric(metric);
        metricObj.setVersioned(true);
        MetricCommand command = new MetricCommand(metricObj);
        Transport.TCP.send(command);

        Series seriesA = series(tagA,
                sample("10:01", 1)
        );
        Series seriesB = series(tagB,
                sample("10:00", 1, "first version")
        );
        Series seriesC = series(tagC,
                sample("10:01", 1, "first version"),
                sample("10:02", 1)
        );
        SeriesMethod.insertSeriesCheck(seriesA, seriesB, seriesC);

        // need a pause to insert next versions
        Thread.sleep(200);
        seriesB = series(tagB,
                sample("10:00", 2),
                sample("10:02", 1, "first version")
        );
        SeriesMethod.insertSeriesCheck(seriesB);

        // need a pause to insert next versions
        Thread.sleep(200);
        seriesA = series(tagA,
                sample("10:00", 1, "single version"),
                sample("10:01", 2, "last version")
        );
        seriesB = series(tagB,
                sample("10:00", 3, "last version"),
                sample("10:01", 1, "single version"),
                sample("10:02", 2, "last version")
        );
        seriesC = series(tagC,
                sample("10:01", 2, "last version"),
                sample("10:02", 2),
                sample("10:03", 1, "single version")
        );
        SeriesMethod.insertSeriesCheck(seriesA, seriesB, seriesC);
    }

    @DataProvider
    public Object[][] testCases() {
        return new Object[][]{
            {
                form(),
                series(tagA,
                        sample("10:00", 1),
                        sample("10:01", 2)),
                series(tagB,
                        sample("10:00", 3),
                        sample("10:01", 1),
                        sample("10:02", 2)),
                series(tagC,
                        sample("10:01", 2),
                        sample("10:02", 2),
                        sample("10:03", 1))
            },
            {
                form().setIncludeTextColumn(true),
                series(tagA,
                        sample("10:00", 1, "single version"),
                        sample("10:01", 2, "last version")),
                series(tagB,
                        sample("10:00", 3, "last version"),
                        sample("10:01", 1, "single version"),
                        sample("10:02", 2, "last version")),
                series(tagC,
                        sample("10:01", 2, "last version"),
                        sample("10:02", 2, ""),
                        sample("10:03", 1, "single version"))
            },
            {
                form().setVersioning(true).setUseFilter(true).setIncludeTextColumn(true),
                series(tagA,
                        sample("10:00", 1, "single version"),
                        sample("10:01", 1, ""),
                        sample("10:01", 2, "last version")),
                series(tagB,
                        sample("10:00", 1, "first version"),
                        sample("10:00", 2, ""),
                        sample("10:00", 3, "last version"),
                        sample("10:01", 1, "single version"),
                        sample("10:02", 1, "first version"),
                        sample("10:02", 2, "last version")),
                series(tagC,
                        sample("10:01", 1, "first version"),
                        sample("10:01", 2, "last version"),
                        sample("10:02", 1, ""),
                        sample("10:02", 2, ""),
                        sample("10:03", 1, "single version"))
            },
            {
                form().setVersioning(true).setUseFilter(true),
                series(tagA,
                        sample("10:00", 1),
                        sample("10:01", 1),
                        sample("10:01", 2)),
                series(tagB,
                        sample("10:00", 1),
                        sample("10:00", 2),
                        sample("10:00", 3),
                        sample("10:01", 1),
                        sample("10:02", 1),
                        sample("10:02", 2)),
                series(tagC,
                        sample("10:01", 1),
                        sample("10:01", 2),
                        sample("10:02", 1),
                        sample("10:02", 2),
                        sample("10:03", 1))
            },
            {
                form().setVersioning(true).setUseFilter(true).setVersionFilter("value=1").setIncludeTextColumn(true),
                series(tagA,
                        sample("10:00", 1, "single version"),
                        sample("10:01", 1, "")),
                series(tagB,
                        sample("10:00", 1, "first version"),
                        sample("10:01", 1, "single version"),
                        sample("10:02", 1, "first version")),
                series(tagC,
                        sample("10:01", 1, "first version"),
                        sample("10:02", 1, ""),
                        sample("10:03", 1, "single version"))
            },
            {
                form().setVersioning(true).setUseFilter(true).setVersionFilter("value=1"),
                series(tagA,
                        sample("10:00", 1),
                        sample("10:01", 1)),
                series(tagB,
                        sample("10:00", 1),
                        sample("10:01", 1),
                        sample("10:02", 1)),
                series(tagC,
                        sample("10:01", 1),
                        sample("10:02", 1),
                        sample("10:03", 1))
            },
            {
                form().setVersioning(true).setUseFilter(true).setVersionFilter("value=2").setIncludeTextColumn(true),
                series(tagA,
                        sample("10:01", 2, "last version")),
                series(tagB,
                        sample("10:00", 2, ""),
                        sample("10:02", 2, "last version")),
                series(tagC,
                        sample("10:01", 2, "last version"),
                        sample("10:02", 2, ""))
            },
            {
                form().setVersioning(true).setUseFilter(true).setVersionFilter("value=2"),
                series(tagA,
                        sample("10:01", 2)),
                series(tagB,
                        sample("10:00", 2),
                        sample("10:02", 2)),
                series(tagC,
                        sample("10:01", 2),
                        sample("10:02", 2))
            },
            {
                form().setVersioning(true).setUseFilter(true).setRevisionsOnly(true).setIncludeTextColumn(true),
                series(tagA,
                        sample("10:01", 1, ""),
                        sample("10:01", 2, "last version")),
                series(tagB,
                        sample("10:00", 1, "first version"),
                        sample("10:00", 2, ""),
                        sample("10:00", 3, "last version"),
                        sample("10:02", 1, "first version"),
                        sample("10:02", 2, "last version")),
                series(tagC,
                        sample("10:01", 1, "first version"),
                        sample("10:01", 2, "last version"),
                        sample("10:02", 1, ""),
                        sample("10:02", 2, ""))
            },
            {
                form().setVersioning(true).setUseFilter(true).setRevisionsOnly(true),
                series(tagA,
                        sample("10:01", 1),
                        sample("10:01", 2)),
                series(tagB,
                        sample("10:00", 1),
                        sample("10:00", 2),
                        sample("10:00", 3),
                        sample("10:02", 1),
                        sample("10:02", 2)),
                series(tagC,
                        sample("10:01", 1),
                        sample("10:01", 2),
                        sample("10:02", 1),
                        sample("10:02", 2))
            },
            {
                form().setVersioning(true).setUseFilter(true).setRevisionsOnly(true).setVersionFilter("value=1").setIncludeTextColumn(true),
                series(tagA,
                        sample("10:01", 1, "")),
                series(tagB,
                        sample("10:00", 1, "first version"),
                        sample("10:02", 1, "first version")),
                series(tagC,
                        sample("10:01", 1, "first version"),
                        sample("10:02", 1, "")),
            },
            {
                form().setVersioning(true).setUseFilter(true).setRevisionsOnly(true).setVersionFilter("value=1"),
                series(tagA,
                        sample("10:01", 1)),
                series(tagB,
                        sample("10:00", 1),
                        sample("10:02", 1)),
                series(tagC,
                        sample("10:01", 1),
                        sample("10:02", 1)),
            },
        };
    }

    @Test(dataProvider = "testCases")
    public void test(ExportForm form, Series a, Series b, Series c) throws IOException {
        CSVParser parser = responseParser(form);
        checkHeader(form, parser);
        List<Series> actual = parseSeries(parser);
        Assert.assertEquals(actual.size(), 3);
        checkSeries(a, actual);
        checkSeries(b, actual);
        checkSeries(c, actual);
    }

    private void checkSeries(Series expected, List<Series> actualSeriesList) {
        String tag = expected.getTag(tagName);
        Series actual = selectSeriesByTag(tag, actualSeriesList);
        Assert.assertNotNull(actual, "Series with tag value " + tag + " not found in response.");
        Assert.assertEquals(expected.compareTo(actual), 0, "Series keys are different.");
        List<Sample> actSamples = actual.getData();
        List<Sample> expSamples = expected.getData();
        String seriesCheckError = "Expected and actual series with tag '%s' are different.%n" +
                "Expected series: %s%n" + "Actual series: %s%n" + "%s%n";
        Assert.assertEquals(actSamples.size(), expSamples.size(),
                String.format(seriesCheckError, tag, expected, actual, "They have different samples count."));
        int count = expSamples.size();
        for (int i = 0; i < count; i++) {
            Assert.assertTrue(actSamples.get(i).theSame(expSamples.get(i)),
                    String.format(seriesCheckError, tag, expected, actual, "They are different at sample with index " + i));
        }
    }

    private Series selectSeriesByTag(String tag, List<Series> actualSeriesList) {
        for (Series series : actualSeriesList) {
            if (tag.equals(series.getTag(tagName))) return series;
        }
        return null;
    }

    private List<Series> parseSeries(CSVParser parser) {
        List<Series> result = new ArrayList<>();
        String currentTag = null;
        Series currentSeries = null;
        for (CSVRecord record : parser) {
            String tag = record.get(tagName);
            Assert.assertNotNull(tag, "Response row does not contain tag " + tagName + ": " + record);
            if (currentTag == null || !currentTag.equals(tag)) {
                currentTag = tag;
                currentSeries = new Series(entity, metric, tagName, tag);
                result.add(currentSeries);
            }
            Sample sample = Sample.ofDateInteger(record.get("Timestamp"), Integer.parseInt(record.get("Value")));
            if (record.isMapped("Text")) {
                sample.setText(record.get("Text"));
            }
            currentSeries.addSample(sample);
        }
        return result;
    }

    private void checkHeader(ExportForm form, CSVParser parser) {
        Set<String> actual = new HashSet<>(parser.getHeaderNames());
        Set<String> expected = new HashSet<>(basicColNames);
        if (form.isIncludeTextColumn()) {
            expected.add("Text");
        }
        if (form.isVersioning()) {
            expected.addAll(versionColNames);
        }
        Assert.assertEquals(actual, expected);
    }

    private CSVParser responseParser(ExportForm form) throws IOException {
        Response response = sendGetRequest(toMap(form));
        Assert.assertEquals(response.getStatusInfo().toEnum(), Response.Status.OK);
        String responseStr = response.readEntity(String.class);
        CSVFormat csvFormat = CSVFormat.DEFAULT.withFirstRecordAsHeader();
        return CSVParser.parse(responseStr, csvFormat);
    }

    private ExportForm form() {
        return new ExportForm()
                .setMetric(metric)
                .setEntity(entity)
                .setStartTime("2021-04-01T00:00:00Z")
                .setEndTime("2021-04-02T00:00:00Z")
                .setExportFormat("CSV")
                .setDateFormat("ISO8601_SECONDS");
    }

    private Map<String, Object> toMap(ExportForm exportForm) throws JsonProcessingException, UnsupportedEncodingException {
        String formString = jacksonMapper.writeValueAsString(exportForm);

        /* WARNING:
        * URLEncoder replaces the space " " by "+", instead of "%20",
        * and lately Jersey encodes this string as a query parameter,
        * and replaces the "+" by "%2B".
        * And after decoding on ATSD side we have "+" instead of " ".
        */
        String encodedForm = URLEncoder.encode(formString, StandardCharsets.UTF_8.toString());

        return Collections.singletonMap("settings", encodedForm);
    }

    private Sample sample(String time, int value) {
        LocalTime localTime = LocalTime.parse(time);
        LocalDateTime ldt = LocalDateTime.of(date, localTime);
        ZonedDateTime zdt = ZonedDateTime.of(ldt, zoneId);
        return Sample.ofJavaDateInteger(zdt, value);
    }

    private Sample sample(String time, int value, String annotation) {
        Sample sample = sample(time, value);
        sample.setText(annotation);
        return sample;
    }

    private Series series(String tagValue, Sample... samples) {
        Series series = new Series(entity, metric, false, tagName, tagValue);
        series.addSamples(samples);
        return series;
    }
}
