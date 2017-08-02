package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SqlSyntaxLocalFormatTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();
    private static final String NANO_SUFFIX = ".123456789";

    //@BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                new Sample("2017-01-01T00:00:00.997Z", 1),
                new Sample("2017-01-01T00:00:00.998Z", 2),
                new Sample("2017-01-01T00:00:00.999Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideSecondsFractionsForTime() {
        Object[][] result = new Object[NANO_SUFFIX.length()][];
        result[0] = new String[]{""};
        for (int i = 1; i < NANO_SUFFIX.length(); i++) {
            result[i] = new String[]{NANO_SUFFIX.substring(0, i)};
        }
        return result;
    }

    @Test
    public void testOk() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM '%1$s' " +
                        "WHERE (datetime > '%2$s') " +
                        "AND (datetime < '%3$s')",
                METRIC_NAME,
                TestUtil.formatAsLocalTime("2017-01-01T00:00:00.997Z") + "8",
                TestUtil.formatAsLocalTime("2017-01-01T00:00:00.999Z") + "9"
        );

        String[][] expectedRows = {
                {"2017-01-01T00:00:00.998Z"}
        };

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }
}
