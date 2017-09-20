package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import jersey.repackaged.com.google.common.collect.Sets;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
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
    private final Object[][] filters = new Object[][] {
            //{"tags.tag LIKE '*'",                 new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}},
            //{"LOWER(tags.tag) LIKE '*'",          new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}},
            //{"tags.tag NOT LIKE '*'",             new String[] {}},
            //{"LOWER(tags.tag) NOT LIKE '*'",      new String[] {}},
            {"tags.tag LIKE '*al*'",                new String[] {"value1", "value2", "otherValue"}},
            //{"LOWER(tags.tag) LIKE '*al*'",       new String[] {"value1", "value2", "VALUE1", "VALUE2", "otherValue"}},
            //{"tags.tag NOT LIKE '*al*'",          new String[] {"VALUE1", "VALUE2"}},
            //{"LOWER(tags.tag) NOT LIKE '*al*'",   new String[] {}},
            {"tags.tag LIKE 'value?'",              new String[] {"value1", "value2"}},
            //{"LOWER(tags.tag) LIKE 'value?'",     new String[] {"value1", "value2", "VALUE1", "VALUE2"}},
            //{"tags.tag NOT LIKE 'value?'",        new String[] {"VALUE1", "VALUE2", "otherValue"}},
            //{"lower(tags.tag) NOT LIKE 'value?'", new String[] {"otherValue"}},

            {"tags.tag = 'value1'",                 new String[] {"value1"}},
            //{"LOWER(tags.tag) = 'value1'",        new String[] {"value1", "VALUE1"}},
            //{"NOT tags.tag = 'value1'",           new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}},
            //{"NOT LOWER(tags.tag) = 'value1'",    new String[] {"value2", "VALUE2", "otherValue"}},
            //{"tags.tag != 'value1'",              new String[] {"value2", "VALUE1", "VALUE2", "otherValue"}},
            //{"LOWER(tags.tag) != 'value1'",       new String[] {"value2", "VALUE2", "otherValue"}},
            //{"NOT tags.tag != 'value1'",          new String[] {"value1"}},
            //{"NOT LOWER(tags.tag) != 'value1'",   new String[] {"value1", "VALUE1"}},
            {"tags.tag >= 'VALUE2'",                new String[] {"value1", "value2", "VALUE2", "otherValue"}},
            //{"LOWER(tags.tag) >= 'value1'",       new String[] {"value1", "value2", "VALUE1", "VALUE2"}},
            //{"NOT tags.tag >= 'VALUE2'",          new String[] {"VALUE1"}},
            //{"NOT LOWER(tags.tag) >= 'value1'",   new String[] {"otherValue"}},
            {"tags.tag > 'VALUE2'",                 new String[] {"value1", "value2", "otherValue"}},
            //{"LOWER(tags.tag) > 'value1'",        new String[] {"value2", "VALUE2"}},
            //{"NOT tags.tag > 'VALUE2'",           new String[] {"VALUE1", "VALUE2"}},
            //{"NOT LOWER(tags.tag) > 'value1'",    new String[] {"value1", "VALUE1", "otherValue"}},
            //{"tags.tag <= 'VALUE2'",              new String[] {"VALUE1", "VALUE2"}},
            //{"LOWER(tags.tag) <= 'VALUE2'",       new String[] {"value1", "VALUE1"}},
            //{"NOT tags.tag <= 'VALUE2'",          new String[] {"value1", "value2", "otherValue"}},
            //{"NOT LOWER(tags.tag) <= 'value1'",   new String[] {"value2", "VALUE2"}},
            //{"tags.tag < 'VALUE2'",               new String[] {"VALUE1"}},
            //{"LOWER(tags.tag) < 'value1'",        new String[] {"otherValue"}},
            //{"NOT tags.tag < 'VALUE2'",           new String[] {"value1", "value2", "VALUE2", "otherValue"}},
            //{"NOT LOWER(tags.tag) < 'value1'",    new String[] {"value1", "value2", "VALUE1", "VALUE2"}},


    };

    @DataProvider(name = "singleTagFiltersProvider")
    Object[][] provideSingleTagFilters() {
        return filters;
    }

    private static Object[][] createFiltersCrossJoin(Object[][] filters) {
        Object[][] result = new Object[filters.length * filters.length][4];
        int resultIndex = 0;
        for (Object[] firstFilterResult : filters) {
            for (Object[] secondFilterResult : filters) {
                result[resultIndex][0] = firstFilterResult[0];
                result[resultIndex][1] = firstFilterResult[1];
                result[resultIndex][2] = secondFilterResult[0];
                result[resultIndex][3] = secondFilterResult[1];

                resultIndex++;
            }
        }

        return result;
    }

    @DataProvider(name = "doubleTagFiltersProvider")
    Object[][] provideDoubleTagFilters() {
        return createFiltersCrossJoin(filters);
    }


    @BeforeTest
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
    public void testSingleTagFilters(String filter, String[] expectedTags) throws Exception {
        checkQuery(filter, expectedTags);
    }

    @Issue("3915")
    @Test(dataProvider = "singleTagFiltersProvider")
    public void testTagFilterWithTagExpression(String filter, String[] expectedTags) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        Set<String> expectedTagsSet = new HashSet<>();
        if (expectedTags.length > 0) {
            expectedTagsSet.add(expectedTags[0]);
            query.setTags(Collections.singletonMap("tag", expectedTags[0]));
        } else {
            query.setTags(Collections.singletonMap("tag", "value1"));
        }
        query.setTagExpression(filter);
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

        assertEquals(actualTagsSet, expectedTagsSet);
    }

    @Issue("3915")
    @Test(dataProvider = "singleTagFiltersProvider")
    public void testTagFilterWithSeriesLimit(String filter, String[] expectedTags) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter);
        int expectedCount;
        if (expectedTags.length > 1) {
            query.setSeriesLimit(expectedTags.length - 1);
            expectedCount = expectedTags.length - 1;
        } else {
            query.setSeriesLimit(1);
            expectedCount = expectedTags.length;
        }

        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(query);
        assertEquals(seriesList.size(), expectedCount);
    }

    @Test(dataProvider = "doubleTagFiltersProvider")
    public void testDoubleTagFiltersAnd(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) throws Exception {
        Set<String> firstResultSet = new HashSet<>();
        Collections.addAll(firstResultSet, firstResult);

        Set<String> finalResultSet = new TreeSet<>((o1, o2) -> {
            if (o1.equals("null") && o2.equals("null")) return 0;
            if (o1.equals("null")) return -1;
            if (o2.equals("null")) return 1;
            return o1.compareTo(o2);
        });

        for (String resultRow : secondResult) {
            if (!firstResultSet.contains(resultRow)) continue;
            finalResultSet.add(resultRow);
        }

        checkQuery(String.format("((%1$s) AND (%2$s)) OR ((%1$s) AND (%2$s))", firstFilter, secondFilter), finalResultSet.toArray());
    }

    @Test(dataProvider = "doubleTagFiltersProvider")
    public void testDoubleTagFiltersOr(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) throws Exception {
        Set<String> finalResultSet = new TreeSet<>((o1, o2) -> {
            if (o1.equals("null") && o2.equals("null")) return 0;
            if (o1.equals("null")) return -1;
            if (o2.equals("null")) return 1;
            return o1.compareTo(o2);
        });

        Collections.addAll(finalResultSet, firstResult);
        Collections.addAll(finalResultSet, secondResult);

        checkQuery(String.format("((%1$s) OR (%2$s)) AND ((%1$s) OR (%2$s))", firstFilter, secondFilter), finalResultSet.toArray());
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

    private void checkQuery(String filter, Object[] expectedTags) throws Exception {
        Set<Object> expectedTagsSet = Sets.newHashSet(expectedTags);

        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter);
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

        assertEquals(expectedTagsSet, actualTagsSet);
    }
}
