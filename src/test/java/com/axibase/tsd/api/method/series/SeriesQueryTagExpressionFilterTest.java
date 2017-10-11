package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
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

            new Filter("tags.tag LIKE 'value?'",            new String[] {"value1", "value2"}),
//            new Filter("lower(tags.tag) LIKE 'value?'",     new String[] {"value1", "value2", "VALUE1", "VALUE2"}),
//            new Filter("tags.tag NOT LIKE 'value?'",        new String[] {"VALUE1", "VALUE2", "otherValue"}),
//            new Filter("lower(tags.tag) NOT LIKE 'value?'", new String[] {"otherValue"}),

            new Filter("tags.tag = 'value1'",               new String[] {"value1"}),
            new Filter("lower(tags.tag) = 'value1'",        new String[] {"value1", "VALUE1"}),
//            new Filter("NOT tags.tag = 'value1'",           new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("NOT lower(tags.tag) = 'value1'",    new String[] {"value2", "VALUE2", "otherValue"}),

//            new Filter("tags.tag != 'value1'",              new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}),
//            new Filter("lower(tags.tag) != 'value1'",       new String[] {"value2", "VALUE2", "otherValue"}),
//            new Filter("NOT tags.tag != 'value1'",          new String[] {"value1"}),
//            new Filter("NOT lower(tags.tag) != 'value1'",   new String[] {"value1", "VALUE1"}),

            new Filter("tags.tag >= 'VALUE2'",              new String[] {"value1", "value2", "VALUE2", "otherValue"}),
            new Filter("lower(tags.tag) >= 'value1'",       new String[] {"value1", "value2", "VALUE1", "VALUE2"}),
//            new Filter("NOT tags.tag >= 'VALUE2'",          new String[] {"VALUE1"}),
//            new Filter("NOT lower(tags.tag) >= 'value1'",   new String[] {"otherValue"}),

            new Filter("tags.tag >= 'VALUE2'",              new String[] {"value1", "value2", "VALUE2", "otherValue"}),
            new Filter("lower(tags.tag) >= 'value1'",       new String[] {"value1", "value2", "VALUE1", "VALUE2"}),
//            new Filter("NOT tags.tag >= 'VALUE2'",          new String[] {"VALUE1"}),
//            new Filter("NOT lower(tags.tag) >= 'value1'",   new String[] {"otherValue"}),

            new Filter("tags.tag > 'VALUE2'",               new String[] {"value1", "value2", "otherValue"}),
            new Filter("lower(tags.tag) > 'value1'",        new String[] {"value2", "VALUE2"}),
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

    @DataProvider(name = "singleTagFiltersProvider", parallel = true)
    Object[][] provideSingleTagFilters() {
        Object[][] result = new Object[filters.length][1];
        for (int i = 0; i < filters.length; i++) {
            result[i][0] = filters[i];
        }

        return result;
    }

    private List<FilterTuple> createFiltersCrossJoin(Filter[] filters) {
        List<FilterTuple> result = new ArrayList<>(filters.length * filters.length);
        for (Filter firstFilter : filters) {
            for (Filter secondFilter : filters) {
                result.add(new FilterTuple(firstFilter, secondFilter));
            }
        }

        return result;
    }

    @DataProvider(name = "doubleTagFiltersProvider", parallel = true)
    Object[][] provideDoubleTagFilters() {
        List<FilterTuple> allTuples = createFiltersCrossJoin(filters);

        Object[][] result = new Object[allTuples.size()][1];
        for (int i = 0; i < allTuples.size(); i++) {
            result[i][0] = allTuples.get(i);
        }

        return result;
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
    @Test(dataProvider = "singleTagFiltersProvider")
    public void testSingleTagFilters(Filter filter) throws Exception {
        checkQuery(filter.expression, Sets.newHashSet(filter.expectedResult));
    }

    @Issue("3915")
    @Test(dataProvider = "singleTagFiltersProvider")
    public void testTagFilterWithTagExpression(Filter filter) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        Set<String> expectedTagsSet = new HashSet<>();
        if (filter.expectedResult.length > 0) {
            expectedTagsSet.add(filter.expectedResult[0]);
            query.setTags(Collections.singletonMap("tag", filter.expectedResult[0]));
        } else {
            query.setTags(Collections.singletonMap("tag", "value1"));
        }
        query.setTagExpression(filter.expression);
        Set<String> actualTagsSet = executeTagsQuery(query);

        assertEquals(actualTagsSet, expectedTagsSet);
    }

    @Issue("3915")
    @Test(dataProvider = "singleTagFiltersProvider")
    public void testTagFilterWithSeriesLimit(Filter filter) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter.expression);
        int expectedCount;
        if (filter.expectedResult.length > 1) {
            query.setSeriesLimit(filter.expectedResult.length - 1);
            expectedCount = filter.expectedResult.length - 1;
        } else {
            query.setSeriesLimit(1);
            expectedCount = filter.expectedResult.length;
        }

        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals(seriesList.size(), expectedCount);
    }

    //TODO pending #3915
    @Issue("3915")
    @Test(dataProvider = "doubleTagFiltersProvider", enabled = false)
    public void testDoubleTagFiltersAnd(FilterTuple filterTuple) throws Exception {
        Set<String> firstResultSet = new HashSet<>();
        Collections.addAll(firstResultSet, filterTuple.firstFilter.expectedResult);

        Set<String> finalResultSet = new TreeSet<>(SeriesQueryTagExpressionFilterTest::compareTags);

        for (String resultRow : filterTuple.secondFilter.expectedResult) {
            if (!firstResultSet.contains(resultRow)) continue;
            finalResultSet.add(resultRow);
        }

        checkQuery(String.format("((%1$s) AND (%2$s)) OR ((%1$s) AND (%2$s))",
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression),
                finalResultSet);
    }

    @Issue("3915")
    @Test(dataProvider = "doubleTagFiltersProvider")
    public void testDoubleTagFiltersOr(FilterTuple filterTuple) throws Exception {
        Set<String> finalResultSet = new TreeSet<>(SeriesQueryTagExpressionFilterTest::compareTags);

        Collections.addAll(finalResultSet, filterTuple.firstFilter.expectedResult);
        Collections.addAll(finalResultSet, filterTuple.secondFilter.expectedResult);

        checkQuery(String.format("((%1$s) OR (%2$s)) AND ((%1$s) OR (%2$s))",
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression),
                finalResultSet);
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

    private static int compareTags(String o1, String o2) {
        if (o1.equals("null") && o2.equals("null")) return 0;
        if (o1.equals("null")) return -1;
        if (o2.equals("null")) return 1;
        return o1.compareTo(o2);
    }

    private class Filter {
        public final String expression;
        public final String[] expectedResult;

        private Filter(String expression, String[] expectedResult) {
            this.expression = expression;
            this.expectedResult = expectedResult;
        }
    }

    private class FilterTuple {
        public final Filter firstFilter;
        public final Filter secondFilter;

        private FilterTuple(Filter firstFilter, Filter secondFilter) {
            this.firstFilter = firstFilter;
            this.secondFilter = secondFilter;
        }
    }
}
