package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.EntityCheck;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;

public class DualTokenTest extends BaseMethod {
    private final String entityName = Mocks.entity();
    private final String firstUrl = "/entities/" + entityName;
    private final String metricName = Mocks.metric();
    private final String secondUrl = "/metrics/" + metricName;

    private final String username;
    private String token;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public DualTokenTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        this.token = TokenRepository.getToken(username, HttpMethod.PUT, firstUrl + "\n" + secondUrl);
    }

    @Test(
            description = "Tests work of first url with dual token."
    )
    @Issue("6052")
    public void testFirstUrl() {
        Entity entity = new Entity(entityName);
        EntityMethod.createOrReplaceEntity(entity, token);
        Checker.check(new EntityCheck(entity));
    }

    @Test(
            description = "Tests work of second url with dual token."
    )
    @Issue("6052")
    public void testSecondUrl() {
        Metric metric = new Metric(metricName);
        MetricMethod.createOrReplaceMetric(metric, token);
        Checker.check(new MetricCheck(metric));
    }
}
