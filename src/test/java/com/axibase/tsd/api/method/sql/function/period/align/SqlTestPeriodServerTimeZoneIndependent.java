package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class SqlTestPeriodServerTimeZoneIndependent extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series testSeries = new Series(Mocks.entity(), METRIC_NAME);
        testSeries.addSamples(new Sample("2017-03-24T23:00:00Z", 1));

        SeriesMethod.insertSeriesCheck(testSeries);
    }

    @DataProvider
    public Object[][] provideTimezoneAndResultDate() {
        return new Object[][]{
                {"America/Los_Angeles", "2017-03-24 00:00:00.000"},
                {"Etc/UTC", "2017-03-24 00:00:00.000"},
                {"Europe/Vienna", "2017-03-25 00:00:00.000"},
                {"Asia/Kathmandu", "2017-03-25 00:00:00.000"}
        };
    }

    /**
     * #4365
     */
    @Test(
            dataProvider = "provideTimezoneAndResultDate",
            description = "Test that GROUP BY PERIOD doesn't depend on ATSD instance timezone."
    )
    public void testPeriodTimezoneIndependence(String timeZoneId, String expectedDate) {
        String sqlQuery = String.format(
                "SELECT date_format(time, 'yyyy-MM-dd HH:mm:ss.SSS', '%2$s')\n" +
                        "FROM '%1$s' \n" +
                        "GROUP BY period(1 day, '%2$s')\n",
                METRIC_NAME,
                timeZoneId
        );

        String[][] expectedRows = {
                {expectedDate}
        };

        assertSqlQueryRows("Incorrect group base date in GROUP BY PERIOD with timezone " + timeZoneId,
                expectedRows, sqlQuery);
    }
}
