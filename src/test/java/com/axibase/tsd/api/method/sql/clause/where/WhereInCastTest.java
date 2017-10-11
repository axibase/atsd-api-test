package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereInCastTest extends SqlTest {
    private final String TEST_ENTITY = entity();
    private final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY, TEST_METRIC);
        series.addSamples(
              Sample.ofDateInteger("2017-01-02T03:04:05Z", 0)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider(parallel = true)
    Object[][] provideFilters() {
        return new Object[][]{
                {"date_format(time, 'yyyy') = '2017'"},
                {"date_format(time, 'yyyy') = 2017"},
                {"date_format(time, 'yyyy') IN ('2017')"},
                {"date_format(time, 'yyyy') IN (2017)"},
                {"SECOND(datetime) IN (1, 2 ,3, 4, 5)"},
                {"UPPER('2017') IN ('2017')"},
                {"UPPER('2017') IN (2017)"},
                {"LOWER('2017') IN ('2017')"},
                {"LOWER('2017') IN (2017)"},
                {"REPLACE('2017', '1', '0') IN ('2007')"},
                {"REPLACE('2017', '1', '0') IN (2007)"},
                {"LENGTH('2017') IN (4)"},
                {"LENGTH('2017') IN ('4')"},
                {"CONCAT(20, 17) IN ('2017')"},
                {"CONCAT(20, 17) IN (2017)"},
                {"LOCATE('7', '2017') IN (4)"},
                {"LOCATE('7', '2017') IN ('4')"},
                {"SUBSTR('12017', 2) IN ('2017')"},
                {"SUBSTR('12017', 2) IN (2017)"},
                {"COALESCE(text, '2017') IN ('2017')"},
                {"COALESCE(text, '2017') IN (2017)"},
                {"COALESCE(text, 2017) IN (2017)"},
                {"COALESCE(text, 2017) IN ('2017')"},
                {"ISNULL(text, '2017') IN ('2017')"},
                {"ISNULL(text, '2017') IN (2017)"},
                {"ISNULL(text, 2017) IN (2017)"},
                {"ISNULL(text, 2017) IN ('2017')"}
        };
    }

    @Issue("4558")
    @Test(dataProvider = "provideFilters")
    public void testInImplicitCast(String filter) {
        String query = String.format(
                "SELECT datetime FROM \"%s\" WHERE %s",
                TEST_METRIC,
                filter);

        String[][] expectedRows = {
                {"2017-01-02T03:04:05.000Z"}
        };

        assertSqlQueryRows(String.format("Incorrect query result with filter %s", filter),
                expectedRows,
                query);
    }
}
