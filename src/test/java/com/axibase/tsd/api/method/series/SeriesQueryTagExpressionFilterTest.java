package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.*;
import io.qameta.allure.Issue;
import org.apache.commons.collections4.CollectionUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryTagExpressionFilterTest extends SeriesMethod {
    private static final String TEST_ENTITY = entity();
    private static final String TEST_METRIC = metric();
    private static final String[] TEST_TAGS = { null, "value1", "value2", "VALUE1", "VALUE2", "otherValue" };

    private final Collection<Filter<String>> filters = Arrays.asList(
            new Filter<>("tags.tag LIKE '*'",
                    "null", "value1", "value2", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) LIKE '*'",
                    "null", "value1", "value2", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("tags.tag NOT LIKE '*'"),
            new Filter<>("lower(tags.tag) NOT LIKE '*'"),

            new Filter<>("tags.tag LIKE '*al*'",
                    "value1", "value2", "otherValue"),
            new Filter<>("lower(tags.tag) LIKE '*al*'",
                    "value1", "value2", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("tags.tag NOT LIKE '*al*'",
                    "null", "VALUE1", "VALUE2"),
            new Filter<>("lower(tags.tag) NOT LIKE '*al*'",
                    "null"),

            new Filter<>("tags.tag LIKE 'value?'",
                    "value1", "value2"),
            new Filter<>("lower(tags.tag) LIKE 'value?'",
                    "value1", "value2", "VALUE1", "VALUE2"),
            new Filter<>("tags.tag NOT LIKE 'value?'",
                    "null", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) NOT LIKE 'value?'",
                    "null", "otherValue"),

            new Filter<>("tags.tag = 'value1'",
                    "value1"),
            new Filter<>("lower(tags.tag) = 'value1'",
                    "value1", "VALUE1"),
            new Filter<>("NOT tags.tag = 'value1'",
                    "null", "value2", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("NOT lower(tags.tag) = 'value1'",
                    "null", "value2", "VALUE2", "otherValue"),

            new Filter<>("tags.tag != 'value1'",
                    "null", "value2", "VALUE1", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) != 'value1'",
                    "null", "value2", "VALUE2", "otherValue"),
            new Filter<>("NOT tags.tag != 'value1'",
                    "value1"),
            new Filter<>("NOT lower(tags.tag) != 'value1'",
                    "value1", "VALUE1"),

            new Filter<>("tags.tag >= 'VALUE2'",
                    "value1", "value2", "VALUE2", "otherValue"),
            new Filter<>("lower(tags.tag) >= 'value1'",
                    "value1", "value2", "VALUE1", "VALUE2"),
            new Filter<>("NOT tags.tag >= 'VALUE2'",
                    "null", "VALUE1"),
            new Filter<>("NOT lower(tags.tag) >= 'value1'",
                    "null", "otherValue"),

            new Filter<>("tags.tag > 'VALUE2'",
                    "value1", "value2", "otherValue"),
            new Filter<>("lower(tags.tag) > 'value1'",
                    "value2", "VALUE2"),
            new Filter<>("NOT tags.tag > 'VALUE2'",
                    "null", "VALUE1", "VALUE2"),
            new Filter<>("NOT lower(tags.tag) > 'value1'",
                    "null", "value1", "VALUE1", "otherValue"),

            new Filter<>("tags.tag <= 'VALUE2'",
                    "null", "VALUE1", "VALUE2"),
            new Filter<>("lower(tags.tag) <= 'VALUE2'",
                    "null"),
            new Filter<>("NOT tags.tag <= 'VALUE2'",
                    "value1", "value2", "otherValue"),
            new Filter<>("NOT lower(tags.tag) <= 'value1'",
                    "value2", "VALUE2"),

            new Filter<>("tags.tag < 'VALUE2'",
                    "null", "VALUE1"),
            new Filter<>("lower(tags.tag) < 'value1'",
                    "null", "otherValue"),
            new Filter<>("NOT tags.tag < 'VALUE2'",
                    "value1", "value2", "VALUE2", "otherValue"),
            new Filter<>("NOT lower(tags.tag) < 'value1'",
                    "value1", "value2", "VALUE1", "VALUE2"));

    @DataProvider
    Object[][] provideSingleTagFilters() {
        return TestUtil.convertTo2DimArray(filters);
    }

    @DataProvider
    Object[][] provideDoubleTagFiltersAnd() {
        Collection<Filter<String>> joinedFilters = Filters.selfCrossProductAnd(filters);
        return TestUtil.convertTo2DimArray(joinedFilters);
    }

    @DataProvider
    Object[][] provideDoubleTagFiltersOr() {
        Collection<Filter<String>> joinedFilters = Filters.selfCrossProductOr(filters);
        return TestUtil.convertTo2DimArray(joinedFilters);
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
    @Test(
            dataProvider = "provideSingleTagFilters",
            description = "test for single tag filter expression")
    public void testSingleTagFilters(Filter<String> filter) throws Exception {
        checkQuery(
                filter.getExpression(),
                filter.getExpectedResultSet(),
                "Incorrect result when using single tag filter");
    }

    @Issue("3915")
    @Test(
            dataProvider = "provideSingleTagFilters",
            description = "test tag expression with exact tag filter")
    public void testTagFilterWithTagExpression(Filter<String> filter) throws Exception {
        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        Set<String> expectedTagsSet = new HashSet<>();
        if (filter.getExpectedResultSet().size() > 0) {
            Optional<String> firstValue = filter.getExpectedResultSet().stream().findFirst();
            if (firstValue.isPresent()) {
                String tagValue = firstValue.get();
                if (!tagValue.equals("null")) {
                    expectedTagsSet.add(tagValue);
                }
                query.setTags(Collections.singletonMap("tag", firstValue.get()));
            }
        } else {
            query.setTags(Collections.singletonMap("tag", "value1"));
        }
        query.setTagExpression(filter.getExpression());
        Set<String> actualTagsSet = executeTagsQuery(query);

        assertEquals(
                String.format("Incorrect result when using exact tag filter with tag expression %s", filter.getExpression()),
                expectedTagsSet,
                actualTagsSet);
    }

    @Issue("3915")
    @Test(
            dataProvider = "provideSingleTagFilters",
            description = "test tag expression with limit")
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

        Set<String> tagsSet = executeTagsQuery(query);
        assertEquals(
                String.format("Incorrect result when using limit with tag expression %s", filter.getExpression()),
                expectedCount,
                tagsSet.size());
    }

    @Issue("3915")
    @Test(
            dataProvider = "provideDoubleTagFiltersAnd",
            description = "test complex tag expression")
    public void testDoubleTagFiltersAnd(Filter<String> filter) throws Exception {
        String complexExpression = String.format("(%1$s) OR (%1$s)", filter.getExpression());
        checkQuery(
                complexExpression,
                filter.getExpectedResultSet(),
                "Incorrect result with complex filter");
    }

    @Issue("3915")
    @Test(
            dataProvider = "provideDoubleTagFiltersOr",
            description = "test complex tag expression")
    public void testDoubleTagFiltersOr(Filter<String> filter) throws Exception {
        String complexExpression = String.format("(%1$s) AND (%1$s)", filter.getExpression());
        checkQuery(
                complexExpression,
                filter.getExpectedResultSet(),
                "Incorrect result with complex filter");
    }

    private void checkQuery(String filter, Set<String> expectedResult, String errorMessage) {
        Set<Object> expectedTagsSet = new HashSet<>(expectedResult);

        SeriesQuery query = new SeriesQuery(TEST_ENTITY, TEST_METRIC, Util.MIN_STORABLE_DATE, Util.MAX_STORABLE_DATE);
        query.setTagExpression(filter);
        Set<String> actualTagsSet = executeTagsQuery(query);

        assertEquals(
                String.format("%s filter %s", errorMessage, filter),
                expectedTagsSet,
                actualTagsSet);
    }

    private Set<String> executeTagsQuery(SeriesQuery query) {
        List<Series> seriesList = SeriesMethod.querySeriesAsList(query);
        Set<String> actualTagsSet = new HashSet<>();
        for (Series series : seriesList) {
            if (CollectionUtils.isEmpty(series.getData())) {
                continue;
            }

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
