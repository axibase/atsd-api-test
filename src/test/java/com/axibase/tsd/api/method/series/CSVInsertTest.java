package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.checks.SeriesCheck;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static com.axibase.tsd.api.method.entity.EntityTest.assertEntityExisting;
import static com.axibase.tsd.api.method.series.SeriesTest.assertSeriesExisting;
import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.*;
import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

public class CSVInsertTest extends CSVInsertMethod {

    @DataProvider(name = "formatPatternProvider")
    private Object[][] provideFormatPattern() {
        return new Object[][]{
                {"yyyy-MM-dd'T'HH:mm:ss'Z'"},
                {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"},
                {"yyyy-MM-dd'T'HH:mm:ssXXX"},
                {"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"},
        };
    }

    @Issue("2009")
    @Test(dataProvider = "formatPatternProvider")
    public void testFormattedDate(String template) {
        Series expectedSeries = series();
        String csvPayload = String.format(
                "date, %s%n%s, %s%n",
                expectedSeries.getMetric(),
                formatISODate(parseDate(SAMPLE.getRawDate()), template), SAMPLE.getValue()
        );
        csvInsert(expectedSeries.getEntity(), csvPayload, expectedSeries.getTags());
        assertSeriesExisting(expectedSeries);
    }

    @Issue("2009")
    @Test
    public void testMultipleISOFormat() throws Exception {
        Series series = Mocks.series();
        series.setSamples(new ArrayList<Sample>());
        String header = String.format("date, %s%n", series.getMetric());
        StringBuilder payloadBuilder = new StringBuilder(header);
        String[][] dateTemplatePairs = new String[][]{
                {"2016-05-21T00:00:00.000Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"},
                {"2016-05-21T00:00:00.001Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"},
                {"2016-05-21T00:00:15.000Z", "yyyy-MM-dd'T'HH:mm:ssXXX"},
                {"2016-05-21T00:00:15.001Z", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"},
        };
        for (int i = 0; i < dateTemplatePairs.length; i++) {
            Date date = parseDate(dateTemplatePairs[i][0]);
            String pattern = dateTemplatePairs[i][1];
            String csvRow = String.format(
                    "%s, %s%n",
                    formatISODate(date, pattern), Mocks.DECIMAL_VALUE
            );
            payloadBuilder.append(csvRow);
            series.addSamples(Sample.ofDateDecimal(dateTemplatePairs[i][0], Mocks.DECIMAL_VALUE));
        }
        String csvPayload = payloadBuilder.toString();
        csvInsertCheck(
                new SeriesCheck(singletonList(series)),
                series.getEntity(),
                csvPayload,
                series.getTags()
        );
    }

    @Issue("2957")
    @Test
    public void testTimeRangeInISO() throws Exception {
        Series series = Mocks.series();
        series.setSamples(Arrays.asList(
                Sample.ofDateDecimal(MIN_STORABLE_DATE, Mocks.DECIMAL_VALUE),
                Sample.ofDateDecimal(MAX_STORABLE_DATE, Mocks.DECIMAL_VALUE)
        ));

        String csvPayload = String.format(
                "date, %s%n%s, %s%n%s, %s%n",
                series.getMetric(),
                MIN_STORABLE_DATE, Mocks.DECIMAL_VALUE,
                MAX_STORABLE_DATE, Mocks.DECIMAL_VALUE

        );
        csvInsertCheck(
                new SeriesCheck(singletonList(series)),
                series.getEntity(),
                csvPayload,
                series.getTags()
        );
    }

    @Issue("2957")
    @Test
    public void testTimeRangeInMS() {
        Series series = Mocks.series();
        series.setSamples(new ArrayList<>());
        series.addSamples(
                Sample.ofDateDecimal(MIN_STORABLE_DATE, Mocks.DECIMAL_VALUE),
                Sample.ofDateDecimal(MAX_STORABLE_DATE, Mocks.DECIMAL_VALUE)
        );

        String csvPayload = String.format(
                "time, %s%n%s, %s%n%s, %s%n",
                series.getMetric(),
                Util.parseDate(MIN_STORABLE_DATE).getTime(), Mocks.DECIMAL_VALUE,
                Util.parseDate(MAX_STORABLE_DATE).getTime(), Mocks.DECIMAL_VALUE

        );
        csvInsertCheck(
                new SeriesCheck(singletonList(series)),
                series.getEntity(),
                csvPayload,
                series.getTags()
        );
    }


    @DataProvider(name = "entityNameProvider")
    private Object[][] provideEntityName() {
        return new Object[][]{
                {"csvinsert entityname-11", "csvinsert_entityname-11",},
                {"csvinsertйёentityname-13", "csvinsertйёentityname-13"},
                {"csvinsert/entityname-12", "csvinsert/entityname-12"}

        };
    }


    @Issue("1278")
    @Test(dataProvider = "entityNameProvider")
    public void testEntityNames(String queryName, String expectedName) throws Exception {
        Entity entity = new Entity(queryName);
        Metric metric = new Metric(metric());
        String csvPayload = String.format(
                "time, %s%n0, 0%n",
                metric.getName()
        );
        Response response = csvInsert(entity.getName(), csvPayload);
        String assertMessage = String.format("Failed to insert entity with name: %s", entity);
        assertEquals(assertMessage, OK.getStatusCode(), response.getStatus());
        entity.setName(expectedName);
        assertEntityExisting(entity);
    }

    private String formatISODate(Date isoDate, String pattern) {
        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        return df.format(isoDate);
    }
}
