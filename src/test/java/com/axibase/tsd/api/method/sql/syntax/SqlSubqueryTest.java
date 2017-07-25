package com.axibase.tsd.api.method.sql.syntax;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class SqlSubqueryTest extends SqlTest {
    public static final String ENTITY_NAME = Mocks.entity();
    public static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(Mocks.entity(), METRIC_NAME);
        series.addSamples(
                new Sample("2017-07-21T12:00:00.000Z", 1)
        );

        SeriesMethod.insertSeriesCheck(series);
    }

    @Test
    public void testNonExistentEntity() {
        String nonExistentEntityName = Mocks.entity();

        String sqlQuery = String.format(
                "SELECT value, entity, datetime\n" +
                        "FROM (\n" +
                        "    SELECT value, '%s' as entity, datetime\n" +
                        "    FROM '%s'\n" +
                        ")",
                nonExistentEntityName,
                METRIC_NAME
        );
        String[][] expectedRows = {};

        assertSqlQueryRows("", expectedRows, sqlQuery);
    }

    @Test
    public void testColumnDuplicates() {
        String nonExistentMetric = Mocks.metric();

        String sqlQuery = String.format(
                "SELECT value, entity, datetime\n" +
                        "FROM (\n" +
                        "    SELECT value, entity, datetime, value as 'value'\n" +
                        "    FROM '%s'\n" +
                        ")",
                METRIC_NAME
        );

        Response response = SqlMethod.queryResponse(sqlQuery);
        assertBadRequest("Invalid date conditions", response);
    }
}
