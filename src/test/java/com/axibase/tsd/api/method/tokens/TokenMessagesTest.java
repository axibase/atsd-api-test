package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MessageCheck;
import com.axibase.tsd.api.method.message.MessageTest;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageQuery;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.testng.Assert.assertTrue;

public class TokenMessagesTest extends MessageTest {
    private final String entity = Mocks.entity();
    private static final String ISO_TIME = Mocks.ISO_TIME;

    private final String username;
    private Message message;

    @Factory(
            dataProvider = "users", dataProviderClass = UserCreator.class
    )
    public TokenMessagesTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() {
        message = new Message(entity, "logger")
                .setMessage(Mocks.message())
                .setDate(ISO_TIME)
                .setSeverity("NORMAL")
                .setSource("default");
        insertMessageCheck(message);
    }

    @Test(
            description = "Tests messages query endpoint with tokens."
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String url = "/messages/query";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        MessageQuery messageQuery = new MessageQuery()
                .setEntity(entity)
                .setStartDate(ISO_TIME)
                .setEndDate(Util.MAX_QUERYABLE_DATE);
        Response response = queryMessageResponse(Collections.singletonList(messageQuery), token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(message)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests messages query stats endpoint with tokens."
    )
    @Issue("6052")
    public void testStatsMethod() throws Exception {
        String url = "/messages/stats/query";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        MessageStatsQuery msq = new MessageStatsQuery()
                .setEntity(entity)
                .setType(message.getType())
                .setStartDate(ISO_TIME)
                .setEndDate(Util.MAX_QUERYABLE_DATE)
                .setAggregate(new Aggregate(AggregationType.COUNT, new Period(1, TimeUnit.DAY)));
        Response response = queryMessageStats(Collections.singletonList(msq), token);
        String expected = String.format("[ {\n" +
                "  \"entity\" : \"%s\",\n" +
                "  \"metric\" : \"messages-count\",\n" +
                "  \"tags\" : {\n" +
                "    \"type\" : \"logger\"\n" +
                "  },\n" +
                "  \"type\" : \"HISTORY\",\n" +
                "  \"aggregate\" : {\n" +
                "    \"type\" : \"DETAIL\"\n" +
                "  },\n" +
                "  \"data\" : [ ]\n" +
                "} ]", entity);
        assertTrue(compareJsonString(expected, response.readEntity(String.class)));
    }

    @Test(
            description = "Tests messages insert endpoint with tokens."
    )
    @Issue("6052")
    public void testInsertMethod() throws Exception {
        String url = "/messages/insert";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        Message messageToInsert = new Message(Mocks.entity(), "logger")
                .setMessage(Mocks.message())
                .setDate(ISO_TIME)
                .setSeverity("NORMAL")
                .setSource("default");
        insertMessageReturnResponse(messageToInsert, token);
        Checker.check(new MessageCheck(messageToInsert));
    }
}
