package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public class TimeZoneFieldAsArgumentTest extends SqlTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME1 = Mocks.metric();
    private static final String METRIC_NAME2 = Mocks.metric();
    private static final String METRIC_NAME3 = Mocks.metric();

    private static final TimeZone ENTITY_ZONE = TimeZone.getTimeZone("Pacific/Chatham");
    private static final TimeZone METRIC_ZONE = TimeZone.getTimeZone("America/Los_Angeles");

    @BeforeClass
    public static void prepareData() throws Exception {
        Entity entity = new Entity(ENTITY_NAME);
        Metric metric1 = new Metric(METRIC_NAME1);
        Metric metric2 = new Metric(METRIC_NAME2);
        Metric metric3 = new Metric(METRIC_NAME3);

        Series series1 = new Series(ENTITY_NAME, METRIC_NAME1);
        series1.addSamples(
                Sample.ofDateInteger("2017-08-01T12:00:00.000Z", 1)
        );

        Series series2 = new Series(ENTITY_NAME, METRIC_NAME2);
        series2.addSamples(
                Sample.ofDateInteger("2017-08-01T04:00:00.000Z", 1),
                Sample.ofDateInteger("2017-08-01T12:00:00.000Z", 2),
                Sample.ofDateInteger("2017-08-01T20:00:00.000Z", 3),
                Sample.ofDateInteger("2017-08-02T03:00:00.000Z", 4)
        );

        Series series3 = new Series(ENTITY_NAME, METRIC_NAME3);
        series3.addSamples(
                Sample.ofDateInteger("2017-08-01T04:00:00.000Z", 1),
                Sample.ofDateInteger("2017-08-01T07:00:00.000Z", 4)
        );

        entity.setTimeZoneID(ENTITY_ZONE.getID());
        metric1.setTimeZoneID(METRIC_ZONE.getID());
        metric2.setTimeZoneID(METRIC_ZONE.getID());
        metric3.setTimeZoneID(METRIC_ZONE.getID());

        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric1);
        MetricMethod.createOrReplaceMetricCheck(metric2);
        MetricMethod.createOrReplaceMetricCheck(metric3);
        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    @Issue("4147")
    @Test(
            description = "Test if date_format correctly supports timezone field of entity/metric"
    )
    public void testDateFormatFromZoneField() {
        String sqlQuery = String.format(
                "SELECT " +
                        "date_format(time, 'yyyy-MM-dd HH:mm:ss', entity.timezone), " +
                        "date_format(time, 'yyyy-MM-dd HH:mm:ss', metric.timezone) " +
                        "FROM \"%s\"",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {"2017-08-02 00:45:00", "2017-08-01 05:00:00"}
        };

        assertSqlQueryRows("Wrong result when specifying timezone from metric/entity timezone field in date_format",
                expectedRows, sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if endtime correctly supports timezone field of entity/metric"
    )
    public void testEndtimeFromZoneFiled() {
        String sqlQuery = String.format(
                "SELECT " +
                        "now, " +
                        "endtime(current_day, entity.timezone), " +
                        "endtime(current_day, metric.timezone) " +
                        "FROM \"%s\"",
                METRIC_NAME1
        );

        StringTable resultTable = queryTable(sqlQuery);

        long now = Long.parseLong(resultTable.getValueAt(0, 0));
        long currentDayEntity = TestUtil.truncateTime(now, ENTITY_ZONE, ChronoUnit.DAYS);
        long currentDayMetric = TestUtil.truncateTime(now, METRIC_ZONE, ChronoUnit.DAYS);

        String[][] expectedRows = {
                {String.valueOf(now), String.valueOf(currentDayEntity), String.valueOf(currentDayMetric)}
        };

        assertRowsMatch("Wrong result when specifying timezone from metric/entity timezone field in endtime",
                expectedRows, resultTable, sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if GROUP BY PERIOD doesn't allow entity.timezone without grouping by entity column"
    )
    public void testGroupByPeriodEntityZoneFieldNoEntityColumn() {
        String sqlQuery = String.format(
                "SELECT " +
                        "datetime, sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY period(1 day, entity.timezone)",
                METRIC_NAME2
        );

        assertBadSqlRequest("entity.timezone in period function requires grouping by entity." +
                " Include \"entity\" column in the GROUP BY clause. Error at line 1 position 150 near \"entity\"", sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if GROUP BY PERIOD correctly supports timezone field of entity"
    )
    public void testGroupByPeriodEntityZoneField() {
        String sqlQuery = String.format(
                "SELECT " +
                        "datetime, sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity, period(1 day, entity.timezone)",
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {"2017-07-31T11:15:00.000Z", "1"},
                {"2017-08-01T11:15:00.000Z", "9"},
        };

        assertSqlQueryRows("Wrong result when specifying timezone from entity.timezone field in GROUP BY PERIOD",
                expectedRows, sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if GROUP BY PERIOD correctly supports timezone field of metric"
    )
    public void testGroupByPeriodMetricZoneField() {
        String sqlQuery = String.format(
                "SELECT " +
                        "datetime, sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY period(1 day, metric.timezone)",
                METRIC_NAME2
        );

        String[][] expectedRows = {
                {"2017-07-31T07:00:00.000Z", "1"},
                {"2017-08-01T07:00:00.000Z", "9"},
        };

        assertSqlQueryRows("Wrong result when specifying timezone from metric.timezone field in GROUP BY PERIOD",
                expectedRows, sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if WITH INTERPOLATE correctly supports timezone field of entity"
    )
    public void testWithInterpolateEntityZoneField() {
        String sqlQuery = String.format(
                "SELECT " +
                        "datetime, value " +
                        "FROM \"%s\" " +
                        "WITH INTERPOLATE(1 HOUR, LINEAR, INNER, FALSE, CALENDAR, entity.timezone)",
                METRIC_NAME3
        );

        String[][] expectedRows = {
                {"2017-08-01T04:15:00.000Z", "1.25"},
                {"2017-08-01T05:15:00.000Z", "2.25"},
                {"2017-08-01T06:15:00.000Z", "3.25"}
        };

        assertSqlQueryRows("Wrong result when specifying timezone from entity.timezone field in WITH INTERPOLATE",
                expectedRows, sqlQuery);
    }

    @Issue("4147")
    @Test(
            description = "Test if WITH INTERPOLATE correctly supports timezone field of metric"
    )
    public void testWithInterpolateMetricZoneField() {
        String sqlQuery = String.format(
                "SELECT " +
                        "datetime, value " +
                        "FROM \"%s\" " +
                        "WITH INTERPOLATE(1 HOUR, LINEAR, INNER, FALSE, CALENDAR, metric.timezone)",
                METRIC_NAME3
        );

        String[][] expectedRows = {
                {"2017-08-01T04:00:00.000Z", "1"},
                {"2017-08-01T05:00:00.000Z", "2"},
                {"2017-08-01T06:00:00.000Z", "3"},
                {"2017-08-01T07:00:00.000Z", "4"}
        };

        assertSqlQueryRows("Wrong result when specifying timezone from metric.timezone field in WITH INTERPOLATE",
                expectedRows, sqlQuery);
    }
}
