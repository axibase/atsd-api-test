package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Date;

public class SqlFunctionDateFormatTimeExpressionTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-function-date-format-time-expression-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2016-06-03T09:41:00.000Z", "1"));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3283
     */
    @Test
    public void testSimpleArithmetic() {
        String sqlQuery = String.format(
                "SELECT date_format(time+1) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"2016-06-03T09:41:00.001Z"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #3283
     */
    @Test
    public void testMinus() {
        String sqlQuery = String.format(
                "SELECT date_format(time-1) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"2016-06-03T09:40:59.999Z"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #3283
     */
    @Test
    public void testDivisionByZero() {
        String sqlQuery = String.format(
                "SELECT date_format(time/0) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {TestUtil.ISOFormat(new Date(Long.MAX_VALUE))}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3283
     */
    @Test
    public void testAllOperations() {
        String sqlQuery = String.format(
                "SELECT date_format(time + 200/2 - 20*5) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"2016-06-03T09:41:00.000Z"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3283
     */
    @Test
    public void testDivisionWithRest() {
        String sqlQuery = String.format(
                "SELECT date_format(time + 10/3) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"2016-06-03T09:41:00.003Z"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #3283
     */
    @Test
    public void testNanHandling() {
        String sqlQuery = String.format(
                "SELECT date_format(time + 0/0) FROM '%s'", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"null"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3283
     */
    @Test
    public void testNullFormattedOrderBy() {
        String sqlQuery = String.format(
                "SELECT date_format(time + 0/0) FROM '%s' ORDER BY datetime", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"null"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3283
     */
    @Test
    public void testValueAsParam() {
        String sqlQuery = String.format(
                "SELECT date_format(value) FROM '%s' ORDER BY datetime", TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {TestUtil.ISOFormat(1)}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * #3283
     */
    @Test(enabled = false)
    public void testOverflow() {
        String sqlQuery = String.format(
                "SELECT date_format(time + %s - %s) FROM '%s' ORDER BY datetime",
                Long.toString(Long.MAX_VALUE), Long.toString(Long.MAX_VALUE), TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"2016-06-03T09:41:00.000Z"}
        };
        assertTableRowsExist(expectedRows, resultTable);
    }


}
