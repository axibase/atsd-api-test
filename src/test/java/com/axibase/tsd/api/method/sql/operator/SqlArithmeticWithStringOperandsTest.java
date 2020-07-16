package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SqlArithmeticWithStringOperandsTest extends SqlTest {
    public static final String TEST_METRIC_NAME = Mocks.metric();
    public static final String TEST_ENTITY_NAME = Mocks.entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        final List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME, "a", "1.2", "b", "7") {{
            addSamples(
                    Sample.ofDateDecimal("2016-06-03T09:26:00.000Z", new BigDecimal("10.3"))
            );
        }});
        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Test(dataProvider = "testData")
    public void test(String expression, String expectedResult) throws Exception {
        String sqlQuery = String.format("SELECT %s FROM \"%s\"", expression, TEST_METRIC_NAME);
        String[][] expectedRows = new String[][]{{expectedResult}};
        assertSqlQueryRows("Test " + expression, expectedRows, sqlQuery);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {"tags.a * 10", "12"},
                {"tags.a + 10", "11.2"},
                {"tags.a + tags.b", "8.2"},
                {"tags.b - tags.a", "5.8"},
                {"tags.a + value", "11.5"},
                {"(tags.a + value + 0.5) / 3", "4"},
                {"concat(value,'') * 10 * 2 + 4", "210"},
                {"tags.b % 2", "1"},
                {"tags.a / 2", "0.6"},
                {"(concat(value, '') * 10 + tags.b) / 2", "55"},
                {"(concat(value, 'abc') * 10 + tags.b) / 2", "NaN"},
                {"tags.a * tags.b * value", "86.52"},
        };
    }

}