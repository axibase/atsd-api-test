package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WithInterpolateNoBoundariesTest extends SqlTest {
    private static String metricName;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = Mocks.series();
        metricName = series.getMetric();

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4120")
    @Test
    public void testWithInterpolateNoBoundaries() {
        /*
            If there are no date limits by WHERE statement,
            then interpolation should be limited by min/max time in scan
         */
        String sqlQuery = String.format(
                "SELECT count(*) " +
                        "FROM \"%s\" " +
                        "WITH INTERPOLATE(1 MINUTE, PREVIOUS) " +
                        "GROUP BY entity",
                metricName
        );

        String[][] expectedRows = {
                {"2"}
        };

        assertSqlQueryRows("Incorrect result for WITH INTERPOLATE without data boundaries",
                expectedRows, sqlQuery);
    }
}
