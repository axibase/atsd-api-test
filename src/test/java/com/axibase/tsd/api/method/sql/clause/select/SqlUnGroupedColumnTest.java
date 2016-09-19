package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import java.util.Collections;

import static javax.ws.rs.core.Response.Status;
import static org.testng.Assert.assertEquals;


public class SqlUnGroupedColumnTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-un-grouped-column-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareDate() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }

    @Test
    public void testErrorRaisingSelectUngroupedColumnWithGroupClause() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, avg(value) %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z' %nGROUP BY entity",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(getErrorMessageFromResponse(response), "Include column \"datetime\" in the GROUP BY clause.");
    }

    @Test
    public void testErrorRaisingSelectUngroupedColumnWithoutGroupClause() {
        String sqlQuery = String.format(
                "SELECT entity, datetime, avg(value) %nFROM '%s' %nWHERE datetime = '2016-06-29T08:00:00.000Z'",
                TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals(getErrorMessageFromResponse(response), "SELECT expression cannot include both an aggregation function and a column not referenced in GROUP BY clause.");
    }

    private String getErrorMessageFromResponse(Response response) {
        String jsonText = response.readEntity(String.class);
        try {
            JSONObject json = new JSONObject(jsonText);
            return json.getJSONArray("errors")
                    .getJSONObject(0)
                    .getString("message");
        } catch (JSONException e) {
            return null;
        }

    }
}
