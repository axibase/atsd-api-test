package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Filter;
import com.axibase.tsd.api.util.Filters;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import jersey.repackaged.com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryTagExpressionFilterTest extends SeriesMethod {
    private static String TEST_ENTITY = entity();
    private static String TEST_METRIC = metric();
    private static final String[] TEST_TAGS = { null, "value1", "value2", "VALUE1", "VALUE2", "otherValue" };

    //TODO pending fix in #3915
    private final Filter[] filters = new Filter[] {
//            new Filter("tags.tag LIKE '*'",                 new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("lower(tags.tag) LIKE '*'",          new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("tags.tag NOT LIKE '*'",             new String[] {}),
//            new Filter("lower(tags.tag) NOT LIKE '*'",      new String[] {}),

//            new Filter("tags.tag LIKE '*al*'",              new String[] {"value1", "value2", "otherValue"}),
//            new Filter("lower(tags.tag) LIKE '*al*'",       new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("tags.tag NOT LIKE '*al*'",          new String[] {"VALUE1", "VALUE2"}),
//            new Filter("lower(tags.tag) NOT LIKE '*al*'",   new String[] {}),

            new Filter<>("tags.tag LIKE 'value?'", "value1", "value2"),
//            new Filter("lower(tags.tag) LIKE 'value?'",     new String[] {"value1", "value2", "VALUE1", "VALUE2"}),
//            new Filter("tags.tag NOT LIKE 'value?'",        new String[] {"VALUE1", "VALUE2", "otherValue"}),
//            new Filter("lower(tags.tag) NOT LIKE 'value?'", new String[] {"otherValue"}),

            new Filter<>("tags.tag = 'value1'", "value1"),
            new Filter<>("lower(tags.tag) = 'value1'", "value1", "VALUE1"),
//            new Filter("NOT tags.tag = 'value1'",           new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("NOT lower(tags.tag) = 'value1'",    new String[] {"value2", "VALUE2", "otherValue"}),

//            new Filter("tags.tag != 'value1'",              new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("lower(tags.tag) != 'value1'",       new String[] {"value2", "VALUE2", "otherValue"}),
//            new Filter("NOT tags.tag != 'value1'",          new String[] {"value1"}),
//            new Filter("NOT lower(tags.tag) != 'value1'",   new String[] {"value1", "VALUE1"}),

            new Filter<>("tags.tag >= 'VALUE2'", "value1", "value2", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) >= 'value1'", "value1", "value2", "VALUE1", "VALUE2"),
//            new Filter("NOT tags.tag >= 'VALUE2'",          new String[] {"VALUE1"}),
//            new Filter("NOT lower(tags.tag) >= 'value1'",   new String[] {"otherValue"}),

            new Filter<>("tags.tag >= 'VALUE2'", "value1", "value2", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) >= 'value1'", "value1", "value2", "VALUE1", "VALUE2"),
//            new Filter("NOT tags.tag >= 'VALUE2'",          new String[] {"VALUE1"}),
//            new Filter("NOT lower(tags.tag) >= 'value1'",   new String[] {"otherValue"}),

            new Filter<>("tags.tag > 'VALUE2'", "value1", "value2", "otherValue"),
            new Filter<>("lower(tags.tag) > 'value1'", "value2", "VALUE2")
//            new Filter("NOT tags.tag > 'VALUE2'",           new String[] {"VALUE1", "VALUE2"}),
//            new Filter("NOT lower(tags.tag) > 'value1'",    new String[] {"value1", "VALUE1", "otherValue"}),

//            new Filter("tags.tag <= 'VALUE2'",              new String[] {"VALUE1", "VALUE2"}),
//            new Filter("lower(tags.tag) <= 'VALUE2'",       new String[] {"value1", "VALUE1"}),
//            new Filter("NOT tags.tag <= 'VALUE2'",          new String[] {"value1", "value2", "otherValue"}),
//            new Filter("NOT lower(tags.tag) <= 'value1'",   new String[] {"value2", "VALUE2"}),

//            new Filter("tags.tag < 'VALUE2'",               new String[] {"VALUE1"}),
//            new Filter("lower(tags.tag) < 'value1'",        new String[] {"otherValue"}),
//            new Filter("NOT tags.tag < 'VALUE2'",           new String[] {"value1", "value2", "VALUE2", "otherValue"}),
//            new Filter("NOT lower(tags.tag) < 'value1'",    new String[] {"value1", "value2", "VALUE1", "VALUE2"}),
    };

    @DataProvider
    Object[][] provideSingleTagFilters() {
        return Filters.formatForDataProvider(filters);
    }

    @DataProvider
    Object[][] provideDoubleTagFiltersAnd() {
        Collection<Filter<String>> joinedFilters = Filters.crossProductAnd(filters, filters);
        return Filters.formatForDataProvider(joinedFilters);
    }

    @DataProvider
    Object[][] provideDoubleTagFiltersOr() {
        Collection<Filter<String>> joinedFilters = Filters.crossProductOr(filters, filters);
        return Filters.formatForDataProvider(joinedFilters);
    }

    @BeforeClass
    public void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();

        for (String tagValues : TEST_TAGS) {
            if (tagValues == null) {
                Series series = new Series(TEST_ENTITY, TEST_METRIC);
                series.addSamples(Mocks.SAMPLE);
                seriesList.add(series);
                continue;
            }

            Series series = new Series(TEST_ENTITY, TEST_METRIC, Collections.singletonMap("tag", tagValues));
            series.addSamples(Mocks.SAMPLE);
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3915")
    @Test(dataProvider = "provideSingleTagFilters")
    public void testSingleTagFilters(Filter<String> filter) throws Exception {
        checkQuery(filter.getExpression(), filter.getExpectedResultSet());
    }

    @Issue("3915")
    @Test(dataProvider = "provideSingleTagFilters")
    public void testTagFilterWithTagExpression(Filter<String> filter) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        Set<String> expectedTagsSet = new HashSet<>();
        if (filter.getExpectedResultSet().size() > 0) {
            Optional<String> firstValue = filter.getExpectedResultSet().stream().findFirst();
            if (firstValue.isPresent()) {
                expectedTagsSet.add(firstValue.get());
                query.setTags(Collections.singletonMap("tag", firstValue.get()));
            }
        } else {
            query.setTags(Collections.singletonMap("tag", "value1"));
        }
        query.setTagExpression(filter.getExpression());
        Set<String> actualTagsSet = executeTagsQuery(query);

        assertEquals(actualTagsSet, expectedTagsSet);
    }

    @Issue("3915")
    @Test(dataProvider = "provideSingleTagFilters")
    public void testTagFilterWithSeriesLimit(Filter<String> filter) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter.getExpression());
        int expectedCount;
        Set<String> expectedResultSet = filter.getExpectedResultSet();
        if (expectedResultSet.size() > 1) {
            query.setSeriesLimit(expectedResultSet.size() - 1);
            expectedCount = expectedResultSet.size() - 1;
        } else {
            query.setSeriesLimit(1);
            expectedCount = expectedResultSet.size();
        }

        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals(seriesList.size(), expectedCount);
    }

    //TODO pending #3915
    @Issue("3915")
    @Test(dataProvider = "provideDoubleTagFiltersAnd", enabled = false)
    public void testDoubleTagFiltersAnd(Filter<String> filter) throws Exception {
        String complexExpression = String.format("(%1$s) OR (%1$s)", filter.getExpression());
        checkQuery(complexExpression, filter.getExpectedResultSet());
    }

    @Issue("3915")
    @Test(dataProvider = "provideDoubleTagFiltersOr")
    public void testDoubleTagFiltersOr(Filter<String> filter) throws Exception {
        String complexExpression = String.format("(%1$s) AND (%1$s)", filter.getExpression());
        checkQuery(complexExpression, filter.getExpectedResultSet());
    }

    @Issue("3915")
    @Test
    public void testTagExpressionFindsNotOnlyLastWrittenSeriesForEntity() throws Exception {
        // Arrange
        String metric = metric();
        String entity = entity();

        Series series1 = new Series(entity, metric, "key", "val1");
        series1.addSamples(Sample.ofDateInteger("2017-03-27T00:00:00.000Z", 1));

        Series series2 = new Series(entity, metric, "key", "val2");
        series2.addSamples(Sample.ofDateInteger("2017-03-27T00:00:01.000Z", 1));

        Series series3 = new Series(entity, metric, "key", "val3");
        series3.addSamples(Sample.ofDateInteger("2017-03-27T00:00:02.000Z", 1));

        insertSeriesCheck(series1, series2, series3);

        // Action
        SeriesQuery query = new SeriesQuery();
        query.setMetric(metric);
        query.setEntity(entity);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        query.setTagExpression("tags.key = 'val2'");

        List<Series> list = executeQueryReturnSeries(query);

        // Assert
        Assert.assertEquals(list, Collections.singletonList(series2), "Series are not matched to tag expression '"+ query.getTagExpression()+"'");
    }

    private void checkQuery(String filter, Set<String> expectedResult) throws Exception {
        Set<Object> expectedTagsSet = Sets.newHashSet(expectedResult);

        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter);
        Set<String> actualTagsSet = executeTagsQuery(query);

        assertEquals(String.format("Incorrect result with filter %s", filter), expectedTagsSet, actualTagsSet);
    }

    private Set<String> executeTagsQuery(SeriesQuery query) throws Exception {
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(query);
        Set<String> actualTagsSet = new HashSet<>();
        for (Series series : seriesList) {
            Map<String, String> tags = series.getTags();
            if (tags == null || tags.size() == 0) {
                actualTagsSet.add("null");
                continue;
            }

            String value = tags.get("tag");
            if (value == null) {
                actualTagsSet.add("null");
                continue;
            }

            actualTagsSet.add(value);
        }

        return actualTagsSet;
    }
}
