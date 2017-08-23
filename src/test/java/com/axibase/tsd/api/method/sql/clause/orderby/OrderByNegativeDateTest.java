package com.axibase.tsd.api.method.sql.clause.orderby;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OrderByNegativeDateTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                new Sample("2000-01-01T00:00:00.000Z", 4),
                new Sample("1980-01-01T00:00:00.000Z", 2),
                new Sample("1970-01-01T00:00:00.000Z", 1),
                new Sample("1990-01-01T00:00:00.000Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * 4307
     */
    @Test(
            description = "Test that values with negative date appear on the right postion " +
                    "when sorting is done in ascending order"
    )
    public void testOrderByNegativeDateAscending() {
        String sqlQuery = String.format(
                "SELECT min(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY period(10 year) " +
                        "ORDER BY datetime ASC",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"1"},
                {"2"},
                {"3"},
                {"4"}
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    /**
     * 4307
     */
    @Test(
            description = "Test that values with negative date appear on the right postion " +
                    "when sorting is done in descending order"
    )
    public void testOrderByNegativeDateDescending() {
        String sqlQuery = String.format(
                "SELECT min(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY period(10 year) " +
                        "ORDER BY datetime DESC",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"4"},
                {"3"},
                {"2"},
                {"1"}
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }
}
