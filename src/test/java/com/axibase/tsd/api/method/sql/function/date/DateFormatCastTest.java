package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class DateFormatCastTest extends SqlTest {
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(entity(), METRIC_NAME);

        series.addSamples(
                Sample.ofDateInteger("2017-04-15T12:00:00.000Z", 1),
                Sample.ofDateInteger("2017-04-20T12:00:00.000Z", 2)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("3747")
    @Test
    public void testDateFormatCast() {
        //This test may not work in some timezones
        String sqlQuery = String.format(
                "SELECT cast(date_format(time, 'M')) + cast(date_format(time, 'd')) " +
                        "FROM \"%s\"",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"24"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...))", expectedRows, sqlQuery);
    }

    @Issue("3747")
    @Test
    public void testDateFormatWithTimeZoneCast() {
        String sqlQuery = String.format(
                "SELECT cast(date_format(time, 'M', 'UTC')) + cast(date_format(time, 'd', 'UTC')) " +
                        "FROM \"%s\"",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"19"},
                {"24"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...)) with timezone", expectedRows, sqlQuery);
    }

    @Issue("3747")
    @Test
    public void testDateFormatCastInWhere() {
        String sqlQuery = String.format(
                "SELECT value " +
                        "FROM \"%s\" " +
                        "WHERE cast(date_format(time, 'd')) = 20",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"2"},
        };

        assertSqlQueryRows("Wrong result for cast(date_format(...)) inside WHERE", expectedRows, sqlQuery);
    }
}
