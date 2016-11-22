package com.axibase.tsd.api.method.alert;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AlertHistorySizeQueryCheck;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Mocks.MIN_QUERYABLE_DATE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(enabled = false)
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
        assertTrue(alertList.size() > 0, assertMessage);
    }

    /**
     * #2993
     */
    @Test(enabled = false)
    public void testUnknownEntityNotAffectProcessingOthers() throws Exception {
        AlertHistoryQuery qExist = templateQuery().setEntity("alert-historyquery-entity-1");
        AlertHistoryQuery qUnknown = templateQuery().setEntity("UNKNOWN");
        List<Alert> resultList = AlertMethod.queryHistory(qExist, qUnknown);
        assertEquals(resultList.size(), 2, "Fail to get alert history by queries with unknown entity");
        assertEquals("ENTITY not found for name: 'unknown'", resultList.get(1).getWarning(), "Unexpected warning message");
    }
}
