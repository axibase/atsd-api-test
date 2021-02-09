package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class MetricSeriesDateLimitTest {
    private static final String METRIC_NAME = Mocks.metric();
    private static final String[] SAMPLE_DATES = {
            "2017-09-01T00:00:00.000Z",
            "2017-09-15T00:00:00.000Z",
            "2017-10-01T00:00:00.000Z"
    };

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        for (String date : SAMPLE_DATES) {
            Series series = new Series(Mocks.entity(), METRIC_NAME);
            series.addSamples(Sample.ofDate(date));
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    private static void assertLimitsPreserved(String minDate, String maxDate, String[] expectedDates) {
        MetricSeriesParameters params =
                MetricSeriesParameters.builder()
                        .minInsertDate(minDate)
                        .maxInsertDate(maxDate)
                        .build();
        MetricSeriesResponse[] responseMetrics;
        try {

            responseMetrics = MetricMethod.queryMetricSeriesResponse(METRIC_NAME, params)
                    .readEntity(MetricSeriesResponse[].class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot perform metric series query", e);
            return;
        }

        String[] actualDates = new String[responseMetrics.length];
        for (int i = 0; i < responseMetrics.length; i++) {
            actualDates[i] = responseMetrics[i].getLastInsertDate();
        }
        Arrays.sort(actualDates, Comparator.comparingLong(Util::getUnixTime));

        String errorMessage = String.format("Actual dates do not satisfy minInsertDate=%s maxInsertDate=%s limits",
                minDate, maxDate);
        assertEquals(expectedDates, actualDates, errorMessage);
    }

    @Issue("4581")
    @Test(
            description = "Test whether maxInsertDate applied, last date = maxInsertDate"
    )
    public void testMetricSeriesLimitEqualToLast() {
        String maxDate = "2017-10-01T00:00:00.000Z";
        String[] expectedDates = {
                "2017-09-01T00:00:00.000Z",
                "2017-09-15T00:00:00.000Z"
        };

        assertLimitsPreserved(null, maxDate, expectedDates);
    }

    @Issue("4581")
    @Test(
            description = "Test whether maxInsertDate applied, last date < maxInsertDate"
    )
    public void testMetricSeriesLimitGreaterThanLast() {
        String maxDate = "2017-10-01T00:00:00.001Z";
        String[] expectedDates = {
                "2017-09-01T00:00:00.000Z",
                "2017-09-15T00:00:00.000Z",
                "2017-10-01T00:00:00.000Z"
        };

        assertLimitsPreserved(null, maxDate, expectedDates);
    }

    @Issue("4581")
    @Test(
            description = "Test whether minInsertDate applied, first date = minInsertDate"
    )
    public void testMetricSeriesEqualToFirst() {
        String minDate = "2017-09-01T00:00:00.000Z";
        String[] expectedDates = {
                "2017-09-01T00:00:00.000Z",
                "2017-09-15T00:00:00.000Z",
                "2017-10-01T00:00:00.000Z"
        };

        assertLimitsPreserved(minDate, null, expectedDates);
    }

    @Issue("4581")
    @Test(
            description = "Test whether minInsertDate applied, first date < minInsertDate"
    )
    public void testMetricSeriesGreaterThanFirst() {
        String minDate = "2017-09-01T00:00:00.001Z";
        String[] expectedDates = {
                "2017-09-15T00:00:00.000Z",
                "2017-10-01T00:00:00.000Z"
        };

        assertLimitsPreserved(minDate, null, expectedDates);
    }

    @Issue("4581")
    @Test(
            description = "Test whether minInsertDate and maxInsertDate applied"
    )
    public void testMetricSeriesBothLimits() {
        String minDate = "2017-09-15T00:00:00.000Z";
        String maxDate = "2017-09-15T00:00:00.001Z";
        String[] expectedDates = {
                "2017-09-15T00:00:00.000Z"
        };

        assertLimitsPreserved(minDate, maxDate, expectedDates);
    }
}
