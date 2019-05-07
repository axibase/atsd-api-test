package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.ZonedDateTimeComparator;
import com.google.common.collect.ImmutableMap;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Predicate;

import static com.axibase.tsd.api.util.Util.parseAsServerZoned;

public class SqlShortTimeTest extends SqlTest {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String ANNUAL_METRIC_NAME = Mocks.metric();
    private static final String MONTHLY_METRIC_NAME = Mocks.metric();
    private static final String DAILY_METRIC_NAME = Mocks.metric();
    private static Map<String, Series> seriesMetricMap;

    @BeforeTest
    public static void prepareData() throws Exception {
        final Series annualSeries = new Series(ENTITY_NAME, ANNUAL_METRIC_NAME).addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00.000Z", 1),
                Sample.ofDateInteger("2017-01-01T00:00:00.001Z", 2),
                Sample.ofDateInteger("2017-12-31T23:59:59.999Z", 3),
                Sample.ofDateInteger("2018-01-01T00:00:00.000Z", 4),
                Sample.ofDateInteger("2018-01-01T00:00:00.001Z", 5),
                Sample.ofDateInteger("2018-01-01T00:00:00.002Z", 6),
                Sample.ofDateInteger("2019-01-01T00:00:00.000Z", 7),
                Sample.ofDateInteger("2020-01-01T00:00:00.000Z", 8)
        );

        final Series monthlySeries = new Series(ENTITY_NAME, MONTHLY_METRIC_NAME).addSamples(
                Sample.ofDateInteger("2018-01-01T00:00:00.000Z", 1),
                Sample.ofDateInteger("2018-01-01T00:00:00.001Z", 2),
                Sample.ofDateInteger("2018-01-31T23:59:59.999Z", 3),
                Sample.ofDateInteger("2018-02-01T00:00:00.000Z", 4),
                Sample.ofDateInteger("2018-02-01T00:00:00.001Z", 5),
                Sample.ofDateInteger("2018-02-01T00:00:00.002Z", 6),
                Sample.ofDateInteger("2018-03-01T00:00:00.000Z", 7),
                Sample.ofDateInteger("2018-04-01T00:00:00.000Z", 8)
        );

        final Series dailySeries = new Series(ENTITY_NAME, DAILY_METRIC_NAME).addSamples(
                Sample.ofDateInteger("2018-01-01T00:00:00.000Z", 1),
                Sample.ofDateInteger("2018-01-01T00:00:00.001Z", 2),
                Sample.ofDateInteger("2018-01-01T23:59:59.999Z", 3),
                Sample.ofDateInteger("2018-01-02T00:00:00.000Z", 4),
                Sample.ofDateInteger("2018-01-02T00:00:00.001Z", 5),
                Sample.ofDateInteger("2018-01-02T00:00:00.002Z", 6),
                Sample.ofDateInteger("2018-01-03T00:00:00.000Z", 7),
                Sample.ofDateInteger("2018-01-04T00:00:00.000Z", 8)
        );

        seriesMetricMap = ImmutableMap.of(
                ANNUAL_METRIC_NAME, annualSeries,
                MONTHLY_METRIC_NAME, monthlySeries,
                DAILY_METRIC_NAME, dailySeries
        );
        SeriesMethod.insertSeriesCheck(seriesMetricMap.values());
    }

    @DataProvider
    public static Object[][] provideData() {
        return new Object[][]{
                {
                        ANNUAL_METRIC_NAME, "datetime >= '2018'",
                        FilterFactory.greaterOrEqual("2018-01-01T00:00:00")
                },
                {
                        ANNUAL_METRIC_NAME, "datetime = '2018'",
                        FilterFactory.equal("2018-01-01T00:00:00")
                },
                {
                        ANNUAL_METRIC_NAME, "datetime <= '2018'",
                        FilterFactory.fewerOrEqual("2018-01-01T00:00:00")
                },
                {
                        ANNUAL_METRIC_NAME, "datetime between '2018' and '2019'",
                        FilterFactory.between("2018-01-01T00:00:00", "2019-01-01T00:00:00")
                },

                {
                        MONTHLY_METRIC_NAME, "datetime >= '2018-02'",
                        FilterFactory.greaterOrEqual("2018-02-01T00:00:00")
                },
                {
                        MONTHLY_METRIC_NAME, "datetime = '2018-02'",
                        FilterFactory.equal("2018-02-01T00:00:00")
                },
                {
                        MONTHLY_METRIC_NAME, "datetime <= '2018-02'",
                        FilterFactory.fewerOrEqual("2018-02-01T00:00:00")
                },
                {
                        MONTHLY_METRIC_NAME, "datetime between '2018-02' and '2018-03'",
                        FilterFactory.between("2018-02-01T00:00:00", "2018-03-01T00:00:00")
                },

                {
                        DAILY_METRIC_NAME, "datetime >= '2018-01-02'",
                        FilterFactory.greaterOrEqual("2018-01-02T00:00:00")
                },
                {
                        DAILY_METRIC_NAME, "datetime = '2018-01-02'",
                        FilterFactory.equal("2018-01-02T00:00:00")
                },
                {
                        DAILY_METRIC_NAME, "datetime <= '2018-01-02'",
                        FilterFactory.fewerOrEqual("2018-01-02T00:00:00")
                },
                {
                        DAILY_METRIC_NAME, "datetime between '2018-01-02' and '2018-01-03'",
                        FilterFactory.between("2018-01-02T00:00:00", "2018-01-03T00:00:00")
                }
        };
    }

    private static String[][] filterSeries(final Series series, final Predicate<Sample> predicate) {
        return series.getData().stream()
                .filter(predicate)
                .map(sample -> new String[]{sample.getValue().toString()})
                .toArray(String[][]::new);
    }

    @Issue("5150")
    @Test(dataProvider = "provideData")
    public void testShortTimeFormat(final String metricName, final String predicate, final Predicate<Sample> filter) {
        // arrange
        final String sqlQuery = String.format("SELECT value FROM \"%s\" WHERE %s", metricName, predicate);
        final Series series = seriesMetricMap.get(metricName);

        // action
        final String[][] expectedRows = filterSeries(series, filter);

        // assert
        final String assertMessage = String.format(
                "Incorrect data after filtering metric '%s' with query '%s' with short time format in WHERE clause",
                metricName, sqlQuery
        );
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }

    private static final class FilterFactory {
        private static final Comparator<ZonedDateTime> COMPARATOR = new ZonedDateTimeComparator();

        private static Predicate<Sample> filter(final String dateString, final Predicate<Integer> predicate) {
            final ZonedDateTime compareDate = parseAsServerZoned(dateString);
            return sample -> predicate.test(COMPARATOR.compare(sample.getZonedDateTime(), compareDate));
        }

        private static Predicate<Sample> greaterOrEqual(final String dateString) {
            return filter(dateString, i -> i >= 0);
        }

        private static Predicate<Sample> fewerOrEqual(final String dateString) {
            return filter(dateString, i -> i <= 0);
        }

        private static Predicate<Sample> equal(final String dateString) {
            return filter(dateString, i -> i == 0);
        }

        private static Predicate<Sample> between(final String dateStringA, final String dateStringB) {
            return greaterOrEqual(dateStringA).and(fewerOrEqual(dateStringB));
        }
    }
}
