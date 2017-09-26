package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WhereDateInListTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                Sample.ofDate("2017-09-01T12:00:00.001Z"),
                Sample.ofDate("2017-09-15T12:00:00.001Z"),
                Sample.ofDate("2017-10-01T12:00:00.000Z")
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4380")
    @Test(
            description = "Test whether 'WHERE datetime IN' is supported"
    )
    public void testWhereDateInList() {
        String sqlQuery = String.format(
                "SELECT datetime FROM \"%s\" " +
                        "WHERE datetime IN ('2017-09-01T12:00:00.001Z', '2017-10-01T12:00:00.000Z') " +
                        "ORDER BY datetime",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-09-01T12:00:00.001Z"},
                {"2017-10-01T12:00:00.000Z"}
        };

        assertSqlQueryRows("Wrong result for 'WHERE datetime IN (...)'", expectedRows, sqlQuery);
    }

    @Issue("4380")
    @Test(
            description = "Test whether 'WHERE datetime IN' is supported, combined with OR operator"
    )
    public void testWhereDateInListOr() {
        String sqlQuery = String.format(
                "SELECT datetime FROM \"%s\" " +
                        "WHERE datetime IN ('2017-09-01T12:00:00.001Z') OR datetime IN ('2017-10-01T12:00:00.000Z') " +
                        "ORDER BY datetime",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-09-01T12:00:00.001Z"},
                {"2017-10-01T12:00:00.000Z"}
        };

        assertSqlQueryRows("Wrong result for 'WHERE datetime IN (...) OR datetime IN (...)'",
                expectedRows, sqlQuery);
    }

    @Issue("4380")
    @Test(
            description = "Test whether 'WHERE datetime IN' is supported, combined with NOT operator"
    )
    public void testWhereDateNotInList() {
        String sqlQuery = String.format(
                "SELECT datetime FROM \"%s\" " +
                        "WHERE NOT(datetime IN ('2017-09-01T12:00:00.001Z', '2017-10-01T12:00:00.000Z')) " +
                        "ORDER BY datetime",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-09-15T12:00:00.001Z"}
        };

        assertSqlQueryRows("Wrong result for 'WHERE NOT(datetime IN (...))'", expectedRows, sqlQuery);
    }

    @Issue("4380")
    @Test(
            description = "Test whether 'HAVNG datetime IN' is supported"
    )
    public void testHavingDateInList() {
        String sqlQuery = String.format(
                "SELECT datetime FROM \"%s\" " +
                        "GROUP BY period(1 hour, 'UTC') " +
                        "HAVING datetime IN ('2017-09-01T12:00:00.000Z', '2017-10-01T12:00:00.000Z') " +
                        "ORDER BY datetime",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2017-09-01T12:00:00.000Z"},
                {"2017-10-01T12:00:00.000Z"}
        };

        assertSqlQueryRows("Wrong result for 'HAVING datetime IN (...)", expectedRows, sqlQuery);
    }
}
