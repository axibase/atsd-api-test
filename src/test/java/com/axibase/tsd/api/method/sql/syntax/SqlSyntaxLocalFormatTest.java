package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SqlSyntaxLocalFormatTest extends SqlTest {
    private static final String METRIC_NAME = Mocks.metric();

    /**
     * Remove characters from the tail of a string
     *
     * @param value      string to remove characters from
     * @param tailLength amount of characters to remove
     * @return truncated string
     */
    private String stringTruncate(String value, int tailLength) {
        return value.substring(0, value.length() - tailLength);
    }

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T00:00:00.997Z", 1),
                Sample.ofDateInteger("2017-01-01T00:00:00.998Z", 2),
                Sample.ofDateInteger("2017-01-01T00:00:00.999Z", 3)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that dates with different digits " +
                    "after 3rd fractional place are indistinguishable in local " +
                    "date format"
    )
    public void testLocalDateFractionalPartTruncationTo3() {
        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" " +
                        "WHERE (datetime > '%s') " +
                        "AND (datetime < '%s')",
                METRIC_NAME,
                TestUtil.formatAsLocalTime("2017-01-01T00:00:00.999Z") + "8",
                TestUtil.formatAsLocalTime("2017-01-01T00:00:00.999Z") + "9"
        );

        assertBadRequest("Wrong result when comparing dates with different digit after 3rd fractional ms position",
                "Start date must be less than end date " +
                        "at line 1 position 122 near \"'2017-01-01 05:45:00.9998'\"", sqlQuery);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that fractions with less than " +
                    "three decimal places aren't supported for dates in local " +
                    "date format"
    )
    public void testLocalDateLessThanThreeFractionalDigits() {
        String beginDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:00.998Z"), 1);
        String endDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:01.000Z"), 2);

        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" " +
                        "WHERE (datetime > '%s') " +
                        "AND (datetime < '%s')",
                METRIC_NAME,
                beginDate,
                endDate
        );

        assertBadRequest("Wrong result for dates in local format with less than 3 digits fractional part",
                String.format("Invalid date value: '%s' at line 1 position 122 near \"'%s'\"", beginDate, beginDate), sqlQuery);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that parsing works for fractions " +
                    "with exactly three decimal places for dates in local " +
                    "date format"
    )
    public void testLocalDateThreeFractionalDigits() {
        String beginDate = TestUtil.formatAsLocalTime("2017-01-01T00:00:00.998Z");
        String endDate = TestUtil.formatAsLocalTime("2017-01-01T00:00:01.000Z");

        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" " +
                        "WHERE (datetime > '%s') " +
                        "AND (datetime < '%s')",
                METRIC_NAME,
                beginDate,
                endDate
        );

        String[][] expectedResult = {
                {"2017-01-01T00:00:00.999Z"}
        };

        assertSqlQueryRows("Wrong result for dates in local format with 3 digits fractional part",
                expectedResult, sqlQuery);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that parsing works for fractions " +
                    "up to 9th decimal place (nanoseconds) for dates in local " +
                    "date format"
    )
    public void testLocalDateNineFractionalDigits() {
        String beginDate = TestUtil.formatAsLocalTime("2017-01-01T00:00:00.998Z") + "000001";
        String endDate = TestUtil.formatAsLocalTime("2017-01-01T00:00:01.000Z") + "999999";

        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" " +
                        "WHERE (datetime > '%s') " +
                        "AND (datetime < '%s')",
                METRIC_NAME,
                beginDate,
                endDate
        );

        String[][] expectedResult = {
                {"2017-01-01T00:00:00.999Z"}
        };

        assertSqlQueryRows("Wrong result for dates in local format with 9 digits fractional part",
                expectedResult, sqlQuery);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that parsing works if fractional " +
                    "is not present, for dates in local date format"
    )
    public void testLocalDateNoFractionalDigits() {
        String beginDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:00.000Z"), 4);
        String endDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:01.000Z"), 4);

        String sqlQuery = String.format(
                "SELECT datetime " +
                        "FROM \"%s\" " +
                        "WHERE (datetime > '%s') " +
                        "AND (datetime < '%s')",
                METRIC_NAME,
                beginDate,
                endDate
        );

        String[][] expectedResult = {
                {"2017-01-01T00:00:00.997Z"},
                {"2017-01-01T00:00:00.998Z"},
                {"2017-01-01T00:00:00.999Z"}
        };

        assertSqlQueryRows("Wrong result for dates in local format without fractional part",
                expectedResult, sqlQuery);
    }

    @Issue("4386")
    @Test(
            description = "The test asserts that parsing works if fractional " +
                    "is not present, for dates in local date format. Use time column"
    )
    public void testLocalDateNoFractionalDigitsWithTime() {
        String beginDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:00.000Z"), 4);
        String endDate = stringTruncate(TestUtil.formatAsLocalTime("2017-01-01T00:00:01.000Z"), 4);

        String sqlQuery = String.format(
                "SELECT time " +
                        "FROM \"%s\" " +
                        "WHERE (time > '%s') " +
                        "AND (time < '%s')",
                METRIC_NAME,
                beginDate,
                endDate
        );

        String[][] expectedResult = {
                {"1483228800997"},
                {"1483228800998"},
                {"1483228800999"}
        };

        assertSqlQueryRows("Wrong result for dates in local format without fractional part (time column)",
                expectedResult, sqlQuery);
    }
}
