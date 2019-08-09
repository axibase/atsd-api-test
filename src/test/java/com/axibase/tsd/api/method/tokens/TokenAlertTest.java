package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.method.checks.AlertCheck;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.model.alert.AlertDeleteQuery;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.model.alert.AlertUpdateQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.ResponseAsList;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class TokenAlertTest extends AlertTest {
    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenAlertTest(String username) {
        this.username = username;
    }


    @Test(
            description = "Tests alert query endpoint with tokens. It first generates alert and retrieves it with basic authorization to then compare it to the alert retrieved by bearer authorization"
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String url = "/alerts/query";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        String entity = Mocks.entity();
        generateAlertForEntity(entity);
        AlertQuery query = new AlertQuery()
                .setStartDate(Util.MIN_QUERYABLE_DATE)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setMetrics(Collections.singletonList(RULE_METRIC_NAME))
                .setEntity(entity);
        Alert alert = queryAlerts(query).readEntity(ResponseAsList.ofAlerts()).get(0); //taking alert by basic authorization to be compared

        Response response = queryAlerts(Collections.singletonList(query), token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(alert)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests alert update endpoint with tokens."
    )
    @Issue("6052")
    public void testUpdateMethod() throws Exception {
        String url = "/alerts/update";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        String entity = Mocks.entity();
        generateAlertForEntity(entity);
        AlertQuery query = new AlertQuery()
                .setStartDate(Util.MIN_QUERYABLE_DATE)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setMetrics(Collections.singletonList(RULE_METRIC_NAME))
                .setEntity(entity);
        Alert alert = queryAlerts(query).readEntity(ResponseAsList.ofAlerts()).get(0);

        alert.setAcknowledged(!alert.getAcknowledged()); //changing data in retrieved alert
        AlertUpdateQuery updateQuery = new AlertUpdateQuery(alert.getId(), alert.getAcknowledged());
        updateAlerts(Collections.singletonList(updateQuery), token);
        Response response = queryAlerts(query);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(alert)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests alert update endpoint with tokens."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        String url = "/alerts/delete";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        String entity = Mocks.entity();
        generateAlertForEntity(entity);
        AlertQuery query = new AlertQuery()
                .setStartDate(Util.MIN_QUERYABLE_DATE)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setMetrics(Collections.singletonList(RULE_METRIC_NAME))
                .setEntity(entity);
        Alert alert = queryAlerts(query).readEntity(ResponseAsList.ofAlerts()).get(0);

        deleteAlerts(Collections.singletonList(new AlertDeleteQuery(alert.getId())), token);
        Checker.check(new DeletionCheck(new AlertCheck(query)));
    }
}
