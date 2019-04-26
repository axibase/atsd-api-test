package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesMetaInfo;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.util.TestUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZoneId;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * Test group by entity and tags. Parameter groupByEntityAndTags (in test named "Parameter") is responsible for set of tags.
 * Check that response contains correct number of grouped series, that each of them grouped rightly,
 * and fields entity and tags for each series and for each field series in field group is correct.
 *
 * Object of class CheckedParameters contains pair of parameters entity and tags.
 *
 * Methods insertSeries() and addSamplesToSeries() create input series.
 *
 * Are used different queries for different status of parameter groupByEntityAndTags.
 */

public class SeriesQueryGroupByEntityAndTagsTest extends SeriesMethod {

    @Data
    @RequiredArgsConstructor
    private static class CheckedParameters {
        private final String entity;
        private final Map<String, String> tags;

        public CheckedParameters(String entity, String... tags ) {
            this.entity = entity;
            this.tags = TestUtil.createTags(tags);
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }

    /**
     * Series parameters
     */
    private static final String START_DATE = "2019-01-01T00:00:00Z";
    private static final String END_DATE = "2019-01-02T00:00:00Z";
    private static final long TOTAL_SAMPLES_COUNT = 24;
    private static final String ZONE_ID = "Asia/Kathmandu";
    private static final String TAG_NAME_1 = "tag-name-1";
    private static final String TAG_VALUE_1 = "tag-value-1";
    private static final String TAG_NAME_2 = "tag-name-2";
    private static final String TAG_VALUE_2 = "tag-value-2";
    private static final String QUERY_ENTITY = "*";
    private static final String METRIC = "test-grouping-by";
    private static final String ENTITY_1 = "entity-first";
    private static final String ENTITY_2 = "entity-second";

    /**
     * Queries for different parameter groupByEntityAndTags
     */
    private static final Period PERIOD = new Period(1, TimeUnit.HOUR, PeriodAlignment.START_TIME);
    private SeriesQuery queryWithParameterIsNull = new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD));
    private SeriesQuery queryWithParameterIsEmpty = new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD)
                    .setGroupByEntityAndTags(new ArrayList<>()));
    private SeriesQuery queryWithParameterIsFirstTag = new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD)
                    .setGroupByEntityAndTags(Arrays.asList(TAG_NAME_1)));
    private SeriesQuery queryWithParameterIsSecondTag = new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD)
                    .setGroupByEntityAndTags(Arrays.asList(TAG_NAME_2)));
    private SeriesQuery queryWithParameterIsListBothTags = new SeriesQuery(QUERY_ENTITY, METRIC, START_DATE, END_DATE)
            .setGroup(new Group()
                    .setType(GroupType.SUM)
                    .setPeriod(PERIOD)
                    .setGroupByEntityAndTags(Arrays.asList(TAG_NAME_1, TAG_NAME_2)));

    /**
     * Expected Data
     */
    private static final CheckedParameters SERIES_1_PARAMETERS = new CheckedParameters(ENTITY_1, new HashMap<>());
    private static final CheckedParameters SERIES_2_PARAMETERS = new CheckedParameters(ENTITY_1, TAG_NAME_1, TAG_VALUE_1);
    private static final CheckedParameters SERIES_3_PARAMETERS = new CheckedParameters(ENTITY_1, TAG_NAME_2, TAG_VALUE_2);
    private static final CheckedParameters SERIES_4_PARAMETERS = new CheckedParameters(ENTITY_1, TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2);
    private static final CheckedParameters SERIES_5_PARAMETERS = new CheckedParameters(ENTITY_1, TAG_NAME_1, TAG_VALUE_2);
    private static final CheckedParameters SERIES_6_PARAMETERS = new CheckedParameters(ENTITY_2, new HashMap<>());
    private static final CheckedParameters SERIES_7_PARAMETERS = new CheckedParameters(ENTITY_2, TAG_NAME_1, TAG_VALUE_1);

    /**
     *                                                 Series
     * ------------------------------------------------------------------------------------------------------------
     * SERIES   |   ENTITY      |                        TAGS AND VALUES                        | VALUE IN SAMPLE |
     * ------------------------------------------------------------------------------------------------------------
     * SERIES_1 |   ENTITY_1    |               |               |               |               |                1|
     * SERIES_2 |   ENTITY_1    |   TAG_NAME_1  |   TAG_VALUE_1 |               |               |               10|
     * SERIES_3 |   ENTITY_1    |   TAG_NAME_2  |   TAG_VALUE_2 |               |               |              100|
     * SERIES_4 |   ENTITY_1    |   TAG_NAME_1  |   TAG_VALUE_1 |   TAG_NAME_2  |   TAG_VALUE_2 |             1000|
     * SERIES_5 |   ENTITY_1    |   TAG_NAME_1  |   TAG_VALUE_2 |               |               |            10000|
     * SERIES_6 |   ENTITY_2    |               |               |               |               |           100000|
     * SERIES_7 |   ENTITY_2    |   TAG_NAME_1  |   TAG_VALUE_1 |               |               |          1000000|
     * ------------------------------------------------------------------------------------------------------------
     */

    @BeforeClass
    private void insertSeries() throws Exception {
        Series[] seriesArray = {
                new Series(ENTITY_1, METRIC),
                new Series(ENTITY_1, METRIC, TAG_NAME_1, TAG_VALUE_1),
                new Series(ENTITY_1, METRIC, TAG_NAME_2, TAG_VALUE_2),
                new Series(ENTITY_1, METRIC, TAG_NAME_1, TAG_VALUE_1, TAG_NAME_2, TAG_VALUE_2),
                new Series(ENTITY_1, METRIC, TAG_NAME_1, TAG_VALUE_2),
                new Series(ENTITY_2, METRIC),
                new Series(ENTITY_2, METRIC, TAG_NAME_1, TAG_VALUE_1)};
        addSamplesToSeries(seriesArray);
        insertSeriesCheck(seriesArray);
    }

    @Test(description = "Check number of grouped series when parameter 'groupByEntityAndTags' is null")
    public void testNumberOfSeriesWhereParameterIsNull() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsNull);
        assertEquals(seriesList.size(), 1, "Wrong series count");
    }

    @Test(description = "Check number of grouped series when parameter 'groupByEntityAndTags' is empty (not null)")
    public void testNumberOfSeriesWhereParameterIsEmpty() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsEmpty);
        assertEquals(seriesList.size(), 2, "Wrong series count");
    }

    @Test(description = "Check number of grouped series when parameter 'groupByEntityAndTags' adjusted by first tag")
    public void testNumberOfSeriesWhereParameterIsFirstTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsFirstTag);
        assertEquals(seriesList.size(), 5, "Wrong series count");
    }

    @Test(description = "Check number of grouped series when parameter 'groupByEntityAndTags' adjusted by second tag")
    public void testNumberOfSeriesWhereParameterIsSecondTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsSecondTag);
        assertEquals(seriesList.size(), 3, "Wrong series count");
    }

    @Test(description = "Check number of grouped series when parameter 'groupByEntityAndTags' adjusted by list of tags")
    public void testNumberOfSeriesWhereParameterIsListBothTags() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsListBothTags);
        assertEquals(seriesList.size(), 7, "Wrong series count");
    }

    @Test(description = "Check the value in grouped series when parameter 'groupByEntityAndTags' is null")
    public void testValuesWhereParameterIsNull() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsNull);
        Set<Double> actualSetValuesOfGroupedSeries = getSeriesValuesFromResponse(seriesList);
        Set<Double> expectedSetValuesOfGroupedSeries = collectCheckedValues(1111111.0);

        assertEquals(actualSetValuesOfGroupedSeries, expectedSetValuesOfGroupedSeries,
                "Set of values of series not match expected");
    }

    @Test(description = "Check the value in grouped series when parameter 'groupByEntityAndTags' is empty (not null)")
    public void testValuesWhereParameterIsEmpty() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsEmpty);
        Set<Double> actualSetValuesOfGroupedSeries = getSeriesValuesFromResponse(seriesList);
        Set<Double> expectedSetValuesOfGroupedSeries = collectCheckedValues(11111.0, 1100000.0);

        assertEquals(actualSetValuesOfGroupedSeries, expectedSetValuesOfGroupedSeries,
                "Set of values of series not match expected");
    }

    @Test(description = "Check the value in grouped series when parameter 'groupByEntityAndTags' adjusted by first tag")
    public void testValuesWhereParameterIsFirstTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsFirstTag);
        Set<Double> actualSetValuesOfGroupedSeries = getSeriesValuesFromResponse(seriesList);
        Set<Double> expectedSetValuesOfGroupedSeries = collectCheckedValues(101.0, 1010.0, 10000.0, 100000.0, 1000000.0);

        assertEquals(actualSetValuesOfGroupedSeries, expectedSetValuesOfGroupedSeries,
                "Set of values of series not match expected");
    }

    @Test(description = "Check the value in grouped series when parameter 'groupByEntityAndTags' adjusted by second tag")
    public void testValuesWhereParameterIsSecondTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsSecondTag);
        Set<Double> actualSetValuesOfGroupedSeries = getSeriesValuesFromResponse(seriesList);
        Set<Double> expectedSetValuesOfGroupedSeries = collectCheckedValues(1100.0, 10011.0, 1100000.0);

        assertEquals(actualSetValuesOfGroupedSeries, expectedSetValuesOfGroupedSeries,
                "Set of values of series not match expected");
    }

    @Test(description = "Check the value in grouped series when parameter 'groupByEntityAndTags' adjusted by list of tags")
    public void testValuesWhereParameterIsListBothTags() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsListBothTags);
        Set<Double> actualSetValuesOfGroupedSeries = getSeriesValuesFromResponse(seriesList);
        Set<Double> expectedSetValuesOfGroupedSeries = collectCheckedValues(1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0, 1000000.0);

        assertEquals(actualSetValuesOfGroupedSeries, expectedSetValuesOfGroupedSeries,
                "Set of values of series not match expected");
    }

    @Test(description = "Check fields entity and tags in grouped series when parameter 'groupByEntityAndTags' is null")
    public void testEntityAndTagsWhereParameterIsNull() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsNull);
        Set<CheckedParameters> actualSetOfEntityAndTags = getEntityAndTags(seriesList);
        Set<CheckedParameters> expectedSetOfEntityAndTags = collectCheckedParameters(
                new CheckedParameters(QUERY_ENTITY, new HashMap<>()));

        assertEquals(actualSetOfEntityAndTags, expectedSetOfEntityAndTags,
                "Set of pairs 'entity-tags' for series not match expected");
    }

    @Test(description = "Check fields entity and tags in grouped series when parameter 'groupByEntityAndTags' is empty (not null)")
    public void testEntityAndTagsWhereParameterIsEmpty() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsEmpty);
        Set<CheckedParameters> actualSetOfEntityAndTags = getEntityAndTags(seriesList);
        Set<CheckedParameters> expectedSetOfEntityAndTags = collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_6_PARAMETERS);

        assertEquals(actualSetOfEntityAndTags, expectedSetOfEntityAndTags,
                "Set of pairs 'entity-tags' for series not match expected");
    }

    @Test(description = "Check fields entity and tags in grouped series when parameter 'groupByEntityAndTags' adjusted by first tag")
    public void testEntityAndTagsWhereParameterIsFirstTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsFirstTag);
        Set<CheckedParameters> actualSetOfEntityAndTags = getEntityAndTags(seriesList);
        Set<CheckedParameters> expectedSetOfEntityAndTags = collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_2_PARAMETERS,
                SERIES_5_PARAMETERS,
                SERIES_6_PARAMETERS,
                SERIES_7_PARAMETERS);

        assertEquals(actualSetOfEntityAndTags, expectedSetOfEntityAndTags,
                "Set of pairs 'entity-tags' for series not match expected");
    }

    @Test(description = "Check fields entity and tags in grouped series when parameter 'groupByEntityAndTags' adjusted by second tag")
    public void testEntityAndTagsWhereParameterIsSecondTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsSecondTag);
        Set<CheckedParameters> actualSetOfEntityAndTags = getEntityAndTags(seriesList);
        Set<CheckedParameters> expectedSetOfEntityAndTags = collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_3_PARAMETERS,
                SERIES_6_PARAMETERS);

        assertEquals(actualSetOfEntityAndTags, expectedSetOfEntityAndTags,
                "Set of pairs 'entity-tags' for series not match expected");
    }

    @Test(description = "Check fields entity and tags in grouped series when parameter 'groupByEntityAndTags' adjusted by list of tags")
    public void testEntityAndTagsWhereParameterIsListBothTags() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsListBothTags);
        Set<CheckedParameters> actualSetOfEntityAndTags = getEntityAndTags(seriesList);
        Set<CheckedParameters> expectedSetOfEntityAndTags = collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_2_PARAMETERS,
                SERIES_3_PARAMETERS,
                SERIES_4_PARAMETERS,
                SERIES_5_PARAMETERS,
                SERIES_6_PARAMETERS,
                SERIES_7_PARAMETERS);

        assertEquals(actualSetOfEntityAndTags, expectedSetOfEntityAndTags,
                "Set of pairs 'entity-tags' for series not match expected");
    }

    @Test(description = "Check fields entity and tags inside fields series in group." +
            "For each grouped series formed set of pairs of this parameters." +
            "Parameter 'groupByEntityAndTags' is null")
    public void testEntityAndTagsInGroupSeriesWhereParameterIsNull() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsNull);
        Set<Set<CheckedParameters>> actualSetOfEntityAndTagsInGroupSeries = getEntityAndTagsInGroupSeries(seriesList);

        Set<Set<CheckedParameters>> expectedSetOfEntityAndTagsInGroupSeries = new HashSet<>();
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_2_PARAMETERS,
                SERIES_3_PARAMETERS,
                SERIES_4_PARAMETERS,
                SERIES_5_PARAMETERS,
                SERIES_6_PARAMETERS,
                SERIES_7_PARAMETERS));

        assertEquals(actualSetOfEntityAndTagsInGroupSeries, expectedSetOfEntityAndTagsInGroupSeries,
                "Sets of pairs 'entity-tags' for each group not match expected");
    }

    @Test(description = "Check fields entity and tags inside fields series in group." +
            "For each grouped series formed set of pairs of this parameters." +
            "Parameter 'groupByEntityAndTags' is empty (not null)")
    public void testEntityAndTagsInGroupSeriesWhereParameterIsEmpty() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsEmpty);
        Set<Set<CheckedParameters>> actualSetOfEntityAndTagsInGroupSeries = getEntityAndTagsInGroupSeries(seriesList);

        Set<Set<CheckedParameters>> expectedSetOfEntityAndTagsInGroupSeries = new HashSet<>();
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_2_PARAMETERS,
                SERIES_3_PARAMETERS,
                SERIES_4_PARAMETERS,
                SERIES_5_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_6_PARAMETERS,
                SERIES_7_PARAMETERS));

        assertEquals(actualSetOfEntityAndTagsInGroupSeries, expectedSetOfEntityAndTagsInGroupSeries,
                "Sets of pairs 'entity-tags' for each group not match expected");
    }

    @Test(description = "Check fields entity and tags inside fields series in group." +
            "For each grouped series formed set of pairs of this parameters." +
            "Parameter 'groupByEntityAndTags' adjusted by first tag")
    public void testEntityAndTagsInGroupSeriesWhereParameterIsFirstTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsFirstTag);
        Set<Set<CheckedParameters>> actualSetOfEntityAndTagsInGroupSeries = getEntityAndTagsInGroupSeries(seriesList);

        Set<Set<CheckedParameters>> expectedSetOfEntityAndTagsInGroupSeries = new HashSet<>();
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_3_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_2_PARAMETERS,
                SERIES_4_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_5_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_6_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_7_PARAMETERS));

        assertEquals(actualSetOfEntityAndTagsInGroupSeries, expectedSetOfEntityAndTagsInGroupSeries,
                "Sets of pairs 'entity-tags' for each group not match expected");
    }

    @Test(description = "Check fields entity and tags inside fields series in group." +
            "For each grouped series formed set of pairs of this parameters." +
            "Parameter 'groupByEntityAndTags' adjusted by second tag")
    public void testEntityAndTagsInGroupSeriesWhereParameterIsSecondTag() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsSecondTag);
        Set<Set<CheckedParameters>> actualSetOfEntityAndTagsInGroupSeries = getEntityAndTagsInGroupSeries(seriesList);

        Set<Set<CheckedParameters>> expectedSetOfEntityAndTagsInGroupSeries = new HashSet<>();
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_1_PARAMETERS,
                SERIES_2_PARAMETERS,
                SERIES_5_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_3_PARAMETERS,
                SERIES_4_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(
                SERIES_6_PARAMETERS,
                SERIES_7_PARAMETERS));

        assertEquals(actualSetOfEntityAndTagsInGroupSeries, expectedSetOfEntityAndTagsInGroupSeries,
                "Sets of pairs 'entity-tags' for each group not match expected");
    }

    @Test(description = "Check fields entity and tags inside fields series in group." +
            "For each grouped series formed set of pairs of this parameters." +
            "Parameter 'groupByEntityAndTags' adjusted by list of tags")
    public void testEntityAndTagsInGroupSeriesWhereParameterIsListBothTags() {
        List<Series> seriesList = querySeriesAsList(queryWithParameterIsListBothTags);
        Set<Set<CheckedParameters>> actualSetOfEntityAndTagsInGroupSeries = getEntityAndTagsInGroupSeries(seriesList);

        Set<Set<CheckedParameters>> expectedSetOfEntityAndTagsInGroupSeries = new HashSet<>();
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_1_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_2_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_3_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_4_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_5_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_6_PARAMETERS));
        expectedSetOfEntityAndTagsInGroupSeries.add(collectCheckedParameters(SERIES_7_PARAMETERS));

        assertEquals(actualSetOfEntityAndTagsInGroupSeries, expectedSetOfEntityAndTagsInGroupSeries,
                "Sets of pairs 'entity-tags' for each group not match expected");
    }

    private void addSamplesToSeries(Series... seriesArray) {
        for (int i = 0; i < TOTAL_SAMPLES_COUNT; i++) {
            String time = TestUtil.addTimeUnitsInTimezone(START_DATE, ZoneId.of(ZONE_ID), TimeUnit.HOUR, i);
            int seriesValue = 1;
            for(Series series: seriesArray) {
                Sample sample = Sample.ofDateInteger(time, seriesValue);
                series.addSamples(sample);
                seriesValue*=10;
            }
        }
    }

    private Set<Double> getSeriesValuesFromResponse(List<Series> seriesList) {
        Set<Double> setValuesOfGroupedSeries = new HashSet<>();
        for (Series series: seriesList) {
            setValuesOfGroupedSeries.add(series.getData().get(0).getValue().doubleValue());
        }

        return setValuesOfGroupedSeries;
    }

    private Set<Double> collectCheckedValues(Double... arrayValues) {
        return new HashSet<>(Arrays.asList(arrayValues));
    }

    private Set<CheckedParameters> getEntityAndTags(List<Series> seriesList) {
        Set<CheckedParameters> setMetaData = new HashSet<>();
        for (Series series: seriesList) {
            setMetaData.add(new CheckedParameters(series.getEntity(), series.getTags()));
        }

        return setMetaData;
    }

    private Set<CheckedParameters> collectCheckedParameters(CheckedParameters... arrayCheckedParameters) {
        return new HashSet<>(Arrays.asList(arrayCheckedParameters));
    }

    private Set<Set<CheckedParameters>> getEntityAndTagsInGroupSeries(List<Series> seriesList) {
        Set<Set<CheckedParameters>> setGroupSeriesData = new HashSet<>();
        for (Series series: seriesList) {
            Set<CheckedParameters> setOfOneSeries = new HashSet<>();
            for (SeriesMetaInfo seriesMetaInfo: series.getGroup().getSeries()) {
                setOfOneSeries.add(new CheckedParameters(seriesMetaInfo.getEntity(), seriesMetaInfo.getTags()));
            }
            setGroupSeriesData.add(setOfOneSeries);
        }

        return setGroupSeriesData;
    }
}
