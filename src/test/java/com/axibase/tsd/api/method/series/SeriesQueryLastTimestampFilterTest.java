package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Checks implements of last timestamp filter by content of output series.
 * For each identical assertion individual dataProvider is create.
 * Methods insertSeries() and addSamplesToSeries() create input series.
 */

public class SeriesQueryLastTimestampFilterTest extends SeriesMethod {
    /**
     * Parameters of compilation of series
     */
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String QUERY_START_DATE = "2019-01-01T02:00:00Z";
    private static final String QUERY_END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final int TOTAL_SAMPLES = 3000;
    private static final String ZONE_ID = "Asia/Kathmandu";
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC = "metric6112";

    private static final SeriesQuery QUERY = new SeriesQuery(QUERY_ENTITY, METRIC, QUERY_START_DATE, QUERY_END_DATE);

    /**
     * Set of possible variants of date
     */
    private static final String[] DATES = {"2018-12-31T22:00:00Z", "2019-01-01T12:00:00Z", "2019-01-02T02:00:00Z", QUERY_START_DATE, "2019-01-01T23:59:30Z"};

    @BeforeClass
    private void insertSeries() throws Exception {
        String entity = Mocks.entity();

        Series series = new Series(entity, METRIC);
        addSamplesToSeries(series);
        insertSeriesCheck(series);
    }

    @DataProvider(name = "min_and_max_count_data")
    public Object[][] minAndMaxDateCountData() {
        List<Object[]> result = new ArrayList<>();
        for (String minDate: DATES) {
            for (String maxDate: DATES) {
                result.add(new Object[] {minDate, maxDate});
            }
        }

        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "min_and_max_inside_range_data")
    public Object[][] minAndMaxInsideRangeData() {
        return generateDataMinMax(true);
    }

    @DataProvider(name = "min_and_max_outside_range_data")
    public Object[][] minAndMaxOutsideRangeData() {
        return generateDataMinMax(false);
    }

    @DataProvider(name = "all_dates_data")
    public Object[][] allDatesData() {
        return TestUtil.convertTo2DimArray(DATES);
    }

    @DataProvider (name = "min_date_inside_range_data")
    public Object[][] MinDateInsideRangeData() {
        return generateDataMin(true);
    }

    @DataProvider (name = "min_date_outside_range_data")
    public Object[][] MinDateOutsideRangeData() {
        return  generateDataMin(false);
    }

    @DataProvider (name = "max_date_inside_range_data")
    public Object[][] MaxDateInsideRangeData() {
        return generateDataMax(true);
    }

    @DataProvider (name = "max_date_outside_range_data")
    public Object[][] MaxDateOutsideRangeData() {
        return  generateDataMax(false);
    }

    @Issue("6112")
    @Test(groups = "count", dataProvider = "min_and_max_count_data", description = "Checks that response contain correct number of series")
    public void testCountSeriesMinMaxInsertDates(String minDate, String maxDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .maxInsertDate(maxDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() <= 1, "Wrong count of series");
    }

    @Issue("6112")
    @Test(dependsOnGroups = "count", dataProvider = "min_and_max_outside_range_data", description = "Check that output series is empty if " +
            "last series timestamp doesn't belong to ['minInsertData', 'maxInsertData')")
    public void testMinMaxInsertDatesNotMatch(String minDate, String maxDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .maxInsertDate(maxDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series not empty");
    }

    @Issue("6112")
    @Test(dependsOnGroups = "count", dataProvider = "min_and_max_inside_range_data", description = "Check that output series is not empty if " +
            "last series timestamp belong to ['minInsertData', 'maxInsertData')")
    public void testMinMaxInsertDatesIsMatch(String minDate, String maxDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .maxInsertDate(maxDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertFalse(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series is empty");
    }

    @Issue("6112")
    @Test (groups = "count", dataProvider = "all_dates_data", description = "Checks that response contain correct number of series")
    public void testCountSeriesMinInsertDate(String minDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() <= 1, "Wrong count of series");
    }

    @Issue("6112")
    @Test (groups = "count", dataProvider = "all_dates_data", description = "Checks that response contain correct number of series")
    public void testCountSeriesMaxInsertDate(String maxDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .maxInsertDate(maxDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() <= 1, "Wrong count of series");
    }

    @Issue("6112")
    @Test (dependsOnGroups = "count", dataProvider = "min_date_inside_range_data",
            description = "Checks that output series is not empty if last series timestamp equal or greater than minInsertDate')")
    public void testLastTimestampEqualOrGreaterMinInsertDate(String minDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertFalse(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series is empty");
    }

    @Issue("6112")
    @Test (dependsOnGroups = "count", dataProvider = "min_date_outside_range_data",
            description = "Checks that output series is empty if last series timestamp less than minInsertDate")
    public void testLastTimestampLessMinInsertDate(String minDate) {
        SeriesQuery query = QUERY
                .toBuilder()
                .minInsertDate(minDate)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series not empty");
    }

    @Issue("6112")
    @Test (dependsOnGroups = "count", dataProvider = "max_date_inside_range_data",
            description = "Checks that output series is not empty if last series timestamp less than maxInsertDate")
    public void testLastTimestampLessMaxInsertDate(String data) {
        SeriesQuery query = QUERY
                .toBuilder()
                .maxInsertDate(data)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertFalse(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series is empty");
    }

    @Issue("6112")
    @Test (dependsOnGroups = "count", dataProvider = "max_date_outside_range_data",
            description = "Checks that output series is empty if last series timestamp equal or greater than maxInsertDate")
    public void testLastTimestampEqualOrGreaterMaxInsertDate(String data) {
        SeriesQuery query = QUERY
                .toBuilder()
                .maxInsertDate(data)
                .build();
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series not empty");
    }

    private void addSamplesToSeries(Series series) {
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, SECONDS_IN_HALF_MINUTE * i);
            Sample sample = Sample.ofDateInteger(time, SERIES_VALUE);
            series.addSamples(sample);
        }
    }

    private Object[][] generateDataMinMax(boolean isMatch) {
        List<Object[]> allSet = Arrays.asList(minAndMaxDateCountData());
        List<Object[]> result = new ArrayList<>();
        for (Object[] minAndMaxDates: allSet) {
            if (isDatesMinMaxMatchLastTimestamp(minAndMaxDates) == isMatch){
                result.add(minAndMaxDates);
            }
        }

        return result.toArray(new Object[0][]);
    }

    private boolean isDatesMinMaxMatchLastTimestamp(Object[] minAndMaxDates) {
        long minDate = Util.getUnixTime((String) minAndMaxDates[0]);
        long maxDate = Util.getUnixTime((String) minAndMaxDates[1]);
        long endDate = Util.getUnixTime(QUERY_END_DATE);
        return (minDate < endDate && maxDate >= endDate);
    }

    private Object[][] generateDataMin(boolean isMatch) {
        List<Object> result = new ArrayList<>();
        for (String date: DATES) {
            if ((Util.getUnixTime(date) <= Util.getUnixTime(QUERY_END_DATE)) == isMatch) {
                result.add(date);
            }
        }
        return TestUtil.convertTo2DimArray(result.toArray());
    }

    private Object[][] generateDataMax(boolean isMatch) {
        List<Object> result = new ArrayList<>();
        for (String date: DATES) {
            if ((Util.getUnixTime(date) > Util.getUnixTime(QUERY_END_DATE)) == isMatch) {
                result.add(date);
            }
        }
        return TestUtil.convertTo2DimArray(result.toArray());
    }
}
