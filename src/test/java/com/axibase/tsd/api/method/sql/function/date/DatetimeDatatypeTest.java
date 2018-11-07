package com.axibase.tsd.api.method.sql.function.date;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class DatetimeDatatypeTest extends SqlTest {
    private static final String TEST_PREFIX = "datetime-datatype-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_DATETIME_VALUE="2018-11-07T09:30:06.000Z";
    private static final String TEST_REQUEST_TEMPLATE = "SELECT %s %n" +
            "FROM \"%s\" %n" +
            "WHERE entity = '%s' %n" +
            "LIMIT 1";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addSamples(
                Sample.ofDate(TEST_DATETIME_VALUE)
        );
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        SeriesMethod.insertSeriesCheck();
    }

    @DataProvider(name = "first_type")
    public static Object[][] provideOne() {
        return new Object[][]{{"min(datetime)"}, {"max(datetime)"}, {"avg(datetime)"}, {"last(datetime)"},
                {"last(datetime)"}, {"isnull(tags.ok, datetime)"}, {"coalesce(null, datetime)"}};
    }

    @Issue("5757")
    @Test(dataProvider = "first_type")
    public void testWithOneParameter(String functionName) {
        String sqlQuery = String.format(
                TEST_REQUEST_TEMPLATE,
                functionName,
                TEST_METRIC_NAME,
                TEST_ENTITY_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery)
                .readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList(TEST_DATETIME_VALUE)
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
