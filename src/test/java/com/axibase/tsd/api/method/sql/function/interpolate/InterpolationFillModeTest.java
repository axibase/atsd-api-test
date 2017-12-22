package com.axibase.tsd.api.method.sql.function.interpolate;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.function.interpolate.InterpolationParams;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class InterpolationFillModeTest extends SqlTest {
    private static final String ENTITY_NAME = entity();
    private static final String METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(ENTITY_NAME, METRIC_NAME);
        series.addSamples(Sample.ofDateInteger("2017-12-15T12:00:00.000Z", 1));
        series.addSamples(Sample.ofDateInteger("2017-12-15T13:00:00.000Z", 2));
        series.addSamples(Sample.ofDateInteger("2017-12-15T14:00:00.000Z", 3));

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode VALUE {n}," +
            "WITH INTERPOLATE(... VALUE {n} ...)")
    public void testInterpolationFillValueMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(-1.0).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "-1"},
                {"2017-12-15T11:30:00.000Z", "-1"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "-1"},
                {"2017-12-15T15:00:00.000Z", "-1"},
        };

        assertSqlQueryRows("Incorrect result with fill=VALUE -1.0 interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode NAN," +
            "WITH INTERPOLATE(... NAN ...)")
    public void testInterpolationFillValueNanMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(Double.NaN).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "NaN"},
                {"2017-12-15T11:30:00.000Z", "NaN"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "NaN"},
                {"2017-12-15T15:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect result with fill=VALUE NaN interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode VALUE NAN," +
            "WITH INTERPOLATE(... NAN ...)")
    public void testInterpolationFillNaNMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(Double.NaN).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "NaN"},
                {"2017-12-15T11:30:00.000Z", "NaN"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "NaN"},
                {"2017-12-15T15:00:00.000Z", "NaN"},
        };

        assertSqlQueryRows("Incorrect result with fill=VALUE NaN interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode NONE," +
            "WITH INTERPOLATE(... NONE ...)")
    public void testInterpolationFillNoneMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(false).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=NONE interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode NO," +
            "WITH INTERPOLATE(... NO ...)")
    public void testInterpolationFillNoMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().no().timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=NO interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode FALSE," +
            "WITH INTERPOLATE(... FALSE ...)")
    public void testInterpolationFillFalseMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(false).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=FALSE interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode EXTEND," +
            "WITH INTERPOLATE(... EXTEND ...)")
    public void testInterpolationFillExtendMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(true).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "1"},
                {"2017-12-15T11:30:00.000Z", "1"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "3"},
                {"2017-12-15T15:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=EXTEND interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode YES," +
            "WITH INTERPOLATE(... YES ...)")
    public void testInterpolationFillYesMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().yes().timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "1"},
                {"2017-12-15T11:30:00.000Z", "1"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "3"},
                {"2017-12-15T15:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=TRUE interpolation parameter", expectedRows, sqlQuery);
    }

    @Issue("4803")
    @Test(description = "Check that interpolation works with fill mode TRUE," +
            "WITH INTERPOLATE(... TRUE ...)")
    public void testInterpolationFillTrueMode() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM \"%s\" " +
                        "WHERE datetime BETWEEN '2017-12-15T11:00:00.000Z' AND '2017-12-15T15:00:00.000Z' " +
                        "WITH INTERPOLATE(%s) " +
                        "ORDER BY datetime",
                METRIC_NAME,
                new InterpolationParams(30, TimeUnit.MINUTE).linear().fill(true).timeZone("UTC")
        );

        String[][] expectedRows = {
                {"2017-12-15T11:00:00.000Z", "1"},
                {"2017-12-15T11:30:00.000Z", "1"},
                {"2017-12-15T12:00:00.000Z", "1"},
                {"2017-12-15T12:30:00.000Z", "1.5"},
                {"2017-12-15T13:00:00.000Z", "2"},
                {"2017-12-15T13:30:00.000Z", "2.5"},
                {"2017-12-15T14:00:00.000Z", "3"},
                {"2017-12-15T14:30:00.000Z", "3"},
                {"2017-12-15T15:00:00.000Z", "3"},
        };

        assertSqlQueryRows("Incorrect result with fill=TRUE interpolation parameter", expectedRows, sqlQuery);
    }
}
