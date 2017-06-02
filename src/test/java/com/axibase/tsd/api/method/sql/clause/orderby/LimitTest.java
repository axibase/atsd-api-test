package com.axibase.tsd.api.method.sql.clause.orderby;


import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

    @BeforeClass
    public static void generateNames() {
        ENTITY_ORDER_METRIC = metric();
        VALUE_ORDER_METRIC = metric();
        DATETIME_ORDER_METRIC = metric();
        TAGS_ORDER_METRIC = metric();
    }

    @BeforeGroups(groups = {ENTITY_ORDER_TEST_GROUP})
    public void prepareEntityOrderData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            Long date = TestUtil.parseDate("2016-06-19T11:00:00.000Z").getTime();
            Series series = new Series(entity(), ENTITY_ORDER_METRIC);
            for (int j = 0; j < 10 - i; j++) {
                Sample sample = new Sample(TestUtil.ISOFormat(date + j * TimeUnit.HOURS.toMillis(1)), j);
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
                        "SELECT entity, AVG (value) FROM '%s'%nGROUP BY entity%nORDER BY AVG(value)",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM '%s'%nGROUP BY entity%nORDER BY AVG(value) DESC",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM '%s'%nWHERE value > 3%nGROUP BY entity%nORDER BY AVG(value)",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM '%s'%nGROUP BY entity%nHAVING AVG(value) > 3%nORDER BY AVG(value)",
                        3
                }
        };
    }

    /**
     * #3416
     */
    @Test(groups = {ENTITY_ORDER_TEST_GROUP}, dataProvider = "entityOrderProvider")
    public void testEntityOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, ENTITY_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }


    @BeforeGroups(groups = {VALUE_ORDER_TEST_GROUP})
    public void prepareValueOrderData() throws Exception {
        Long date = TestUtil.parseDate("2016-06-19T11:00:00.000Z").getTime();
        Series series = new Series(entity(), VALUE_ORDER_METRIC);
        float[] values = {1.23f, 3.12f, 5.67f, 4.13f, 5, -4, 4, 8, 6, 5};
        for (int i = 1; i < 10; i++) {
            Sample sample = new Sample(
                    TestUtil.ISOFormat(date + i * TimeUnit.HOURS.toMillis(1)),
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
                        "SELECT value FROM '%s'%nORDER BY value",
                        3
                },
                {
                        "SELECT value FROM '%s'%nORDER BY value DESC",
                        3
                },
                {
                        "SELECT entity, AVG (value) FROM '%s'%nWHERE value > 3%nGROUP BY entity%nORDER BY AVG (value)",
                        3
                }
        };
    }


    /**
     * #3416
     */
    @Test(groups = {VALUE_ORDER_TEST_GROUP}, dataProvider = "valueOrderProvider")
    public void testValueOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, VALUE_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }

    @BeforeGroups(groups = {DATETIME_ORDER_TEST_GROUP})
    public void prepareDateTimeOrderData() throws Exception {
        Series series = new Series(entity(), DATETIME_ORDER_METRIC);
        series.addSamples(
                new Sample("2016-06-19T11:00:00.000Z", 1),
                new Sample("2016-06-19T11:03:00.000Z", 2),
                new Sample("2016-06-19T11:02:00.000Z", 3),
                new Sample("2016-06-19T11:01:00.000Z", 5),
                new Sample("2016-06-19T11:04:00.000Z", 4)
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @DataProvider(name = "datetimeOrderProvider")
    public Object[][] datetimeOrderProvider() {
        return new Object[][]{
                {
                        "SELECT datetime FROM '%s'%nORDER BY datetime",
                        3
                },
                {
                        "SELECT datetime FROM '%s'%nORDER BY datetime DESC",
                        3
                },
                {
                        "SELECT datetime FROM '%s'%nWHERE datetime > '2016-06-19T11:02:00.000Z'%n",
                        3
                }
        };
    }

    @BeforeGroups(groups = {TAGS_ORDER_TEST_GROUP})
    public void prepareTagsTimeOrderData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String entityName = entity();
        Long startTime = TestUtil.parseDate("2016-06-19T11:00:00.000Z").getTime();
        int[] values = {6, 7, 0, -1, 5, 15, 88, 3, 11, 2};
        for (int i = 0; i < 3; i++) {
            Series series = new Series(entityName, TAGS_ORDER_METRIC);
            series.addSamples(new Sample(TestUtil.ISOFormat(startTime + i * TimeUnit.HOURS.toMillis(1)), values[i]));
            seriesList.add(series);
        }
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3416
     */
    @Test(groups = {DATETIME_ORDER_TEST_GROUP}, dataProvider = "datetimeOrderProvider")
    public void testDateTimeOrder(String sqlQueryTemplate, Integer limit) throws Exception {
        String sqlQuery = String.format(sqlQueryTemplate, DATETIME_ORDER_METRIC);
        assertQueryLimit(limit, sqlQuery);
    }


    @DataProvider(name = "tagsOrderProvider")
    public Object[][] tagsOrderProvider() {
        return new Object[][]{
                {
                        "SELECT value , tags.* FROM '%s'%nORDER BY tags.a",
                        2
                },
                {
                        "SELECT value , tags.* FROM '%s'%nORDER BY tags.a DESC",
                        2
                }
        };
    }


    @DataProvider(name = "metricOrderProvider")
    public Object[][] metricOrderProvider() {
        return new Object[][]{
                {
                        "SELECT * FROM '%s' t1%nOUTER JOIN '%s' t2%nOUTER JOIN '%s' t3%nORDER BY t1.metric",
                        2
                },
                {
                        "SELECT * FROM '%s' t1%nOUTER JOIN '%s' t2%nOUTER JOIN '%s' t3%nORDER BY t1.metric DESC",
                        2
                }
        };
    }

    /**
     * #3416
     */
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
}
