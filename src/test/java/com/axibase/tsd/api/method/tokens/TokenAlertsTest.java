package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.method.alert.AlertTest;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class TokenAlertsTest extends AlertTest {
    private String entity = Mocks.entity();

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
    }
    //TODO finish after completing alerts method
    @Test(
            description = "Tests alert query endpoint with tokens."
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String url = "/alerts/query";
    }
}
