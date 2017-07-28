package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

public class SelectNonExistentMetricTest extends SqlTest {
    /**
     * #4421
     */
    @Test
    public void testErrorNonExistentMetric() {
        String metricName = Mocks.metric();
        String sqlQuery = String.format("SELECT * FROM '%s'", metricName);

        String expectedMessage = String.format("Metric '%s' not found", metricName);

        Response responce = queryResponse(sqlQuery);
        assertBadRequest(expectedMessage, responce);
    }
}
