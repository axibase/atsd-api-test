package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.axibase.tsd.api.util.Mocks.DECIMAL_VALUE;
import static com.axibase.tsd.api.util.Mocks.TEXT_VALUE;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class GroupByCaseExpression extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();
    private static final String TEXT_VALUE_1 = TEXT_VALUE + "1";
    private static final String TEXT_VALUE_2 = TEXT_VALUE + "2";

    @BeforeClass
    public static void prepareData() throws Exception {

        Entity testEntity = new Entity();
        testEntity.setName(TEST_ENTITY_NAME);
        testEntity.setLabel(Mocks.LABEL);
        EntityMethod.createOrReplaceEntity(testEntity);

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);

        series.setData(Arrays.asList(
                new Sample("2017-02-09T12:00:00.000Z", new BigDecimal(DECIMAL_VALUE), TEXT_VALUE_1),
                new Sample("2017-02-10T12:00:00.000Z", new BigDecimal(DECIMAL_VALUE), TEXT_VALUE_1),
                new Sample("2017-02-11T12:00:00.000Z", new BigDecimal(DECIMAL_VALUE), TEXT_VALUE_2),
                new Sample("2017-02-12T12:00:00.000Z", new BigDecimal(DECIMAL_VALUE), TEXT_VALUE_2)
                )
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInSelectWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END " +
                        "FROM '%s' " +
                        "ORDER BY 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend"},
                {"weekend"},
                {"workday"},
                {"workday"}
        };

        assertSqlQueryRows("CASE in SELECT without GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInGroupByOnly() {
        String sqlQuery = String.format(
                "SELECT count(value) FROM '%s' " +
                        "GROUP BY CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"2"}
        };

        assertSqlQueryRows("CASE in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3892
     */
    @Test
    public void testCaseInSelectAndGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END AS day_type, " +
                        "count(value) " +
                        "FROM '%s' " +
                        "GROUP BY day_type " +
                        "ORDER BY 1",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend", "2"},
                {"workday", "2"}
        };

        assertSqlQueryRows("CASE in SELECT and GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3912
     */
    @Test
    public void testGroupByColumnAlias() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END AS \"Day type\"," +
                        "count(value) AS \"Value\"" +
                        "FROM '%s'" +
                        "GROUP BY \"Day type\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend", "2"},
                {"workday", "2"}
        };

        assertSqlQueryRows("CASE in SELECT and GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3855
     */
    @Test
    public void testGroupByEntityLabel() {
        String sqlQuery = String.format(
                "SELECT entity.label " +
                        "FROM '%s' " +
                        "GROUP BY entity.label",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {Mocks.LABEL}
        };

        assertSqlQueryRows("GROUP BY entity label gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3855
     */
    @Test
    public void testGroupByEntityLabelAlias() {
        String sqlQuery = String.format(
                "SELECT entity.label AS \"Label\" " +
                "FROM '%s' " +
                "GROUP BY \"Label\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {Mocks.LABEL}
        };

        assertSqlQueryRows("GROUP BY entity label alias gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3855
     */
    @Test
    public void testGroupByTextField() {
        String sqlQuery = String.format(
                "SELECT text " +
                "FROM '%s' " +
                "GROUP BY text",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEXT_VALUE_1},
                {TEXT_VALUE_2}
        };

        assertSqlQueryRows("GROUP BY text field gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3855
     */
    @Test
    public void testGroupByTextFieldAlias() {
        String sqlQuery = String.format(
                "SELECT text AS \"Text field\" " +
                "FROM '%s' " +
                "GROUP BY \"Text field\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEXT_VALUE_1},
                {TEXT_VALUE_2}
        };

        assertSqlQueryRows("GROUP BY text field alias gives wrong result", expectedRows, sqlQuery);
    }

    /**
     * #3855
     */
    @Test
    public void testGroupByTextAggregationFunction() {
        String sqlQuery = String.format(
                "SELECT CONCAT(text, \"2\") AS \"Text field\" " +
                        "FROM '%s' " +
                        "GROUP BY \"Text field\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEXT_VALUE_1 + "2"},
                {TEXT_VALUE_2 + "2"}
        };

        assertSqlQueryRows("GROUP BY text aggregation function gives wrong result", expectedRows, sqlQuery);
    }
}
