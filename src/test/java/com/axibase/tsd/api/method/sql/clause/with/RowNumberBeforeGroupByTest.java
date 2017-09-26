package com.axibase.tsd.api.method.sql.clause.with;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.*;

public class RowNumberBeforeGroupByTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();;

        for (int i = 0; i < 20; i++) {
            Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, "tag", Integer.toString(i));
            series.addSamples(Sample.ofDateInteger(Util.ISOFormat(MILLS_TIME + i), i));
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3797")
    @Test
    public void testRowNumberWithoutWhereAndGroupBy() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WITH ROW_NUMBER(entity ORDER BY time ASC) <= 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"0"},
                {"1"},
                {"2"}
        };

        assertSqlQueryRows("Row Number (apart from WHERE or GROUP BY) function gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberWithWhereAndWithoutGroupBy() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE value < 2 " +
                        "WITH ROW_NUMBER(entity ORDER BY time ASC) <= 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"0"},
                {"1"}
        };

        assertSqlQueryRows("Row Number function with WHERE gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberBeforeGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(value) " +
                        "FROM \"%s\" " +
                        "WITH ROW_NUMBER(entity ORDER BY time ASC) <= 9 " +
                        "GROUP BY entity",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"36"}
        };

        assertSqlQueryRows("Row Number function before GROUP BY gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberAfterGroupBy() {
        String sqlQuery = String.format(
                "SELECT sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY tags.tag " +
                        "WITH ROW_NUMBER(entity ORDER BY sum(value) DESC) <= 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"18"},
                {"17"}
        };

        assertSqlQueryRows("Row Number function after GROUP BY gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberBeforeGroupByWithWhere() {
        String sqlQuery = String.format(
                "SELECT sum(value) " +
                        "FROM \"%s\" " +
                        "WHERE value < 15 " +
                        "WITH ROW_NUMBER(entity ORDER BY time ASC) <= 9 " +
                        "GROUP BY entity",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"36"}
        };

        assertSqlQueryRows("WHERE and Row Number function used before GROUP BY gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberAfterGroupByWithWhere() {
        String sqlQuery = String.format(
                "SELECT sum(value) " +
                        "FROM \"%s\" " +
                        "WHERE value > 17 " +
                        "GROUP BY tags.tag " +
                        "WITH ROW_NUMBER(entity ORDER BY sum(value) DESC) <= 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"18"}
        };

        assertSqlQueryRows("WHERE and Row Number function used after GROUP BY gives wrong result",
                expectedRows, sqlQuery);
    }

    @Issue("3797")
    @Test
    public void testRowNumberWithGroupingByPeriod() {
        String sqlQuery = String.format(
                "SELECT sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY period(5 millisecond) " +
                        "WITH ROW_NUMBER(entity ORDER BY period(5 millisecond) DESC) <= 3",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"85"},
                {"60"},
                {"35"}
        };

        assertSqlQueryRows("Row Number function with period function inside gives wrong result",
                expectedRows, sqlQuery);
    }
}
