package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AlertHistorySizeQueryCheck;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@Test
public class AlertHistoryQueryTest extends AlertTest {
    private final static String ALERTHISTORY_ENTITY_NAME = "alert-historyquery-entity-1";

    @BeforeClass
    public void generateAlertHistory() throws Exception {
        Registry.Entity.register(ALERTHISTORY_ENTITY_NAME);
        generateAlertForEntity(ALERTHISTORY_ENTITY_NAME);
        AlertHistoryQuery query = templateQuery()
                .setEntity(ALERTHISTORY_ENTITY_NAME)
                .setLimit(1);
        Checker.check(new AlertHistorySizeQueryCheck(query, 1));
    }

    private AlertHistoryQuery templateQuery() {
        AlertHistoryQuery query = new AlertHistoryQuery();
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        query.setMetric(RULE_METRIC_NAME);
        return query;
    }


    @DataProvider(name = "alertEntityFiltersProvider")
    public Object[][] provideEntityFilters() {
        return new Object[][]{
                {templateQuery().setEntity("alert-historyquery-entity*")},
                {templateQuery().setEntities(Collections.singletonList("alert-historyquery-entity-?"))},
                {templateQuery().setEntityExpression("name LIKE '*rt-historyquery-entity-1'")}
        };
    }


    /**
     * #2991
     */
    @Test(enabled = false, dataProvider = "alertEntityFiltersProvider")
    public void testEntityFilter(AlertHistoryQuery query) throws Exception {
        List<Alert> alertList = queryHistory(query);
        String assertMessage = String.format(
                "Query response must contain at least one alert. Query %s",
                query
        );
        assertTrue(assertMessage, alertList.size() > 0);
    }

    /**
     * #2993
     */
    @Test(enabled = false)
    public void testUnknownEntityNotAffectProcessingOthers() throws Exception {
        AlertHistoryQuery qExist = templateQuery().setEntity("alert-historyquery-entity-1");
        AlertHistoryQuery qUnknown = templateQuery().setEntity("UNKNOWN");
        List<Alert> resultList = AlertMethod.queryHistory(qExist, qUnknown);
        assertEquals("Fail to get alert history by queries with unknown entity", 2, resultList.size());
        assertEquals("Unexpected warning message", "ENTITY not found for name: 'unknown'", resultList.get(1).getWarning());
    }

    @DataProvider(name = "lotsOfNullProvider")
    public Object[][] provideLotsOfNullResponse() {

        return new Object[][] {
            {"[\n" +
             "  {\n" +
             "    \"entity\": \"*\",\n" +
             "    \"entities\": null,\n" +
             "    \"entityExpression\": null,\n" +
             "    \"entityGroup\": null,\n" +
             "    \"rule\": null,\n" +
             "    \"metric\": null,\n" +
             "    \"startDate\": \"1000-01-01T00:00:00.000Z\",\n" +
             "    \"endDate\": \"9999-12-31T23:59:59.999Z\",\n" +
             "    \"interval\": null,\n" +
             "    \"limit\": null\n" +
             "  }\n" +
             "]"}
        };
    }

    /**
     * #3640
     */
    @Test(dataProvider = "lotsOfNullProvider")
    public void testQueryWithNullFieldsSuccess(String json) throws Exception {
        Response resp = AlertMethod.queryHistoryResponseRawJSON(json);
        assertNotThrowsNullPointerException(resp);
    }

    public static void assertNotThrowsNullPointerException(Response resp) throws Exception {
        if (resp.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            return;
        }
        String errMsg = extractErrorMessage(resp);
        assertEquals("Unexpected exception thrown","NullPointerException", errMsg);
        assertTrue("NullPointerException thrown", !"NullPointerException".equals(errMsg));
    }
}
