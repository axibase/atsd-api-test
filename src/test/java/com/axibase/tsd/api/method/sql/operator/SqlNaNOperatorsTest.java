package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlNaNOperatorsTest extends SqlTest {
    private static String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(entity(), TEST_METRIC);
        series.addSamples(Sample.ofDateText(Mocks.ISO_TIME, "text"));
        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(name = "operatorsProvider")
    public Object[][] provideTestOperators() {
        return new Object[][] {
                {"value",                       "NaN"},
                {"value IS NULL",               "true"},
                {"value IS NOT NULL",           "false"},
                {"ISNULL(value, '-')",          "-"},

                {"ABS(value)",                  "NaN"},
                {"NOT ABS(value) = 1",          "null"},
                {"CEIL(value)",                 "NaN"},
                {"NOT CEIL(value) = 1",         "null"},
                {"FLOOR(value)",                "NaN"},
                {"NOT FLOOR(value) = 1",        "null"},
                {"ROUND(value)",                "NaN"},
                {"NOT ROUND(value) = 1",        "null"},
                {"ROUND(value, 0)",             "NaN"},
                {"NOT ROUND(value, 0) = 1",     "null"},
                {"MOD(value, 2)",               "NaN"},
                {"NOT MOD(value, 2) = 0",       "null"},
                {"EXP(value)",                  "NaN"},
                {"NOT EXP(value) = 1",          "null"},
                {"LN(value)",                   "NaN"},
                {"NOT LN(value) = 1",           "null"},
                {"POWER(value, 2)",             "NaN"},
                {"NOT POWER(value, 2) = 1",     "null"},
                {"LOG(value, 8)",               "NaN"},
                {"NOT LOG(value, 8) = 3",       "null"},
                {"SQRT(value)",                 "NaN"},
                {"NOT SQRT(value) = 1",         "null"},

                {"CAST(value AS STRING)",       "null"},
                {"NOT CAST(value AS STRING) = 'val'", "null"},

                {"date_format(value)",          "null"},
                {"NOT date_format(value) = '1970-01-01T00:00:00.001Z'", "null"},

                {"value = 1",                   "null"},
                {"NOT value = 1",               "null"},
                {"value != 1",                  "null"},
                {"NOT value != 1",              "null"},
                {"value > 1",                   "null"},
                {"NOT value > 1",               "null"},
                {"value >= 1",                  "null"},
                {"NOT value >= 1",              "null"},
                {"value < 1",                   "null"},
                {"NOT value < 1",               "null"},
                {"value <= 1",                  "null"},
                {"NOT value <= 1",              "null"},
                {"(value = 1) OR (value > 3)",  "null"},
                {"(value != 1) AND (value > 3)",    "null"}
        };
    }

    @Issue("4247")
    @Test(dataProvider = "operatorsProvider")
    public void testOperatorsWithNaN(String operator, String expectedResult) {
        String sqlQuery = String.format("SELECT %s FROM \"%s\"", operator, TEST_METRIC);

        String[][] expectedRows = new String[][] {{expectedResult}};

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
