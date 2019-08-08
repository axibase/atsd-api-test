package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.model.alert.Alert;
import com.axibase.tsd.api.model.alert.AlertQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;

public class TokenAlertsTest extends AlertTest {
    private String entity = Mocks.entity();
    private Alert alert;

    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = UserCreator.class
    )
    public TokenAlertsTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        generateAlertForEntity(entity);
        AlertQuery query = new AlertQuery()
                .setStartDate(Util.MIN_QUERYABLE_DATE)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setMetrics(Collections.singletonList(RULE_METRIC_NAME))
                .setEntity(entity);
        Response response = queryAlerts(query);
        Map alertInfo = (Map)response.readEntity(List.class).get(0);

    }

    @Test(
            description = "Tests alert query endpoint with tokens."
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String url = "/alerts/query";
        String token =  TokenRepository.getToken(username, HttpMethod.POST, url);
        AlertQuery query = new AlertQuery()
                .setStartDate(Util.MIN_QUERYABLE_DATE)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setMetrics(Collections.singletonList(RULE_METRIC_NAME))
                .setEntity(entity);
        Response response = queryAlerts(Collections.singletonList(query), token);
        assertEquals("Alert query returned bad response for tokens!", Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        Map alertInfo = (Map) response.readEntity(List.class).get(0); //response can be deserialized only to map
        assertEquals("Alert query returned wrong entity for tokens!", alertInfo.get("entity"), entity);
        assertEquals("Alert query returned wrong metric for tokens!", alertInfo.get("metric"), RULE_METRIC_NAME);
    }


}
