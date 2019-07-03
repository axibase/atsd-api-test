package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Filter;
import com.axibase.tsd.api.util.Filters;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.testng.AssertJUnit.assertEquals;

public class SqlMerticTagsTest extends SqlTest {
    private final String[] TEST_METRICS = new String[] { metric(), metric(), metric(), metric(), metric() };

    private final String[] tagValues = new String[]{null, "VALUE1", "otherValue", "value1", "value2", "value3"};

    private final List<Filter<String>> isNullFilterResults = Arrays.asList(
            new Filter<>("tags.tag IS NULL", "null"),
            new Filter<>("tags.tag IS NOT NULL", "VALUE1", "otherValue", "value1", "value2", "value3"),
            new Filter<>("ISNULL(tags.tag, 'null') = 'null'", "null"),
            new Filter<>("NOT ISNULL(tags.tag, 'null') = 'null'", "VALUE1", "otherValue", "value1", "value2", "value3")
    );

    private final List<Filter<String>> matchFunctionsFilterResults = Arrays.asList(
            new Filter<>("tags.tag LIKE 'value_'", "value1", "value2", "value3"),
            new Filter<>("tags.tag NOT LIKE 'value_'", "VALUE1", "otherValue"),
            new Filter<>("tags.tag LIKE '%2'", "value2"),
            new Filter<>("tags.tag NOT LIKE '%2'", "VALUE1", "otherValue", "value1", "value3"),
            new Filter<>("tags.tag IN ('VALUE1', 'value2')", "VALUE1", "value2"),
            new Filter<>("tags.tag NOT IN ('VALUE1', 'value2')", "otherValue", "value1", "value3"),
            new Filter<>("tags.tag REGEX 'value[1,2]{1}|.*Value'", "otherValue", "value1", "value2"),
            new Filter<>("tags.tag NOT REGEX 'value[1,2]{1}|.*Value'", "VALUE1", "value3")
    );

    private final List<Filter<String>> mathFilterResultsGroup1 = Arrays.asList(
            new Filter<>("ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "VALUE1", "value1"),
            new Filter<>("NOT ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "value2", "value3"),
            new Filter<>("CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "VALUE1", "value1"),
            new Filter<>("NOT CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "value2", "value3"),
            new Filter<>("FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "VALUE1", "value1"),
            new Filter<>("NOT FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "value2", "value3")
    );

    private final List<Filter<String>> mathFilterResultsGroup2 = Arrays.asList(
            new Filter<>("ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1", "VALUE1", "value1"),
            new Filter<>("NOT ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1", "value2", "value3"),
            new Filter<>("MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0", "value2"),
            new Filter<>("NOT MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0", "VALUE1", "value1", "value3"),
            new Filter<>("CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3", "VALUE1", "value1"),
            new Filter<>("NOT CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3", "value2", "value3"),
            new Filter<>("FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1", "value3"),
            new Filter<>("NOT FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1", "VALUE1", "value1", "value2")
    );

    private final List<Filter<String>> mathFilterResultsGroup3 = Arrays.asList(
            new Filter<>("POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4", "value2"),
            new Filter<>("NOT POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4", "VALUE1", "value1", "value3"),
            new Filter<>("LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3", "value2"),
            new Filter<>("NOT LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3", "VALUE1", "value1", "value3"),
            new Filter<>("SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "VALUE1", "value1"),
            new Filter<>("NOT SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", "value2", "value3")
    );

    private final List<Filter<String>> stringFunctionsFilterResultsGroup1 = Arrays.asList(
            new Filter<>("UPPER(tags.tag) = 'VALUE1'", "VALUE1", "value1"),
            new Filter<>("NOT UPPER(tags.tag) = 'VALUE1'", "otherValue", "value2", "value3"),
            new Filter<>("LOWER(tags.tag) = 'value1'", "VALUE1", "value1"),
            new Filter<>("NOT LOWER(tags.tag) = 'value1'", "otherValue", "value2", "value3"),
            new Filter<>("REPLACE(tags.tag, 'other', 'new') = 'newValue'", "otherValue"),
            new Filter<>("NOT REPLACE(tags.tag, 'other', 'new') = 'newValue'", "VALUE1", "value1", "value2", "value3"),
            new Filter<>("LENGTH(tags.tag) = 6", "VALUE1", "value1", "value2", "value3"),
            new Filter<>("NOT LENGTH(tags.tag) = 6", "null", "otherValue")
    );

    private final List<Filter<String>> stringFunctionsFilterResultsGroup2 = Arrays.asList(
            new Filter<>("CONCAT(tags.tag, '1', '2') = 'value312'", "value3"),
            new Filter<>("NOT CONCAT(tags.tag, '1', '2') = 'value312'", "null", "VALUE1", "otherValue", "value1", "value2"),
            new Filter<>("SUBSTR(tags.tag, 3, 2) = 'lu'", "value1", "value2", "value3"),
            new Filter<>("NOT SUBSTR(tags.tag, 3, 2) = 'lu'", "VALUE1", "otherValue"),
            new Filter<>("CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1", "VALUE1", "value1"),
            new Filter<>("NOT CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1", "value2", "value3")
    );

    private final List<Filter<String>> dateFunctionsFilterResults = Arrays.asList(
            new Filter<>("date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    "VALUE1", "value1"),
            new Filter<>("NOT date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    "value2", "value3"),
            new Filter<>("date_parse(CONCAT('1970-01-01 00:00:0', " +
                    "ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), " +
                    "'yyyy-MM-dd HH:mm:ssZ') = 1000", "VALUE1", "value1"),
            new Filter<>("NOT date_parse(CONCAT('1970-01-01 00:00:0', " +
                    "ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), " +
                    "'yyyy-MM-dd HH:mm:ssZ') = 1000",
                    "null", "otherValue", "value2", "value3")
    );

    private final List<Filter<String>> comparisonFilterResults =Arrays.asList(
            new Filter<>("tags.tag = 'value1'", "value1"),
            new Filter<>("NOT tags.tag = 'value1'", "VALUE1", "otherValue", "value2", "value3"),
            new Filter<>("tags.tag != 'value1'", "VALUE1", "otherValue", "value2", "value3"),
            new Filter<>("NOT tags.tag != 'value1'", "value1"),
            new Filter<>("tags.tag > 'value1'", "value2", "value3"),
            new Filter<>("NOT tags.tag > 'value1'", "VALUE1", "otherValue", "value1"),
            new Filter<>("tags.tag >= 'value1'", "value1", "value2", "value3"),
            new Filter<>("NOT tags.tag >= 'value1'", "VALUE1", "otherValue"),
            new Filter<>("tags.tag < 'value1'", "VALUE1", "otherValue"),
            new Filter<>("NOT tags.tag < 'value1'", "value1", "value2", "value3"),
            new Filter<>("tags.tag <= 'value1'", "VALUE1", "otherValue", "value1"),
            new Filter<>("NOT tags.tag <= 'value1'", "value2", "value3")
    );

    @BeforeClass
    public void prepareData() throws Exception {
        String entity1 = entity();
        String entity2 = entity();

        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < tagValues.length; i++) {
            for (String metric : TEST_METRICS) {
                String tagValue = tagValues[i];
                Sample sample = Sample.ofDateInteger(String.format("2017-01-01T00:0%S:00Z", i), i);
                String entity = i % 2 == 0 ? entity1 : entity2;

                Series series = new Series(entity, metric);
                if (tagValue != null) {
                    series.addTag("tag", tagValue);
                }
                series.addSamples(sample);
                seriesList.add(series);
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("4180")
    @Test(description = "Test query without tag filter")
    public void testNoTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" ORDER BY tags.tag",
                TEST_METRICS[0]
        );

        String[][] expectedRows = {
                {"null"},
                {"VALUE1"},
                {"otherValue"},
                {"value1"},
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    @DataProvider
    public Object[][] provideAllSingleOperators() {
        List<Filter<String>> allFilters = new ArrayList<>();
        allFilters.addAll(isNullFilterResults);
        allFilters.addAll(matchFunctionsFilterResults);
        allFilters.addAll(mathFilterResultsGroup1);
        allFilters.addAll(mathFilterResultsGroup2);
        allFilters.addAll(mathFilterResultsGroup3);
        allFilters.addAll(stringFunctionsFilterResultsGroup1);
        allFilters.addAll(stringFunctionsFilterResultsGroup2);
        allFilters.addAll(dateFunctionsFilterResults);
        allFilters.addAll(comparisonFilterResults);

        return TestUtil.convertTo2DimArray(allFilters);
    }

    @Issue("4180")
    @Test(
            dataProvider = "provideAllSingleOperators",
            description = "Test single tag filter")
    public void testSingleTagFilter(Filter<String> filter) {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" WHERE %s ORDER BY tags.tag",
                TEST_METRICS[0],
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong query result using single tag filter: %s", filter),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    @SuppressWarnings("unchecked")
    @DataProvider
    public Object[][] provideDoubleOperatorsAnd() {
        List<Filter<String>> allFilters = new ArrayList<>();
        allFilters.addAll(Filters.crossProductAnd(isNullFilterResults, isNullFilterResults));
        allFilters.addAll(Filters.crossProductAnd(matchFunctionsFilterResults, matchFunctionsFilterResults));
        allFilters.addAll(Filters.crossProductAnd(mathFilterResultsGroup1, mathFilterResultsGroup1));
        allFilters.addAll(Filters.crossProductAnd(mathFilterResultsGroup2, mathFilterResultsGroup2));
        allFilters.addAll(Filters.crossProductAnd(mathFilterResultsGroup3, mathFilterResultsGroup3));
        allFilters.addAll(Filters.crossProductAnd(stringFunctionsFilterResultsGroup1, stringFunctionsFilterResultsGroup1));
        allFilters.addAll(Filters.crossProductAnd(stringFunctionsFilterResultsGroup2, stringFunctionsFilterResultsGroup2));
        allFilters.addAll(Filters.crossProductAnd(dateFunctionsFilterResults, dateFunctionsFilterResults));
        allFilters.addAll(Filters.crossProductAnd(comparisonFilterResults, comparisonFilterResults));

        return TestUtil.convertTo2DimArray(allFilters);
    }

    @SuppressWarnings("unchecked")
    @DataProvider
    public Object[][] provideDoubleOperatorsOr() {
        List<Filter<String>> allFilters = new ArrayList<>();
        allFilters.addAll(Filters.crossProductOr(isNullFilterResults, isNullFilterResults));
        allFilters.addAll(Filters.crossProductOr(matchFunctionsFilterResults, matchFunctionsFilterResults));
        allFilters.addAll(Filters.crossProductOr(mathFilterResultsGroup1, mathFilterResultsGroup1));
        allFilters.addAll(Filters.crossProductOr(mathFilterResultsGroup2, mathFilterResultsGroup2));
        allFilters.addAll(Filters.crossProductOr(mathFilterResultsGroup3, mathFilterResultsGroup3));
        allFilters.addAll(Filters.crossProductOr(stringFunctionsFilterResultsGroup1, stringFunctionsFilterResultsGroup1));
        allFilters.addAll(Filters.crossProductOr(stringFunctionsFilterResultsGroup2, stringFunctionsFilterResultsGroup2));
        allFilters.addAll(Filters.crossProductOr(dateFunctionsFilterResults, dateFunctionsFilterResults));
        allFilters.addAll(Filters.crossProductOr(comparisonFilterResults, comparisonFilterResults));

        return TestUtil.convertTo2DimArray(allFilters);
    }

    @Issue("4180")
    @Test(
            dataProvider = "provideDoubleOperatorsAnd",
            description = "Test all filters connected with complex filter equals to AND. (A AND B) OR (A AND B) = A AND B." +
                    "Each filter will be tested with all other filters in his group, i.e. cross-product." +
                    "Expected result is a intersection of first and second filter expected tags")
    public void testDoubleTagFiltersAnd(Filter<String> filter) {
        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%1$s\" WHERE (%2$s) OR (%2$s)",
                TEST_METRICS[0],
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    @Issue("4180")
    @Test(
            dataProvider = "provideDoubleOperatorsOr",
            description = "Test all filters connected with complex filter equals to OR. (A OR B) AND (A OR B) = A OR B." +
            "Each filter will be tested with all other filters in his group, i.e. cross-product." +
            "Expected result is a union of first and second filter expected tags")
    public void testDoubleTagFiltersOr(Filter filter) {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%1$s\" WHERE (%2$s) AND (%2$s)",
                TEST_METRICS[0],
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    //TODO pending fix in #4180
    @Issue("4180")
    @Test(
            dataProvider = "provideDoubleOperatorsAnd",
            description = "Test join with all filters connected with complex filter equals to AND. " +
                    "(A AND B) OR (A AND B) = A AND B." +
                    "Each filter will be tested with all other filters in his group, i.e. cross-product." +
                    "Expected result is a intersection of first and second filter expected tags",
            enabled = false)
    public void testDoubleTagFiltersJoinAnd(Filter filter) {
        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT m1.tags.tag " +
                "FROM \"%s\" m1 " +
                "JOIN \"%s\" m2 " +
                "JOIN USING ENTITY \"%s\" m3 " +
                "OUTER JOIN \"%s\" m4 " +
                "OUTER JOIN USING ENTITY \"%s\" m5 " +
                "WHERE (%s) OR (%s) OR (%s) " +
                "ORDER BY m1.tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                TEST_METRICS[2],
                TEST_METRICS[3],
                TEST_METRICS[4],
                filter.getExpression(),
                filter.getExpression(),
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    //TODO pending fix in #4180
    @Issue("4180")
    @Test(
            dataProvider = "provideDoubleOperatorsOr",
            description = "Test join all filters connected with complex filter equals to OR. " +
                    "(A OR B) AND (A OR B) = A OR B." +
                    "Each filter will be tested with all other filters in his group, i.e. cross-product." +
                    "Expected result is a union of first and second filter expected tags",
            enabled = false)
    public void testDoubleTagFiltersJoinOr(Filter filter) {
        // Logically similar to A OR B
        String sqlQuery = String.format(
                "SELECT m1.tags.tag " +
                "FROM \"%s\" m1 " +
                "JOIN \"%s\" m2 " +
                "JOIN USING ENTITY \"%s\" m3 " +
                "OUTER JOIN \"%s\" m4 " +
                "OUTER JOIN USING ENTITY \"%s\" m5 " +
                "WHERE (%s) AND (%s) AND (%s) " +
                "ORDER BY m1.tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                TEST_METRICS[2],
                TEST_METRICS[3],
                TEST_METRICS[4],
                filter.getExpression(),
                filter.getExpression(),
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    @Issue("4180")
    @Test(
            dataProvider = "provideDoubleOperatorsAnd",
            description = "Test all filters select from atsd_series connected with complex filter equals to AND. " +
                    "(A AND B) OR (A AND B) = A AND B." +
                    "Each filter will be tested with all other filters in his group, i.e. cross-product." +
                    "Expected result is a intersection of first and second filter expected tags")
    public void testDoubleTagFiltersAtsdSeriesAnd(Filter filter) {
        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                "FROM atsd_series " +
                "WHERE metric IN ('%1$s', '%2$s') AND ((%3$s) OR (%3$s)) " +
                "ORDER BY tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong atsd_series query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }

    @Issue("4180")
    @Test(dataProvider = "provideDoubleOperatorsOr",
            description = "Test all filters select from atsd_series connected with complex filter equals to OR. " +
                    "(A OR B) AND (A OR B) = A OR B." +
                    "Each filter will be tested with all other filters in his group, i.e. cross-product." +
                    "Expected result is a union of first and second filter expected tags")
    public void testDoubleTagFiltersAtsdSeriesOr(Filter filter) {
        // Logically similar to A OR B
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                "FROM atsd_series " +
                "WHERE metric IN ('%1$s', '%2$s') AND ((%3$s) AND (%3$s)) " +
                "ORDER BY tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                filter.getExpression()
        );

        StringTable table = queryTable(sqlQuery);

        assertEquals(
                String.format("Wrong atsd_series query result using double tag filters: %s", filter.getExpression()),
                Sets.newHashSet(table.columnValues("tags.tag")),
                filter.getExpectedResultSet());
    }
}
