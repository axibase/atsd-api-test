package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.TestUtil.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DateFormatCastTest extends SqlTest {
    private static final String METRIC_NAME = TestNames.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TestNames.entity(), METRIC_NAME);

        series.addData(new Sample("2017-04-15T12:00:00.000Z", 1));
        series.addData(new Sample("2017-04-20T12:00:00.000Z", 2));

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3747
     */
    @Test
    public void testDateFormatCast() {
        //This test may not work in some timezones
        String sqlQuery = String.format(
                "SELECT cast(date_format(time, 'M')) + cast(date_format(time, 'd')) " +
                        "FROM '%s'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"24"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...))", expectedRows, sqlQuery);
    }

    /**
     * #3747
     */
    @Test
    public void testDateFormatWithTimeZoneCast() {
        String sqlQuery = String.format(
                "SELECT cast(date_format(time, 'M', 'UTC')) + cast(date_format(time, 'd', 'UTC')) " +
                        "FROM '%s'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"24"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...)) with timezone", expectedRows, sqlQuery);
    }

    /**
     * #3747
     */
    @Test
    public void testDateFormatCastInWhere() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM '%s' " +
                        "WHERE cast(date_format(time, 'd')) = 20",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...)) inside WHERE", expectedRows, sqlQuery);
    }
}
