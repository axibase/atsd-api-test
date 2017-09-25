package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlMerticTagsTest extends SqlTest {
    private final String[] TEST_METRICS = new String[] { metric(), metric(), metric(), metric(), metric() };

    private final String[] tagValues = new String[]{null, "VALUE1", "otherValue", "value1", "value2", "value3"};

    private final Filter[] isNullFilterResults = new Filter[]{
            new Filter("tags.tag IS NULL",
                    new String[]{"null"}),

            new Filter("tags.tag IS NOT NULL",
                    new String[]{"VALUE1", "otherValue", "value1", "value2", "value3"}),

            new Filter("ISNULL(tags.tag, 'null') = 'null'",
                    new String[]{"null"}),

            new Filter("NOT ISNULL(tags.tag, 'null') = 'null'",
                    new String[]{"VALUE1", "otherValue", "value1", "value2", "value3"})
    };

    private final Filter[] matchFunctionsFilterResults = new Filter[]{
            new Filter("tags.tag LIKE 'value_'",
                    new String[]{"value1", "value2", "value3"}),

            new Filter("tags.tag NOT LIKE 'value_'",
                    new String[]{"VALUE1", "otherValue"}),

            new Filter("tags.tag LIKE '%2'",
                    new String[]{"value2"}),

            new Filter("tags.tag NOT LIKE '%2'",
                    new String[]{"VALUE1", "otherValue", "value1", "value3"}),

            new Filter("tags.tag IN ('VALUE1', 'value2')",
                    new String[]{"VALUE1", "value2"}),

            new Filter("tags.tag NOT IN ('VALUE1', 'value2')",
                    new String[]{"otherValue", "value1", "value3"}),

            new Filter("tags.tag REGEX 'value[1,2]{1}|.*Value'",
                    new String[]{"otherValue", "value1", "value2"}),

            new Filter("tags.tag NOT REGEX 'value[1,2]{1}|.*Value'",
                    new String[]{"VALUE1", "value3"})
    };

    private final Filter[] mathFilterResultsGroup1 = new Filter[]{
            new Filter("ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"value2", "value3"}),

            new Filter("CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"value2", "value3"}),

            new Filter("FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"value2", "value3"})
    };

    private final Filter[] mathFilterResultsGroup2 = new Filter[]{
            new Filter("ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1",
                    new String[]{"value2", "value3"}),

            new Filter("MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0",
                    new String[]{"value2"}),

            new Filter("NOT MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0",
                    new String[]{"VALUE1", "value1", "value3"}),

            new Filter("CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3",
                    new String[]{"value2", "value3"}),

            new Filter("FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1",
                    new String[]{"value3"}),

            new Filter("NOT FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1",
                    new String[]{"VALUE1", "value1", "value2"})
    };

    private final Filter[] mathFilterResultsGroup3 = new Filter[]{
            new Filter("POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4",
                    new String[]{"value2"}),

            new Filter("NOT POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4",
                    new String[]{"VALUE1", "value1", "value3"}),

            new Filter("LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3",
                    new String[]{"value2"}),

            new Filter("NOT LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3",
                    new String[]{"VALUE1", "value1", "value3"}),

            new Filter("SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",
                    new String[]{"value2", "value3"})
    };

    private final Filter[] stringFunctionsFilterResultsGroup1 = new Filter[] {
            new Filter("UPPER(tags.tag) = 'VALUE1'",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT UPPER(tags.tag) = 'VALUE1'",
                    new String[]{"otherValue", "value2", "value3"}),

            new Filter("LOWER(tags.tag) = 'value1'",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT LOWER(tags.tag) = 'value1'",
                    new String[]{"otherValue", "value2", "value3"}),

            new Filter("REPLACE(tags.tag, 'other', 'new') = 'newValue'",
                    new String[]{"otherValue"}),

            new Filter("NOT REPLACE(tags.tag, 'other', 'new') = 'newValue'",
                    new String[]{"VALUE1", "value1", "value2", "value3"}),

            new Filter("LENGTH(tags.tag) = 6",
                    new String[]{"VALUE1", "value1", "value2", "value3"}),

            new Filter("NOT LENGTH(tags.tag) = 6",
                    new String[]{"null", "otherValue"})
    };

    private final Filter[] stringFunctionsFilterResultsGroup2 = new Filter[]{
            new Filter("CONCAT(tags.tag, '1', '2') = 'value312'",
                    new String[]{"value3"}),

            new Filter("NOT CONCAT(tags.tag, '1', '2') = 'value312'",
                    new String[]{"null", "VALUE1", "otherValue", "value1", "value2"}),

            new Filter("SUBSTR(tags.tag, 3, 2) = 'lu'",
                    new String[]{"value1", "value2", "value3"}),

            new Filter("NOT SUBSTR(tags.tag, 3, 2) = 'lu'",
                    new String[]{"VALUE1", "otherValue"}),

            new Filter("CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1",
                    new String[]{"value2", "value3"})
    };

    private final Filter[] dateFunctionsFilterResults = new Filter[] {
            new Filter("date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    new String[]{"value2", "value3"}),

            new Filter("date_parse(CONCAT('1970-01-01 00:00:0', " +
                    "ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), " +
                    "'yyyy-MM-dd HH:mm:ssZ') = 1000",
                    new String[]{"VALUE1", "value1"}),

            new Filter("NOT date_parse(CONCAT('1970-01-01 00:00:0', " +
                    "ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), " +
                    "'yyyy-MM-dd HH:mm:ssZ') = 1000",
                    new String[]{"null", "otherValue", "value2", "value3"})
    };

    private final Filter[] comparisonFilterResults = new Filter[] {
            new Filter("tags.tag = 'value1'",
                    new String[]{"value1"}),

            new Filter("NOT tags.tag = 'value1'",
                    new String[]{"VALUE1", "otherValue", "value2", "value3"}),

            new Filter("tags.tag != 'value1'",
                    new String[]{"VALUE1", "otherValue", "value2", "value3",}),

            new Filter("NOT tags.tag != 'value1'",
                    new String[]{"value1"}),

            new Filter("tags.tag > 'value1'",
                    new String[]{"value2", "value3"}),

            new Filter("NOT tags.tag > 'value1'",
                    new String[]{"VALUE1", "otherValue", "value1"}),

            new Filter("tags.tag >= 'value1'",
                    new String[]{"value1", "value2", "value3"}),

            new Filter("NOT tags.tag >= 'value1'",
                    new String[]{"VALUE1", "otherValue"}),

            new Filter("tags.tag < 'value1'",
                    new String[]{"VALUE1", "otherValue"}),

            new Filter("NOT tags.tag < 'value1'",
                    new String[]{"value1", "value2", "value3"}),

            new Filter("tags.tag <= 'value1'",
                    new String[]{"VALUE1", "otherValue", "value1"}),

            new Filter("NOT tags.tag <= 'value1'",
                    new String[]{"value2", "value3"})
    };

    private static int compareTags(String o1, String o2) {
        if (o1.equals("null") && o2.equals("null")) return 0;
        if (o1.equals("null")) return -1;
        if (o2.equals("null")) return 1;
        return o1.compareTo(o2);
    }

    @BeforeTest
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
    @Test
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

    @DataProvider(name = "singleOperatorsDataProvider", parallel = true)
    public Object[][] provideAllSingleOperators() {
        List<Filter> allFilters = new ArrayList<>();
        Collections.addAll(allFilters, isNullFilterResults);
        Collections.addAll(allFilters, matchFunctionsFilterResults);
        Collections.addAll(allFilters, mathFilterResultsGroup1);
        Collections.addAll(allFilters, mathFilterResultsGroup2);
        Collections.addAll(allFilters, mathFilterResultsGroup3);
        Collections.addAll(allFilters, stringFunctionsFilterResultsGroup1);
        Collections.addAll(allFilters, stringFunctionsFilterResultsGroup2);
        Collections.addAll(allFilters, dateFunctionsFilterResults);
        Collections.addAll(allFilters, comparisonFilterResults);

        Object[][] result = new Object[allFilters.size()][];
        for (int i = 0; i < allFilters.size(); i++) {
            result[i] = new Object[] { allFilters.get(i) };
        }

        return result;
    }

    @Issue("4180")
    @Test(dataProvider = "singleOperatorsDataProvider")
    public void testSingleTagFilter(Filter filter) {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" WHERE %s ORDER BY tags.tag",
                TEST_METRICS[0],
                filter.expression
        );

        String[][] expectedRows = new String[filter.expectedResult.length][1];
        for (int i = 0; i < filter.expectedResult.length; i++) {
            expectedRows[i][0] = filter.expectedResult[i];
        }

        assertSqlQueryRows(
                String.format("Wrong query result using single tag filter: %s", filter),
                expectedRows,
                sqlQuery);
    }

    // cross-join of each filter group
    @DataProvider(name = "doubleOperatorsDataProvider", parallel = true)
    public Object[][] provideAllDoubleOperators() {
        List<FilterTuple> allFilterTuples = new ArrayList<>();
        allFilterTuples.addAll(createFiltersCrossJoin(isNullFilterResults));
        allFilterTuples.addAll(createFiltersCrossJoin(matchFunctionsFilterResults));
        allFilterTuples.addAll(createFiltersCrossJoin(mathFilterResultsGroup1));
        allFilterTuples.addAll(createFiltersCrossJoin(mathFilterResultsGroup2));
        allFilterTuples.addAll(createFiltersCrossJoin(mathFilterResultsGroup3));
        allFilterTuples.addAll(createFiltersCrossJoin(stringFunctionsFilterResultsGroup1));
        allFilterTuples.addAll(createFiltersCrossJoin(stringFunctionsFilterResultsGroup2));
        allFilterTuples.addAll(createFiltersCrossJoin(dateFunctionsFilterResults));
        allFilterTuples.addAll(createFiltersCrossJoin(comparisonFilterResults));

        Object[][] result = new Object[allFilterTuples.size()][1];
        for (int i = 0; i < allFilterTuples.size(); i++) {
            result[i] = new Object[] { allFilterTuples.get(i) };
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

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAnd(FilterTuple filterTuple) {
        Set<String> firstResultSet = new HashSet<>();
        Collections.addAll(firstResultSet, filterTuple.firstFilter.expectedResult);

        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        for (String resultRow : filterTuple.secondFilter.expectedResult) {
            if (!firstResultSet.contains(resultRow)) continue;
            finalResultSet.add(resultRow);
        }

        String[][] expectedRows = new String[finalResultSet.size()][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" WHERE ((%2$s) AND (%3$s)) OR ((%2$s) AND (%3$s)) ORDER BY tags.tag",
                TEST_METRICS[0],
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersOr(FilterTuple filterTuple) {
        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        Collections.addAll(finalResultSet, filterTuple.firstFilter.expectedResult);
        Collections.addAll(finalResultSet, filterTuple.secondFilter.expectedResult);

        String[][] expectedRows = new String[finalResultSet.size()][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A OR B
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%1$s\" WHERE ((%2$s) OR (%3$s)) AND ((%2$s) OR (%3$s)) ORDER BY tags.tag",
                TEST_METRICS[0],
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s OR %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
    }

    //TODO pending fix in #4180
    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider", enabled = false)
    public void testDoubleTagFiltersJoinAnd(FilterTuple filterTuple) {
        Set<String> firstResultSet = new HashSet<>();
        Collections.addAll(firstResultSet, filterTuple.firstFilter.expectedResult);

        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        for (String resultRow : filterTuple.secondFilter.expectedResult) {
            if (!firstResultSet.contains(resultRow)) continue;
            finalResultSet.add(resultRow);
        }

        String[][] expectedRows = new String[finalResultSet.size()][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT m1.tags.tag " +
                "FROM \"%1$s\" m1 " +
                "JOIN \"%2$s\" m2 " +
                "JOIN USING ENTITY \"%3$s\" m3 " +
                "OUTER JOIN \"%4$s\" m4 " +
                "OUTER JOIN USING ENTITY \"%5$s\" m5 " +
                "WHERE ((%6$s) AND (%7$s)) OR ((%8$s) AND (%9$s)) OR ((%10$s) AND (%11$s)) " +
                "ORDER BY m1.tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                TEST_METRICS[2],
                TEST_METRICS[3],
                TEST_METRICS[4],
                filterTuple.firstFilter.expression.replace("tags.tag", "m1.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m2.tags.tag"),
                filterTuple.firstFilter.expression.replace("tags.tag", "m3.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m4.tags.tag"),
                filterTuple.firstFilter.expression.replace("tags.tag", "m5.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m1.tags.tag")
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersJoinOr(FilterTuple filterTuple) {
        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        Collections.addAll(finalResultSet, filterTuple.firstFilter.expectedResult);
        Collections.addAll(finalResultSet, filterTuple.secondFilter.expectedResult);

        String[][] expectedRows = new String[finalResultSet.size()][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A OR B
        String sqlQuery = String.format(
                "SELECT m1.tags.tag " +
                "FROM \"%1$s\" m1 " +
                "JOIN \"%2$s\" m2 " +
                "JOIN USING ENTITY \"%3$s\" m3 " +
                "OUTER JOIN \"%4$s\" m4 " +
                "OUTER JOIN USING ENTITY \"%5$s\" m5 " +
                "WHERE ((%6$s) OR (%7$s)) AND ((%8$s) OR (%9$s)) AND ((%10$s) OR (%11$s)) " +
                "ORDER BY m1.tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                TEST_METRICS[2],
                TEST_METRICS[3],
                TEST_METRICS[4],
                filterTuple.firstFilter.expression.replace("tags.tag", "m1.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m2.tags.tag"),
                filterTuple.firstFilter.expression.replace("tags.tag", "m3.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m4.tags.tag"),
                filterTuple.firstFilter.expression.replace("tags.tag", "m5.tags.tag"),
                filterTuple.secondFilter.expression.replace("tags.tag", "m1.tags.tag")
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAtsdSeriesAnd(FilterTuple filterTuple) {
        Set<String> firstResultSet = new HashSet<>();
        Collections.addAll(firstResultSet, filterTuple.firstFilter.expectedResult);

        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        for (String resultRow : filterTuple.secondFilter.expectedResult) {
            if (!firstResultSet.contains(resultRow)) continue;
            finalResultSet.add(resultRow);
        }

        String[][] expectedRows = new String[finalResultSet.size() * 2][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A AND B
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                "FROM atsd_series " +
                "WHERE metric IN ('%1$s', '%2$s') AND (((%3$s) AND (%4$s)) OR ((%3$s) AND (%4$s))) " +
                "ORDER BY tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAtsdSeriesOr(FilterTuple filterTuple) {
        Set<String> finalResultSet = new TreeSet<>(SqlMerticTagsTest::compareTags);

        Collections.addAll(finalResultSet, filterTuple.firstFilter.expectedResult);
        Collections.addAll(finalResultSet, filterTuple.secondFilter.expectedResult);

        String[][] expectedRows = new String[finalResultSet.size() * 2][1];
        int i = 0;
        for (String resultRow : finalResultSet) {
            expectedRows[i][0] = resultRow;
            i++;
            expectedRows[i][0] = resultRow;
            i++;
        }

        // Logically similar to A OR B
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                "FROM atsd_series " +
                "WHERE metric IN ('%1$s', '%2$s') AND (((%3$s) OR (%4$s)) AND ((%3$s) OR (%4$s))) " +
                "ORDER BY tags.tag",
                TEST_METRICS[0],
                TEST_METRICS[1],
                filterTuple.firstFilter.expression,
                filterTuple.secondFilter.expression
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s OR %s",
                        filterTuple.firstFilter.expression,
                        filterTuple.secondFilter.expression),
                expectedRows,
                sqlQuery);
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
