package com.axibase.tsd.api.method.sql.clause.orderby;


import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;


public class LimitTest extends SqlTest {
    private static final String ENTITY_ORDER_TEST_GROUP = "entity-order-test-group";
    private static final String VALUE_ORDER_TEST_GROUP = "value-order-test-group";
    private static final String DATETIME_ORDER_TEST_GROUP = "datetime-order-test-group";
    private static final String TAGS_ORDER_TEST_GROUP = "tags-order-test-group";
    private static String ENTITY_ORDER_METRIC;
    private static String VALUE_ORDER_METRIC;
    private static String DATETIME_ORDER_METRIC;
    private static String TAGS_ORDER_METRIC;
    private static String HIGH_CARDINALITY_METRIC;

    @BeforeClass
    public static void prepareData() throws Exception {
        ENTITY_ORDER_METRIC = metric();
        VALUE_ORDER_METRIC = metric();
        DATETIME_ORDER_METRIC = metric();
        TAGS_ORDER_METRIC = metric();

        HIGH_CARDINALITY_METRIC = metric();
        String highCardinalityEntity = entity();
        List<Series> highCardinalitySeries = new ArrayList<>();
        for (int tagIndex = 0; tagIndex < 5; tagIndex++) {
            for (int value = 0; value < 10; value++) {
                Series series = new Series(
                        highCardinalityEntity,
                        HIGH_CARDINALITY_METRIC,
                        "tag" + tagIndex,
                        "value" + value);
                series.addSamples(Sample.ofTimeInteger(value, value));
                highCardinalitySeries.add(series);
            }
        }
        for (int tagIndex = 5; tagIndex < 8; tagIndex++) {
            for (int value = 0; value < 20; value++) {
                Series series = new Series(
                        highCardinalityEntity,
                        HIGH_CARDINALITY_METRIC,
                        "tag" + tagIndex,
                        "value" + (value % 5));
                series.addSamples(Sample.ofTimeInteger(value, value));
                highCardinalitySeries.add(series);
            }
        }

        SeriesMethod.insertSeriesCheck(highCardinalitySeries);
    }

    @BeforeGroups(groups = {ENTITY_ORDER_TEST_GROUP})
    public void prepareEntityOrderData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            Long date = Util.parseDate("2016-06-19T11:00:00.000Z").getTime();
            Series series = new Series(entity(), ENTITY_ORDER_METRIC);
            for (int j = 0; j < 10 - i; j++) {
                Sample sample = Sample.ofDateInteger(Util.ISOFormat(date + j * TimeUnit.HOURS.toMillis(1)), j);
                series.addSamples(sample);

            }
            seriesList.add(series);
        }
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @DataProvider(name = "entityOrderProvider")
    public Object[][] entityOrderProvider() {
        return new Object[][]{
                {
                        "SELECT entity, AVG (value) FROM \"%s\"%nGROUP BY entity%nORDER BY AVG(value)",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM \"%s\"%nGROUP BY entity%nORDER BY AVG(value) DESC",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM \"%s\"%nWHERE value > 3%nGROUP BY entity%nORDER BY AVG(value)",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM \"%s\"%nGROUP BY entity%nHAVING AVG(value) > 3%nORDER BY AVG(value)",
                        3
                }
        };
    }

    @Issue("3416")
    @Test(groups = {ENTITY_ORDER_TEST_GROUP}, dataProvider = "entityOrderProvider")
    public void testEntityOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, ENTITY_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }


    @BeforeGroups(groups = {VALUE_ORDER_TEST_GROUP})
    public void prepareValueOrderData() throws Exception {
        Long date = Util.parseDate("2016-06-19T11:00:00.000Z").getTime();
        Series series = new Series(entity(), VALUE_ORDER_METRIC);
        float[] values = {1.23f, 3.12f, 5.67f, 4.13f, 5, -4, 4, 8, 6, 5};
        for (int i = 1; i < 10; i++) {
            Sample sample = Sample.ofDateDecimal(
                    Util.ISOFormat(date + i * TimeUnit.HOURS.toMillis(1)),
                    new BigDecimal(values[i])
            );
            series.addSamples(sample);

        }
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @DataProvider(name = "valueOrderProvider")
    public Object[][] valueOrderProvider() {
        return new Object[][]{
                {
                        "SELECT value FROM \"%s\"%nORDER BY value",
                        3
                },
                {
                        "SELECT value FROM \"%s\"%nORDER BY value DESC",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM \"%s\"%nWHERE value > 3%nGROUP BY entity%nORDER BY AVG (value)",
                        3
                }
        };
    }


    @Issue("3416")
    @Test(groups = {VALUE_ORDER_TEST_GROUP}, dataProvider = "valueOrderProvider")
    public void testValueOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, VALUE_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }

    @BeforeGroups(groups = {DATETIME_ORDER_TEST_GROUP})
    public void prepareDateTimeOrderData() throws Exception {
        Series series = new Series(entity(), DATETIME_ORDER_METRIC);
        series.addSamples(
                Sample.ofDateInteger("2016-06-19T11:00:00.000Z", 1),
                Sample.ofDateInteger("2016-06-19T11:03:00.000Z", 2),
                Sample.ofDateInteger("2016-06-19T11:02:00.000Z", 3),
                Sample.ofDateInteger("2016-06-19T11:01:00.000Z", 5),
                Sample.ofDateInteger("2016-06-19T11:04:00.000Z", 4)
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @DataProvider(name = "datetimeOrderProvider")
    public Object[][] datetimeOrderProvider() {
        return new Object[][]{
                {
                        "SELECT datetime FROM \"%s\"%nORDER BY datetime",
                        3
                },
                {
                        "SELECT datetime FROM \"%s\"%nORDER BY datetime DESC",
                        3
                },
                {
                        "SELECT datetime FROM \"%s\"%nWHERE datetime > '2016-06-19T11:02:00.000Z'%n",
                        3
                }
        };
    }

    @BeforeGroups(groups = {TAGS_ORDER_TEST_GROUP})
    public void prepareTagsTimeOrderData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String entityName = entity();
        Long startTime = Util.parseDate("2016-06-19T11:00:00.000Z").getTime();
        int[] values = {6, 7, 0, -1, 5, 15, 88, 3, 11, 2};
        for (int i = 0; i < 3; i++) {
            Series series = new Series(entityName, TAGS_ORDER_METRIC);
            series.addSamples(Sample.ofDateInteger(Util.ISOFormat(startTime + i * TimeUnit.HOURS.toMillis(1)), values[i]));
            seriesList.add(series);
        }
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3416")
    @Test(groups = {DATETIME_ORDER_TEST_GROUP}, dataProvider = "datetimeOrderProvider")
    public void testDateTimeOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, DATETIME_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }


    @DataProvider(name = "tagsOrderProvider")
    public Object[][] tagsOrderProvider() {
        return new Object[][]{
                {
                        "SELECT value , tags.* FROM \"%s\"%nORDER BY tags.a",
                        2
                },
                {
                        "SELECT value , tags.* FROM \"%s\"%nORDER BY tags.a DESC",
                        2
                }
        };
    }


    @DataProvider(name = "metricOrderProvider")
    public Object[][] metricOrderProvider() {
        return new Object[][]{
                {
                        "SELECT * FROM \"%s\" t1%nOUTER JOIN \"%s\" t2%nOUTER JOIN \"%s\" t3%nORDER BY t1.metric",
                        2
                },
                {
                        "SELECT * FROM \"%s\" t1%nOUTER JOIN \"%s\" t2%nOUTER JOIN \"%s\" t3%nORDER BY t1.metric DESC",
                        2
                }
        };
    }

    @Issue("3416")
    @Test(groups = {TAGS_ORDER_TEST_GROUP}, dataProvider = "tagsOrderProvider")
    public void testTagsOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, TAGS_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }

    private void assertQueryLimit(Integer limit, String sqlQuery) {
        List<List<String>> rows = queryTable(sqlQuery).getRows();
        String limitedSqlQuery = String.format("%s%nLIMIT %d", sqlQuery, limit);
        List<List<String>> expectedRows = (rows.size() > limit) ? rows.subList(0, limit) : rows;
        String errorMessage = String.format("SQL query with limit doesn't return first %d rows of query without limit!", limit);
        assertSqlQueryRows(errorMessage, expectedRows, limitedSqlQuery);
    }

    @DataProvider
    public Object[][] orderLimitFilterProvider() {
        return new Object[][]{
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY tags.tag1 LIMIT 3",
                        new String[][]{
                                {"5", "value5"},
                                {"6", "value6"},
                                {"7", "value7"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY tags.tag1 ASC LIMIT 3",
                        new String[][]{
                                {"5", "value5"},
                                {"6", "value6"},
                                {"7", "value7"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' AND tags.tag1 <= 'value8' " +
                        "ORDER BY tags.tag1 DESC LIMIT 3",
                        new String[][]{
                                {"8", "value8"},
                                {"7", "value7"},
                                {"6", "value6"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY datetime LIMIT 3",
                        new String[][]{
                                {"5", "value5"},
                                {"6", "value6"},
                                {"7", "value7"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY datetime ASC LIMIT 3",
                        new String[][]{
                                {"5", "value5"},
                                {"6", "value6"},
                                {"7", "value7"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' AND tags.tag1 <= 'value8' " +
                        "ORDER BY datetime DESC LIMIT 3",
                        new String[][]{
                                {"8", "value8"},
                                {"7", "value7"},
                                {"6", "value6"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY datetime LIMIT 3 OFFSET 2",
                        new String[][]{
                                {"7", "value7"},
                                {"8", "value8"},
                                {"9", "value9"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value4' " +
                        "ORDER BY datetime ASC LIMIT 3 OFFSET 2",
                        new String[][]{
                                {"7", "value7"},
                                {"8", "value8"},
                                {"9", "value9"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' AND tags.tag1 <= 'value8' " +
                        "ORDER BY datetime DESC LIMIT 3 OFFSET 2",
                        new String[][]{
                                {"6", "value6"},
                                {"5", "value5"},
                                {"4", "value4"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' WITH ROW_NUMBER(entity ORDER BY time) <= 3 " +
                        "ORDER BY tags.tag5",
                        new String[][]{
                                {"2", "value2"},
                                {"3", "value3"},
                                {"4", "value4"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' " +
                        "WITH ROW_NUMBER(entity ORDER BY time ASC) <= 3 " +
                        "ORDER BY tags.tag5",
                        new String[][]{
                                {"2", "value2"},
                                {"3", "value3"},
                                {"4", "value4"}
                        }},
                {"SELECT time, tags.tag1 FROM \"%s\" " +
                        "WHERE tags.tag1 > 'value1' AND tags.tag1 <= 'value8' " +
                        "WITH ROW_NUMBER(entity ORDER BY time DESC) <= 3 " +
                        "ORDER BY tags.tag5 DESC",
                        new String[][]{
                                {"8", "value8"},
                                {"7", "value7"},
                                {"6", "value6"}
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 > 'value1' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 3 " +
                        "ORDER BY tags.tag5, time",
                        new String[][]{
                                {"2", "value2"},
                                {"7", "value2"},
                                {"12", "value2"},
                                {"3", "value3"},
                                {"8", "value3"},
                                {"13", "value3"},
                                {"4", "value4"},
                                {"9", "value4"},
                                {"14", "value4"}
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 > 'value1' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time ASC) <= 3 " +
                        "ORDER BY tags.tag5 ASC, time",
                        new String[][]{
                                {"2", "value2"},
                                {"7", "value2"},
                                {"12", "value2"},
                                {"3", "value3"},
                                {"8", "value3"},
                                {"13", "value3"},
                                {"4", "value4"},
                                {"9", "value4"},
                                {"14", "value4"}
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value3' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 3 " +
                        "ORDER BY tags.tag5 ASC, time DESC",
                        new String[][]{
                                {"15", "value0"},
                                {"10", "value0"},
                                {"5", "value0"},
                                {"16", "value1"},
                                {"11", "value1"},
                                {"6", "value1"},
                                {"17", "value2"},
                                {"12", "value2"},
                                {"7", "value2"},
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 > 'value1' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 3 " +
                        "ORDER BY time LIMIT 4",
                        new String[][]{
                                {"2", "value2"},
                                {"3", "value3"},
                                {"4", "value4"},
                                {"7", "value2"}
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value3' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 3 " +
                        "ORDER BY time DESC LIMIT 4 OFFSET 1",
                        new String[][]{
                                {"11", "value1"},
                                {"10", "value0"},
                                {"7", "value2"},
                                {"6", "value1"},
                        }},
                {"SELECT MAX(time), tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 3 " +
                        "GROUP BY tags.tag5 " +
                        "ORDER BY tags.tag5",
                        new String[][]{
                                {"10", "value0"},
                                {"11", "value1"},
                                {"12", "value2"},
                                {"13", "value3"},
                        }},
                {"SELECT MAX(time), tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 3 " +
                        "GROUP BY tags.tag5 " +
                        "ORDER BY tags.tag5",
                        new String[][]{
                                {"15", "value0"},
                                {"16", "value1"},
                                {"17", "value2"},
                                {"18", "value3"},
                        }},
                {"SELECT MAX(time), tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 3 " +
                        "GROUP BY tags.tag5 " +
                        "ORDER BY tags.tag5",
                        new String[][]{
                                {"10", "value0"},
                                {"11", "value1"},
                                {"12", "value2"},
                                {"13", "value3"},
                        }},
                {"SELECT MAX(time), tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 3 " +
                        "GROUP BY tags.tag5 " +
                        "ORDER BY tags.tag5 LIMIT 2 OFFSET 1",
                        new String[][]{
                                {"16", "value1"},
                                {"17", "value2"}
                        }},
                {"SELECT MAX(time), tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 3 " +
                        "GROUP BY tags.tag5 " +
                        "ORDER BY tags.tag5 DESC LIMIT 2 OFFSET 1",
                        new String[][]{
                                {"17", "value2"},
                                {"16", "value1"}
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "GROUP BY tags.tag5, time " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 2 " +
                        "ORDER BY tags.tag5, time",
                        new String[][]{
                                {"0", "value0"},
                                {"5", "value0"},
                                {"1", "value1"},
                                {"6", "value1"},
                                {"2", "value2"},
                                {"7", "value2"},
                                {"3", "value3"},
                                {"8", "value3"},
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "GROUP BY tags.tag5, time " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 2 " +
                        "ORDER BY tags.tag5, time",
                        new String[][]{
                                {"10", "value0"},
                                {"15", "value0"},
                                {"11", "value1"},
                                {"16", "value1"},
                                {"12", "value2"},
                                {"17", "value2"},
                                {"13", "value3"},
                                {"18", "value3"},
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "GROUP BY tags.tag5, time " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time) <= 2 " +
                        "ORDER BY tags.tag5, time LIMIT 3 OFFSET 1",
                        new String[][]{
                                {"5", "value0"},
                                {"1", "value1"},
                                {"6", "value1"},
                        }},
                {"SELECT time, tags.tag5 FROM \"%s\" " +
                        "WHERE tags.tag5 < 'value4' " +
                        "GROUP BY tags.tag5, time " +
                        "WITH ROW_NUMBER(tags.tag5 ORDER BY time DESC) <= 2 " +
                        "ORDER BY tags.tag5 DESC, time DESC LIMIT 3 OFFSET 1",
                        new String[][]{
                                {"13", "value3"},
                                {"17", "value2"},
                                {"12", "value2"},
                        }},

        };
    }

    @Issue("4708")
    @Test(
            dataProvider = "orderLimitFilterProvider",
            description = "test ORDER BY LIMIT")
    public void testOrderByDatetimeLimitDescOrder(String expression, String[][] expectedResult) {
        String sqlQuery = String.format(expression, HIGH_CARDINALITY_METRIC);

        assertSqlQueryRows(
                String.format("Incorrect query result with filter: %s", expression),
                expectedResult,
                sqlQuery);
    }
}
