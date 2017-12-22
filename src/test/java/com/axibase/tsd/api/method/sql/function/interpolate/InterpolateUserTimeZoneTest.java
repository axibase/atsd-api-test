package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class InterpolateUserTimeZoneTest extends SqlTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();

    private static final int FIVE_HOURS = 5 * 60 * 60 * 1000;

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(ENTITY_NAME, METRIC_NAME);
        series.addSamples(
                Sample.ofDateInteger("2017-10-11T05:00:00.000Z", 1),
                Sample.ofDateInteger("2017-10-12T05:00:00.000Z", 2)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    private static final String[] timeZoneList = {
            "America/Los_Angeles",
            "Europe/London",
            "Europe/Vienna",
            "Europe/Moscow",
            "Asia/Kathmandu",
            "Asia/Kolkata",
            "Pacific/Chatham",
            "Pacific/Kiritimati",
            "Etc/UTC",
    };

    @DataProvider
    public Object[][] provideTimezoneList() {
        List<Object[]> cases = new ArrayList<>();
        for (String timeZone : timeZoneList) {
            cases.add(new Object[]{timeZone});
        }
        Object[][] result = new Object[cases.size()][];
        cases.toArray(result);
        return result;
    }

    @Issue("4103")
    @Test(
            description = "Check that interpolated date is aligned correctly in different timezones",
            dataProvider = "provideTimezoneList"
    )
    public void testInterpolateTimezoneAlign(String timeZoneId) throws ParseException {
        String sqlQuery = String.format(
                "SELECT date_format(datetime, 'yy-MM-dd HH:mm:ss', '%2$s') " +
                        "FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN '2017-10-11T05:00:00.000Z' " +
                        "AND '2017-10-12T05:00:00.000Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, FALSE, CALENDAR, '%2$s')",
                METRIC_NAME,
                timeZoneId
        );

        long testDate = new SimpleDateFormat("yy-MM-dd").parse("2017-10-11").getTime();

        String expectedDate =
                TimeZone.getTimeZone(timeZoneId).getOffset(testDate) < -FIVE_HOURS ?
                "17-10-11 00:00:00" : "17-10-12 00:00:00";

        String[][] expectedRows = {
                {expectedDate}
        };

        assertSqlQueryRows("Incorrect interpolation alignment for timezone " + timeZoneId, expectedRows, sqlQuery);
    }

    @Issue("4103")
    @Test(
            description = "Check that interpolation works with timezone=null. " +
                    "In this case, server timezone should be applied"
    )
    public void testInterpolateServerTimezoneAlign() throws ParseException {
        TimeZone timeZone = Util.getServerTimeZone();

        /* Here metric.timezone=null, INTERPOLATE should default to server timezone */
        String sqlQuery = String.format(
                "SELECT date_format(datetime, 'yy-MM-dd HH:mm:ss', '%s') " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-10-11T05:00:00.000Z' " +
                        "AND '2017-10-12T05:00:00.000Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, FALSE, CALENDAR, metric.timezone)",
                timeZone.getID(),
                METRIC_NAME
        );

        long testDate = new SimpleDateFormat("yy-MM-dd").parse("2017-10-11").getTime();

        String expectedDate =
                timeZone.getOffset(testDate) < -FIVE_HOURS ? "17-10-11 00:00:00" : "17-10-12 00:00:00";

        String[][] expectedRows = {
                {expectedDate}
        };

        assertSqlQueryRows("Incorrect interpolation alignment for server timezone", expectedRows, sqlQuery);
    }

    @Issue("4103")
    @Test(
            description = "Check that interpolation works with DST or TZ change"
    )
    public void testInterpolateTimezoneDST() throws Exception {
        String entityName = Mocks.entity();
        String metricName = Mocks.metric();
        Series series = new Series(entityName, metricName);
        series.addSamples(
                Sample.ofDateInteger("2011-03-26T21:00:00.000Z", 1),
                Sample.ofDateInteger("2011-03-27T22:00:00.000Z", 2)
        );

        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT date_format(datetime, 'yy-MM-dd HH:mm:ss', 'Europe/Moscow'), value " +
                        "FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2011-03-26T21:00:00.000Z' " +
                        "AND '2011-03-27T21:00:00.000Z' " +
                        "WITH INTERPOLATE(1 DAY, LINEAR, OUTER, FALSE, CALENDAR, 'Europe/Moscow')",
                metricName
        );

        String[][] expectedRows = {
                {"11-03-27 00:00:00", "1.00"},
                {"11-03-28 00:00:00", "1.92"}
        };

        assertSqlQueryRows("Incorrect interpolation with offset changes for timezone", expectedRows, sqlQuery);
    }

    @Issue("4103")
    @Test(
            description = "Check that metric.timezone can be used for interpolation"
    )
    public void testInterpolateMetricTimezone() throws Exception {
        String entityName = Mocks.entity();
        String metricName = Mocks.metric();

        Metric metric = new Metric(metricName);
        metric.setTimeZoneID("Asia/Kolkata");
        Series series = new Series(entityName, metricName);
        series.addSamples(
                Sample.ofDateInteger("2017-10-11T05:22:43.573Z", 1),
                Sample.ofDateInteger("2017-10-12T05:22:43.573Z", 2)
        );

        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT date_format(datetime, 'yy-MM-dd HH:mm:ss', metric.timezone) " +
                        "FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN '2017-10-11T05:00:00.000Z' " +
                        "AND '2017-10-12T05:00:00.000Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, FALSE, CALENDAR, metric.timezone)",
                metricName
        );

        String[][] expectedRows = {
                {"17-10-12 00:00:00"}
        };

        assertSqlQueryRows("Incorrect interpolation when using metric.timezone as custom timezone",
                expectedRows, sqlQuery);
    }

    @Issue("4103")
    @Test(
            description = "Check that entity.timezone can be used for interpolation"
    )
    public void testInterpolateEntityTimezone() throws Exception {
        String entityName = Mocks.entity();
        String metricName = Mocks.metric();

        Entity entity = new Entity(entityName);
        entity.setTimeZoneID("America/Los_Angeles");
        Series series = new Series(entityName, metricName);
        series.addSamples(
                Sample.ofDateInteger("2017-10-11T05:22:43.573Z", 1),
                Sample.ofDateInteger("2017-10-12T05:22:43.573Z", 2)
        );

        EntityMethod.createOrReplaceEntityCheck(entity);
        SeriesMethod.insertSeriesCheck(series);

        String sqlQuery = String.format(
                "SELECT date_format(datetime, 'yy-MM-dd HH:mm:ss', entity.timezone) " +
                        "FROM \"%1$s\" " +
                        "WHERE datetime BETWEEN '2017-10-11T05:00:00.000Z' " +
                        "AND '2017-10-12T05:00:00.000Z' " +
                        "WITH INTERPOLATE(1 DAY, PREVIOUS, OUTER, FALSE, CALENDAR, entity.timezone)",
                metricName
        );

        String[][] expectedRows = {
                {"17-10-11 00:00:00"}
        };

        assertSqlQueryRows("Incorrect interpolation when using entity.timezone as custom timezone",
                expectedRows, sqlQuery);
    }
}
