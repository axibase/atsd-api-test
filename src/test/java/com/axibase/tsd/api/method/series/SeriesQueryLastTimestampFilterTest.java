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
import java.util.function.IntPredicate;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;

/**
 * Checks implements of last timestamp filter by content of output series.
 * For each identical assertion individual dataProvider is create.
 * Method generateData() create input series.
 */

public class SeriesQueryLastTimestampFilterTest extends SeriesMethod {
    enum Compare {
        LESS_OR_EQUAL(result -> result <= 0),
        GREATER(result -> result > 0);

        private final IntPredicate matcher;

        Compare(IntPredicate matcher) {
            this.matcher = matcher;
        }

        public boolean compare(String a) {
            final int result = a.compareTo(QUERY_END_DATE);
            return matcher.test(result);
        }
    }

    /**
     * Parameters of compilation of series
     */
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String QUERY_START_DATE = "2019-01-01T02:00:00Z";
    private static final String QUERY_END_DATE = "2019-01-02T00:00:00Z";
    private static final int SERIES_VALUE = 101;
    private static final int SECONDS_IN_HALF_MINUTE = 30;
    private static final int TOTAL_SAMPLES = 3000;
    private static final String ZONE_ID = Mocks.TIMEZONE_ID;
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC = Mocks.metric();

    private static final SeriesQuery QUERY = new SeriesQuery(QUERY_ENTITY, METRIC, QUERY_START_DATE, QUERY_END_DATE);

    /**
     * Set of possible variants of date and list of pairs of dates
     */
    private static final String[] DATES = {"2018-12-31T22:00:00Z", "2019-01-01T12:00:00Z", "2019-01-02T02:00:00Z", QUERY_START_DATE, "2019-01-01T23:59:30Z"};
    private static final List<String[]> DATE_PAIRS = createDatePermutations(DATES);

    @BeforeClass
    private void prepareData() throws Exception {
        String entity = Mocks.entity();

        Series series = new Series(entity, METRIC);
        for (int i = 0; i < TOTAL_SAMPLES; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.SECOND, SECONDS_IN_HALF_MINUTE * i);
            Sample sample = Sample.ofDateInteger(time, SERIES_VALUE);
            series.addSamples(sample);
        }
        insertSeriesCheck(series);
    }

    @DataProvider(name = "min_and_max_count_data")
    public Object[][] minAndMaxDateCountData() {
        return DATE_PAIRS.toArray(new Object[0][]);
    }

    @DataProvider(name = "all_dates_data")
    public Object[][] allDatesData() {
        return TestUtil.convertTo2DimArray(DATES);
    }

    @DataProvider(name = "dates_match_range_data")
    public Object[][] datesMatchRangeData() {
        return filterDatesIsMatchRange();
    }

    @DataProvider(name = "dates_not_match_range_data")
    public Object[][] datesNotMatchRangeData() {
        return filterDatesIsNotMatchRange();
    }

    @DataProvider (name = "date_less_or_equals_last_data")
    public Object[][] dateLessOrEqualsLastData() {
        return filterDate(Compare.LESS_OR_EQUAL);
    }

    @DataProvider (name = "date_greater_last_data")
    public Object[][] dateGreaterLastData() {
        return filterDate(Compare.GREATER);
    }

    @Issue("6112")
    @Test(dataProvider = "dates_not_match_range_data", description = "Check that output series is empty if " +
            "last series timestamp doesn't belong to ['minInsertData', 'maxInsertData')")
    public void testMinMaxInsertDatesNotMatch(String minDate, String maxDate) {
        SeriesQuery query = QUERY
                .withMinInsertDate(minDate)
                .withMaxInsertDate(maxDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || (seriesList.size()==1 && seriesList.get(0).getData().isEmpty()), "Output series not empty");
    }

    @Issue("6112")
    @Test(dataProvider = "dates_match_range_data", description = "Check that output series is not empty if " +
            "last series timestamp belong to ['minInsertData', 'maxInsertData')")
    public void testMinMaxInsertDatesIsMatch(String minDate, String maxDate) {
        SeriesQuery query = QUERY
                .withMinInsertDate(minDate)
                .withMaxInsertDate(maxDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() == 1 && !seriesList.get(0).getData().isEmpty(), "One series expected");
    }


    @Issue("6112")
    @Test (dataProvider = "date_less_or_equals_last_data",
            description = "Checks that output series is not empty if last series timestamp equal or greater than minInsertDate')")
    public void testLastTimestampEqualOrGreaterMinInsertDate(String minDate) {
        SeriesQuery query = QUERY.withMinInsertDate(minDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() == 1 && !seriesList.get(0).getData().isEmpty(), "One series expected");
    }

    @Issue("6112")
    @Test (dataProvider = "date_greater_last_data",
            description = "Checks that output series is empty if last series timestamp less than minInsertDate")
    public void testLastTimestampLessMinInsertDate(String minDate) {
        SeriesQuery query = QUERY.withMinInsertDate(minDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || (seriesList.size()==1 && seriesList.get(0).getData().isEmpty()), "Output series not empty");
    }

    @Issue("6112")
    @Test (dataProvider = "date_greater_last_data",
            description = "Checks that output series is not empty if last series timestamp less than maxInsertDate")
    public void testLastTimestampLessMaxInsertDate(String maxDate) {
        SeriesQuery query = QUERY.withMaxInsertDate(maxDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.size() == 1 && !seriesList.get(0).getData().isEmpty(), "One series expected");
    }

    @Issue("6112")
    @Test (dataProvider = "date_less_or_equals_last_data",
            description = "Checks that output series is empty if last series timestamp equal or greater than maxInsertDate")
    public void testLastTimestampEqualOrGreaterMaxInsertDate(String maxDate) {
        SeriesQuery query = QUERY.withMaxInsertDate(maxDate);
        List<Series> seriesList = querySeriesAsList(query);
        assertTrue(seriesList.isEmpty() || seriesList.get(0).getData().isEmpty(), "Output series not empty");
    }

    private static List<String[]> createDatePermutations(String[] dates) {
        return Arrays.stream(dates)
                .flatMap(d1 -> Arrays.stream(dates).map(d2 -> new String[] {d1, d2}))
                .collect(toList());
    }

    private Object[][] filterDate(Compare operator) {
        return Arrays.stream(DATES)
                .filter(date -> operator.compare(date))
                .map(date -> new Object[]{date})
                .toArray(Object[][]::new);
    }

    private Object[][] filterDatesIsMatchRange() {
        return DATE_PAIRS.stream()
                .filter(date -> (Compare.LESS_OR_EQUAL.compare(date[0]) && Compare.GREATER.compare(date[1])))
                .toArray(Object[][]::new);
    }

    private Object[][] filterDatesIsNotMatchRange() {
        return DATE_PAIRS.stream()
                .filter(date -> (Compare.GREATER.compare(date[0]) || Compare.LESS_OR_EQUAL.compare(date[1])))
                .toArray(Object[][]::new);
    }
}
