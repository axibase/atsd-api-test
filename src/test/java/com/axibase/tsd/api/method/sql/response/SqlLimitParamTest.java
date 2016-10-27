package com.axibase.tsd.api.method.sql.response;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;

public class SqlLimitParamTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-response-limit-param-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        series.addData(new Sample("2016-06-29T08:00:01.000Z", "1"));
        series.addData(new Sample("2016-06-29T08:00:02.000Z", "2"));
        series.addData(new Sample("2016-06-29T08:00:03.000Z", "3"));
        series.addData(new Sample("2016-06-29T08:00:04.000Z", "4"));
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * Issue #3278
     */
    @Test
    public void testRequestLessLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY value LIMIT 3",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery, 5);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"0.0"}, {"1.0"}, {"2.0"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * Issue #3278
     */
    @Test
    public void testRequestGraterLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY value LIMIT 5",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery, 3);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"0.0"}, {"1.0"}, {"2.0"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * Issue #3278
     */
    @Test
    public void testLimitUndefined() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY value",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery, 3);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"0.0"},
                {"1.0"},
                {"2.0"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * Issue #3278
     */
    @Test
    public void testRequestNegativeWithLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY value LIMIT 4",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery, -1);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"0.0"}, {"1.0"}, {"2.0"}, {"3.0"}
        };

        assertTableRowsExist(expectedRows, resultTable, "value");
    }


    /**
     * Issue #3278
     */
    @Test
    public void testRequestUndefinedWithLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY VALUE LIMIT 3",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        String[][] expectedRows = {
                {"0.0"}, {"1.0"}, {"2.0"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }


    /**
     * Issue #3278
     */
    @Test
    public void testRequestNegativeWithoutLimit() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' ORDER BY value",
                TEST_METRIC_NAME
        );

        Response response = queryResponse(sqlQuery, -1);

        StringTable resultTable = response.readEntity(StringTable.class);


        String[][] expectedRows = {
                {"0.0"}, {"1.0"}, {"2.0"}, {"3.0"}, {"4.0"}
        };

        assertTableRowsExist(expectedRows, resultTable);
    }
}
