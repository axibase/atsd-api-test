package com.axibase.tsd.api.method.sql.examples.ordering;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;


public class SqlExampleOrderByTimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-order-by-time-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2016-07-27T22:41:52.000Z", "0"));
        series.addData(new Sample("2016-07-27T22:41:51.000Z", "1"));
        series.addData(new Sample("2016-07-27T22:41:50.000Z", "2"));
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3047
     * Test for alias documentation example.
     *
     * @see <a href="Computed Columns">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/order-by-time.md</a>
     */
    @Test
    public void test() {
        String sqlQuery = String.format(
                "SELECT datetime, value FROM '%s'%nORDER BY datetime",
                TEST_METRIC_NAME
        );
        Response response = executeQuery(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-07-27T22:41:50.000Z", "2.0"),
                Arrays.asList("2016-07-27T22:41:51.000Z", "1.0"),
                Arrays.asList("2016-07-27T22:41:52.000Z", "0.0")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }
}
