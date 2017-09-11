package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.*;

public class GroupByCaseExpressionTest extends SqlTest {
    private static final String TEST_ENTITY1_NAME = entity();
    private static final String TEST_ENTITY1_LABEL = Mocks.LABEL + "1";

    private static final String TEST_ENTITY2_NAME = entity();
    private static final String TEST_ENTITY2_LABEL = Mocks.LABEL + "2";

    private static final String TEST_METRIC_NAME = metric();

    private static final String TEXT_VALUE_1 = TEXT_VALUE + "1";
    private static final String TEXT_VALUE_2 = TEXT_VALUE + "2";

    @BeforeClass
    public static void prepareData() throws Exception {

        Entity testEntity1 = new Entity(TEST_ENTITY1_NAME);
        testEntity1.setLabel(TEST_ENTITY1_LABEL);

        Entity testEntity2 = new Entity(TEST_ENTITY2_NAME);
        testEntity2.setLabel(TEST_ENTITY2_LABEL);


        Series series1 = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME);

        series1.addSamples(
                Sample.ofDateDecimalText("2017-02-09T12:00:00.000Z", DECIMAL_VALUE, TEXT_VALUE_1),
                Sample.ofDateDecimalText("2017-02-10T12:00:00.000Z", DECIMAL_VALUE, TEXT_VALUE_1)
        );

        Series series2 = new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME);

        series2.addSamples(
                Sample.ofDateDecimalText("2017-02-11T12:00:00.000Z", DECIMAL_VALUE, TEXT_VALUE_2),
                Sample.ofDateDecimalText("2017-02-12T12:00:00.000Z", DECIMAL_VALUE, TEXT_VALUE_2)
        );

        EntityMethod.createOrReplaceEntity(testEntity2);
        EntityMethod.createOrReplaceEntity(testEntity1);
        SeriesMethod.insertSeriesCheck(series1, series2);
    }

    @Issue("3892")
    @Test
    public void testCaseInSelectWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END " +
                        "FROM \"%s\" " +
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

    @Issue("3892")
    @Test
    public void testCaseInGroupByOnly() {
        String sqlQuery = String.format(
                "SELECT count(value) FROM \"%s\" " +
                        "GROUP BY CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
                {"2"}
        };

        assertSqlQueryRows("CASE in GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3892")
    @Test
    public void testCaseInSelectAndGroupBy() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END AS day_type, " +
                        "count(value) " +
                        "FROM \"%s\" " +
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

    @Issue("3912")
    @Test
    public void testGroupByColumnAlias() {
        String sqlQuery = String.format(
                "SELECT CASE WHEN date_format(time, 'u') > '5' THEN 'weekend' ELSE 'workday' END AS \"Day type\"," +
                        "count(value) AS \"Value\"" +
                        "FROM \"%s\"" +
                        "GROUP BY \"Day type\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"weekend", "2"},
                {"workday", "2"}
        };

        assertSqlQueryRows("CASE in SELECT and GROUP BY gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3855")
    @Test
    public void testGroupByEntityLabel() {
        String sqlQuery = String.format(
                "SELECT entity.label, COUNT(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity.label",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY1_LABEL, "2"},
                {TEST_ENTITY2_LABEL, "2"}
        };

        assertSqlQueryRows("GROUP BY entity label gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3855")
    @Test
    public void testGroupByEntityLabelAlias() {
        String sqlQuery = String.format(
                "SELECT entity.label AS \"Label\", COUNT(value)" +
                "FROM \"%s\" " +
                "GROUP BY \"Label\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEST_ENTITY1_LABEL, "2"},
                {TEST_ENTITY2_LABEL, "2"}
        };

        assertSqlQueryRows("GROUP BY entity label alias gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3855")
    @Test
    public void testGroupByTextField() {
        String sqlQuery = String.format(
                "SELECT text, COUNT(value) " +
                "FROM \"%s\" " +
                "GROUP BY text",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEXT_VALUE_1, "2"},
                {TEXT_VALUE_2, "2"}
        };

        assertSqlQueryRows("GROUP BY text field gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3855")
    @Test
    public void testGroupByTextFieldAlias() {
        String sqlQuery = String.format(
                "SELECT text AS \"Text field\", COUNT(value) " +
                "FROM \"%s\" " +
                "GROUP BY \"Text field\"",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {TEXT_VALUE_1, "2"},
                {TEXT_VALUE_2, "2"}
        };

        assertSqlQueryRows("GROUP BY text field alias gives wrong result", expectedRows, sqlQuery);
    }
}
