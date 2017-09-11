package com.axibase.tsd.api.method.sql.examples.ordering;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;


public class SqlExampleOrderByValueTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-order-by-value-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";
    private static final String TEST_ENTITY3_NAME = TEST_PREFIX + "entity-3";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME);
        series1.addSamples(
                Sample.ofDateInteger("2016-07-27T22:41:52.000Z", 0),
                Sample.ofDateInteger("2016-07-27T22:41:51.000Z", 1),
                Sample.ofDateInteger("2016-07-27T22:41:50.000Z", 2)
        );

        Series series2 = new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME);
        series2.addSamples(
                Sample.ofDateInteger("2016-07-27T22:41:52.000Z", 2),
                Sample.ofDateInteger("2016-07-27T22:41:51.000Z", 3),
                Sample.ofDateInteger("2016-07-27T22:41:50.000Z", 4)
        );

        Series series3 = new Series(TEST_ENTITY3_NAME, TEST_METRIC_NAME);
        series3.addSamples(
                Sample.ofDateInteger("2016-07-27T22:41:52.000Z", 4),
                Sample.ofDateInteger("2016-07-27T22:41:51.000Z", 5),
                Sample.ofDateInteger("2016-07-27T22:41:50.000Z", 6)
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2, series3));
    }

    @Issue("3047")
    @Test(description = "Test for alias documentation example. " +
            "https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/order-by-value.md")
    public void test() {
        String sqlQuery = String.format(
                "SELECT entity, AVG(value) FROM \"%s\"%nGROUP BY entity%nORDER BY AVG(value) DESC",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList(TEST_ENTITY3_NAME, "5.0"),
                Arrays.asList(TEST_ENTITY2_NAME, "3.0"),
                Arrays.asList(TEST_ENTITY1_NAME, "1.0")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }
}
