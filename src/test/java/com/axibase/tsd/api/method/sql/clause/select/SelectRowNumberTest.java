package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SelectRowNumberTest extends SqlTest {
    private static final String ENTITY_NAME1 = entity();
    private static final String ENTITY_NAME2 = entity();
    private static final String ENTITY_NAME3 = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME);
        series1.addSamples(
                Sample.ofDateInteger("2017-06-20T00:00:00Z", 1),
                Sample.ofDateInteger("2017-06-21T00:00:00Z", 2)
        );

        /* Additional samples with different entity */
        Series series2 = new Series(ENTITY_NAME2, METRIC_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2017-06-22T00:00:00Z", 3),
                Sample.ofDateInteger("2017-06-23T00:00:00Z", 4)
        );

        Series series3 = new Series(ENTITY_NAME3, METRIC_NAME);
        series3.addSamples(
                Sample.ofDateInteger("2017-06-24T00:00:00Z", 5),
                Sample.ofDateInteger("2017-06-25T00:00:00Z", 6)
        );

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    @Issue("3845")
    @Test(
            description = "Test row_number support in SELECT and ORDER BY"
    )
    public void testSelectRowNumberOrderBy() {
        String sqlQuery = String.format(
                "SELECT entity, value, row_number() FROM \"%s\" " +
                        "WITH ROW_NUMBER(entity order by value) > 0 " +
                        "ORDER BY row_number(), value",
                METRIC_NAME
        );

        String[][] expectedResult = {
                {ENTITY_NAME1, "1", "1"},
                {ENTITY_NAME2, "3", "1"},
                {ENTITY_NAME3, "5", "1"},
                {ENTITY_NAME1, "2", "2"},
                {ENTITY_NAME2, "4", "2"},
                {ENTITY_NAME3, "6", "2"}
        };

        assertSqlQueryRows("Wrong result for row_number in ORDER BY", expectedResult, sqlQuery);
    }

    @Issue("3845")
    @Test(
            description = "Test row_number support in SELECT and GROUP BY"
    )
    public void testSelectRowNumberGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(value), row_number() FROM \"%s\" " +
                        "WITH ROW_NUMBER(entity order by value) > 0 " +
                        "GROUP BY row_number() " +
                        "ORDER BY row_number()",
                METRIC_NAME
        );

        String[][] expectedResult = {
                {"9", "1"},
                {"12", "2"}
        };

        assertSqlQueryRows("Wrong result for row_number in GROUP BY", expectedResult, sqlQuery);
    }

    @Issue("3845")
    @Test(
            description = "Check error message when trying to use row_number() function inside WITH ROW_NUMBER"
    )
    public void testSelectRowNumberRecursive() {
        String sqlQuery = String.format(
                "SELECT value, row_number() FROM \"%s\" " +
                        "WITH ROW_NUMBER(entity order by row_number()) > 0 " +
                        "ORDER BY row_number(), value",
                METRIC_NAME
        );

        assertBadRequest("row_number() function is not allowed inside row_number clause " +
                "at line 1 position 148 near \"row_number\"", queryResponse(sqlQuery));
    }

    @Issue("3845")
    @Test(
            description = "Check error message when trying to use row_number() function without WITH ROW_NUMBER"
    )
    public void testSelectRowNumberWithIsRequired() {
        String sqlQuery = String.format(
                "SELECT value, row_number() FROM \"%s\"",
                METRIC_NAME
        );

        assertBadRequest("row_number function requires WITH ROW_NUMBER clause. at line 1 position 14 near \"row_number\"", queryResponse(sqlQuery));
    }
}
