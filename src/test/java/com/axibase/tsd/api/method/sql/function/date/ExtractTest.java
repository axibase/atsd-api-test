package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.function.Function;

public class ExtractTest extends SqlTest {
    private static final String DATE_STRING_A = "2016-06-01T15:04:03.002Z";
    private static final String DATE_STRING_B = "2017-07-02T13:06:01.004Z";
    private static ZonedDateTime dateA = zonedFromStringDate(DATE_STRING_A);
    private static ZonedDateTime dateB = zonedFromStringDate(DATE_STRING_B);
    private static final String METRIC_NAME = Mocks.metric();

    private static ZonedDateTime zonedFromStringDate(String date) {
        try {
            return ZonedDateTime.ofInstant(Instant.parse(date), TestUtil.getServerTimeZone().toZoneId());
        } catch (JSONException e) {
            return null;
        }
    }

    private enum DateAccessor {
        GET_YEAR(date -> String.valueOf(date.getYear())),
        GET_QUARTER(date -> String.valueOf(date.getMonth().ordinal() / 3 + 1)),
        GET_MONTH(date -> String.valueOf(date.getMonth().getValue())),
        GET_DAY(date -> String.valueOf(date.getDayOfMonth())),
        GET_DAYOFWEEK(date -> String.valueOf(date.getDayOfWeek().getValue())),
        GET_HOUR(date -> String.valueOf(date.getHour())),
        GET_MINUTE(date -> String.valueOf(date.getMinute())),
        GET_SECOND(date -> String.valueOf(date.getSecond()));

        Function<ZonedDateTime, String> accessFunction;
        DateAccessor(Function<ZonedDateTime, String> accessFunction) {
            this.accessFunction = accessFunction;
        }

        String apply(ZonedDateTime date) {
            return accessFunction.apply(date);
        }
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                new Sample(DATE_STRING_A, 1),
                new Sample(DATE_STRING_B, 2)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideDatePartNameAndAccessor() {
        Object[][] partsAccessors = new Object[][] {
                {"year", DateAccessor.GET_YEAR},
                {"quarter", DateAccessor.GET_QUARTER},
                {"month", DateAccessor.GET_MONTH},
                {"day", DateAccessor.GET_DAY},
                {"dayofweek", DateAccessor.GET_DAYOFWEEK},
                {"hour", DateAccessor.GET_HOUR},
                {"minute", DateAccessor.GET_MINUTE},
                {"second", DateAccessor.GET_SECOND}
        };

        Object[][] extractionSources = new Object[][] {
                {"time"}, {"datetime"}
        };

        Object[][] testData = new Object[partsAccessors.length * extractionSources.length][];
        for (int i = 0; i < partsAccessors.length; i++) {
            for (int j = 0; j < extractionSources.length; j++) {
                testData[i * extractionSources.length + j] = new Object[] {
                        extractionSources[j][0], partsAccessors[i][0], partsAccessors[i][1]
                };
            }
        }
        return testData;
    }

    /**
     * #4393
     */
    @Test(dataProvider = "provideDatePartNameAndAccessor")
    public void testExtractFrom(String source, String part, DateAccessor accessFunction) {
        String sqlQuery = String.format(
                "SELECT extract(%2$s FROM %1$s) " +
                        "FROM \"%3$s\"",
                source, part, METRIC_NAME
        );

        String[][] expectedRows = {
                {accessFunction.apply(dateA)},
                {accessFunction.apply(dateB)},
        };

        assertSqlQueryRows("Wrong result for extract function from datetime", expectedRows, sqlQuery);
    }

    /**
     * #4393
     */
    @Test(dataProvider = "provideDatePartNameAndAccessor")
    public void testIndividualExtractFunctions(String source, String part, DateAccessor accessFunction) {
        String sqlQuery = String.format(
                "SELECT %2$s(%1$s) " +
                        "FROM \"%3$s\"",
                source, part, METRIC_NAME
        );

        String[][] expectedRows = {
                {accessFunction.apply(dateA)},
                {accessFunction.apply(dateB)},
        };

        assertSqlQueryRows("Wrong result for extract function from time", expectedRows, sqlQuery);
    }

    /**
     * #4393
     */
    @Test(dataProvider = "provideDatePartNameAndAccessor")
    public void testWhereExtractIn(String source, String part, DateAccessor accessFunction) {
        String extractedValue = accessFunction.apply(dateA);

        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%3$s\" " +
                        "WHERE extract(%2$s FROM %1$s) IN (%4$s, 2018, 0, 'a')",
                source, part, METRIC_NAME, extractedValue
        );

        String[][] expectedRows = {
                {DATE_STRING_A, "1"}
        };

        assertSqlQueryRows("Wrong when using WHERE extract(...) IN (...)", expectedRows, sqlQuery);
    }

    /**
     * #4393
     */
    @Test(dataProvider = "provideDatePartNameAndAccessor")
    public void testWhereIndividualExtractIn(String source, String part, DateAccessor accessFunction) {
        String extractedValue = accessFunction.apply(dateA);

        String sqlQuery = String.format(
                "SELECT datetime, value " +
                        "FROM \"%3$s\" " +
                        "WHERE %2$s(%1$s) IN (%4$s, 2018, 0, 'a')",
                source, part, METRIC_NAME, extractedValue
        );

        String[][] expectedRows = {
                {DATE_STRING_A, "1"}
        };

        String assertMessage = String.format("Wrong when using WHERE %s(...) IN (...)", part);
        assertSqlQueryRows(assertMessage, expectedRows, sqlQuery);
    }
}
