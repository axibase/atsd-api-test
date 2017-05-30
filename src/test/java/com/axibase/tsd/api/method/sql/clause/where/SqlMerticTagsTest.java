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

    private final Object[][] isNullFilterResults = new Object[][]{
            {"tags.tag IS NULL",                        new String[]{"null"}},
            {"tags.tag IS NOT NULL",                    new String[]{"VALUE1", "otherValue", "value1", "value2", "value3"}},
            {"ISNULL(tags.tag, 'null') = 'null'",       new String[]{"null"}},
            {"NOT ISNULL(tags.tag, 'null') = 'null'",   new String[]{"VALUE1", "otherValue", "value1", "value2", "value3"}}
    };

    private final Object[][] matchFunctionsFilterResults = new Object[][]{
            {"tags.tag LIKE 'value_'",                      new String[]{"value1", "value2", "value3"}},
            {"tags.tag NOT LIKE 'value_'",                  new String[]{"VALUE1", "otherValue"}},
            {"tags.tag LIKE '%2'",                          new String[]{"value2"}},
            {"tags.tag NOT LIKE '%2'",                      new String[]{"VALUE1", "otherValue", "value1", "value3"}},
            {"tags.tag IN ('VALUE1', 'value2')",            new String[]{"VALUE1", "value2"}},
            {"tags.tag NOT IN ('VALUE1', 'value2')",        new String[]{"otherValue", "value1", "value3"}},
            {"tags.tag REGEX 'value[1,2]{1}|.*Value'",      new String[]{"otherValue", "value1", "value2"}},
            {"tags.tag NOT REGEX 'value[1,2]{1}|.*Value'",  new String[]{"VALUE1", "value3"}}
    };

    private final Object[][] mathFilterResultsGroup1 = new Object[][]{
            {"ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"VALUE1", "value1"}},
            {"NOT ABS(-1 * CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"value2", "value3"}},
            {"CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"VALUE1", "value1"}},
            {"NOT CEIL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"value2", "value3"}},
            {"FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"VALUE1", "value1"}},
            {"NOT FLOOR(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1", new String[]{"value2", "value3"}}
    };

    private final Object[][] mathFilterResultsGroup2 = new Object[][]{
            {"ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1", new String[]{"VALUE1", "value1"}},
            {"NOT ROUND(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0) = 1", new String[]{"value2", "value3"}},
            {"MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0", new String[]{"value2"}},
            {"NOT MOD(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 0", new String[]{"VALUE1", "value1", "value3"}},
            {"CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3", new String[]{"VALUE1", "value1"}},
            {"NOT CEIL(EXP(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 3", new String[]{"value2", "value3"}},
            {"FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1", new String[]{"value3"}},
            {"NOT FLOOR(LN(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER))) = 1", new String[]{"VALUE1", "value1", "value2"}}
    };

    private final Object[][] mathFilterResultsGroup3 = new Object[][]{
            {"POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4",        new String[]{"value2"}},
            {"NOT POWER(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 2) = 4",    new String[]{"VALUE1", "value1", "value3"}},
            {"LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3",          new String[]{"value2"}},
            {"NOT LOG(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 8) = 3",      new String[]{"VALUE1", "value1", "value3"}},
            {"SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",            new String[]{"VALUE1", "value1"}},
            {"NOT SQRT(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = 1",        new String[]{"value2", "value3"}}
    };

    private final Object[][] stringFunctionsFilterResultsGroup1 = new Object[][]{
            {"UPPER(tags.tag) = 'VALUE1'", new String[]{"VALUE1", "value1"}},
            {"NOT UPPER(tags.tag) = 'VALUE1'", new String[]{"otherValue", "value2", "value3"}},
            {"LOWER(tags.tag) = 'value1'", new String[]{"VALUE1", "value1"}},
            {"NOT LOWER(tags.tag) = 'value1'", new String[]{"otherValue", "value2", "value3"}},
            {"REPLACE(tags.tag, 'other', 'new') = 'newValue'", new String[]{"otherValue"}},
            {"NOT REPLACE(tags.tag, 'other', 'new') = 'newValue'", new String[]{"VALUE1", "value1", "value2", "value3"}},
            {"LENGTH(tags.tag) = 6", new String[]{"VALUE1", "value1", "value2", "value3"}},
            {"NOT LENGTH(tags.tag) = 6", new String[]{"null", "otherValue"}},
    };

    private final Object[][] stringFunctionsFilterResultsGroup2 = new Object[][]{
            {"CONCAT(tags.tag, '1', '2') = 'value312'",             new String[]{"value3"}},
            {"NOT CONCAT(tags.tag, '1', '2') = 'value312'",         new String[]{"null", "VALUE1", "otherValue", "value1", "value2"}},
            {"SUBSTR(tags.tag, 3, 2) = 'lu'",                       new String[]{"value1", "value2", "value3"}},
            {"NOT SUBSTR(tags.tag, 3, 2) = 'lu'",                   new String[]{"VALUE1", "otherValue"}},
            {"CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1",          new String[]{"VALUE1", "value1"}},
            {"NOT CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER) = 1",      new String[]{"value2", "value3"}}
    };

    private final Object[][] dateFunctionsFilterResults = new Object[][]{
            {"date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    new String[]{"VALUE1", "value1"}},
            {"NOT date_format(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER)) = '1970-01-01T00:00:00.001Z'",
                    new String[]{"value2", "value3"}},
            {"date_parse(CONCAT('1970-01-01 00:00:0', ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), 'yyyy-MM-dd HH:mm:ssZ') = 1000",
                    new String[]{"VALUE1", "value1"}},
            {"NOT date_parse(CONCAT('1970-01-01 00:00:0', ISNULL(CAST(SUBSTR(tags.tag, 6, 1) AS NUMBER), 0), 'Z'), 'yyyy-MM-dd HH:mm:ssZ') = 1000",
                    new String[]{"null", "otherValue", "value2", "value3"}}
    };

    private final Object[][] comparisonFilterResults = new Object[][]{
            {"tags.tag = 'value1'",         new String[]{"value1"}},
            {"NOT tags.tag = 'value1'",     new String[]{"VALUE1", "otherValue", "value2", "value3"}},
            {"tags.tag != 'value1'",        new String[]{"VALUE1", "otherValue", "value2", "value3",}},
            {"NOT tags.tag != 'value1'",    new String[]{"value1"}},
            {"tags.tag > 'value1'",         new String[]{"value2", "value3"}},
            {"NOT tags.tag > 'value1'",     new String[]{"VALUE1", "otherValue", "value1"}},
            {"tags.tag >= 'value1'",        new String[]{"value1", "value2", "value3"}},
            {"NOT tags.tag >= 'value1'",    new String[]{"VALUE1", "otherValue"}},
            {"tags.tag < 'value1'",         new String[]{"VALUE1", "otherValue"}},
            {"NOT tags.tag < 'value1'",     new String[]{"value1", "value2", "value3"}},
            {"tags.tag <= 'value1'",        new String[]{"VALUE1", "otherValue", "value1"}},
            {"NOT tags.tag <= 'value1'",    new String[]{"value2", "value3"}}
    };

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

    @DataProvider(name = "singleOperatorsDataProvider")
    public Object[][] provideAllSingleOperators() {
        List<Object[]> allFilters = new ArrayList<>();
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
            result[i] = allFilters.get(i);
        }

        return result;
    }

    @Issue("4180")
    @Test(dataProvider = "singleOperatorsDataProvider")
    public void testSingleTagFilter(String filter, String[] result) {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM \"%s\" WHERE %s ORDER BY tags.tag",
                TEST_METRICS[0],
                filter
        );

        String[][] expectedRows = new String[result.length][1];
        for (int i = 0; i < result.length; i++) {
            expectedRows[i][0] = result[i];
        }

        assertSqlQueryRows(
                String.format("Wrong query result using single tag filter: %s", filter),
                expectedRows,
                sqlQuery);
    }

    // cross-join of each filter group
    @DataProvider(name = "doubleOperatorsDataProvider")
    public Object[][] provideAllDoubleOperators() {
        List<Object[]> allFilters = new ArrayList<>();
        Collections.addAll(allFilters, createFiltersCrossJoin(isNullFilterResults));
        Collections.addAll(allFilters, createFiltersCrossJoin(matchFunctionsFilterResults));
        Collections.addAll(allFilters, createFiltersCrossJoin(mathFilterResultsGroup1));
        Collections.addAll(allFilters, createFiltersCrossJoin(mathFilterResultsGroup2));
        Collections.addAll(allFilters, createFiltersCrossJoin(mathFilterResultsGroup3));
        Collections.addAll(allFilters, createFiltersCrossJoin(stringFunctionsFilterResultsGroup1));
        Collections.addAll(allFilters, createFiltersCrossJoin(stringFunctionsFilterResultsGroup2));
        Collections.addAll(allFilters, createFiltersCrossJoin(dateFunctionsFilterResults));
        Collections.addAll(allFilters, createFiltersCrossJoin(comparisonFilterResults));

        Object[][] result = new Object[allFilters.size()][];
        for (int i = 0; i < allFilters.size(); i++) {
            result[i] = allFilters.get(i);
        }

        return result;
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

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAnd(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
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
                firstFilter,
                secondFilter
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersOr(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
        Set<String> finalResultSet = new TreeSet<>((o1, o2) -> {
            if (o1.equals("null") && o2.equals("null")) return 0;
            if (o1.equals("null")) return -1;
            if (o2.equals("null")) return 1;
            return o1.compareTo(o2);
        });

        Collections.addAll(finalResultSet, firstResult);
        Collections.addAll(finalResultSet, secondResult);

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
                firstFilter,
                secondFilter
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s OR %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }

    //TODO pending fix in #4180
    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider", enabled = false)
    public void testDoubleTagFiltersJoinAnd(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
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
                firstFilter.replace("tags.tag", "m1.tags.tag"),
                secondFilter.replace("tags.tag", "m2.tags.tag"),
                firstFilter.replace("tags.tag", "m3.tags.tag"),
                secondFilter.replace("tags.tag", "m4.tags.tag"),
                firstFilter.replace("tags.tag", "m5.tags.tag"),
                secondFilter.replace("tags.tag", "m1.tags.tag")
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersJoinOr(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
        Set<String> finalResultSet = new TreeSet<>((o1, o2) -> {
            if (o1.equals("null") && o2.equals("null")) return 0;
            if (o1.equals("null")) return -1;
            if (o2.equals("null")) return 1;
            return o1.compareTo(o2);
        });

        Collections.addAll(finalResultSet, firstResult);
        Collections.addAll(finalResultSet, secondResult);

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
                firstFilter.replace("tags.tag", "m1.tags.tag"),
                secondFilter.replace("tags.tag", "m2.tags.tag"),
                firstFilter.replace("tags.tag", "m3.tags.tag"),
                secondFilter.replace("tags.tag", "m4.tags.tag"),
                firstFilter.replace("tags.tag", "m5.tags.tag"),
                secondFilter.replace("tags.tag", "m1.tags.tag")
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAtsdSeriesAnd(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
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
                firstFilter,
                secondFilter
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s AND %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }

    @Issue("4180")
    @Test(dataProvider = "doubleOperatorsDataProvider")
    public void testDoubleTagFiltersAtsdSeriesOr(
            String firstFilter,
            String[] firstResult,
            String secondFilter,
            String[] secondResult) {
        Set<String> finalResultSet = new TreeSet<>((o1, o2) -> {
            if (o1.equals("null") && o2.equals("null")) return 0;
            if (o1.equals("null")) return -1;
            if (o2.equals("null")) return 1;
            return o1.compareTo(o2);
        });

        Collections.addAll(finalResultSet, firstResult);
        Collections.addAll(finalResultSet, secondResult);

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
                firstFilter,
                secondFilter
        );

        assertSqlQueryRows(
                String.format("Wrong query result using double tag filters: %s OR %s", firstFilter, secondFilter),
                expectedRows,
                sqlQuery);
    }
}
