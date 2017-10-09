package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class WhereColumnNameAliasPriorityTest extends SqlTest {
    private final String TEST_ENTITY = entity();
    private final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(
                TEST_ENTITY,
                TEST_METRIC,
                Collections.singletonMap("tag", "value"));

        series.addSamples(
                Sample.ofDateDecimalText(
                        Mocks.ISO_TIME,
                        Mocks.DECIMAL_VALUE,
                        Mocks.TEXT_VALUE));

        SeriesMethod.insertSeriesCheck(series);
    }

    @DataProvider
    public Object[][] provideAliasFilters() {
        return new Object[][] {
                {"'some_string' AS \"entity\"", String.format("entity = '%s'", TEST_ENTITY)},
                {"'some_string' AS \"metric.name\"", String.format("metric.name = '%s'", TEST_METRIC)},
                {"'some_string' AS \"datetime\"", String.format("datetime = '%s'", Mocks.ISO_TIME)},
                {"'some_string' AS \"time\"", String.format("time = %s", Mocks.MILLS_TIME)},
                {"'some_string' AS \"value\"", String.format("value = '%s'", Mocks.DECIMAL_VALUE)},
                {"'some_string' AS \"text\"", String.format("text = '%s'", Mocks.TEXT_VALUE)},
                {"'some_string' AS \"tags\"", "tags = 'tag=value'"},
                {"'some_string' AS \"tags.tag\"", "tags.tag = 'value'"},
        };
    }

    @Issue("4489")
    @Test(dataProvider = "provideAliasFilters")
    public void testEntityColumnNameAliasPriority(String selectExpr, String filter) {
        String sqlQuery = String.format(
                "SELECT %s " +
                "FROM \"%s\" " +
                "WHERE %s",
                selectExpr,
                TEST_METRIC,
                filter);

        String[][] expectedResult = {
                {"some_string"}
        };

        assertSqlQueryRows(
                String.format("Incorrect result with filter %s", filter),
                expectedResult,
                sqlQuery);
    }
}
