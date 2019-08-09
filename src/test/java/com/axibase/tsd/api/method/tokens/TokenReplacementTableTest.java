package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.ReplacementTableCheck;
import com.axibase.tsd.api.method.replacementtable.ReplacementTableMethod;
import com.axibase.tsd.api.model.replacementtable.ReplacementTable;
import com.axibase.tsd.api.model.replacementtable.SupportedFormat;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertTrue;

public class TokenReplacementTableTest extends ReplacementTableMethod {
    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = TokenUsers.class
    )
    public TokenReplacementTableTest(String username) {
        this.username = username;
    }

    @Test(
            description = "Tests replacement table get endpoint."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String replacementTableName = Mocks.replacementTable();
        String url = "/replacement-tables/json/" + replacementTableName;
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        ReplacementTable replacementTable = ReplacementTable.of(replacementTableName, SupportedFormat.JSON)
                .addValue("key", "value");
        createCheck(replacementTable);

        Response response = getReplacementTablesResponse(replacementTableName, token);
        assertTrue(compareJsonString(Util.prettyPrint(replacementTable), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests replacement table update endpoint."
    )
    @Issue("6052")
    public void testUpdateMethod() throws Exception {
        String replacementTableName = Mocks.replacementTable();
        String url = "/replacement-tables/json/" + replacementTableName;
        String token = TokenRepository.getToken(username, "PATCH", url);
        ReplacementTable replacementTable = ReplacementTable.of(replacementTableName, SupportedFormat.JSON)
                .addValue("key", "value");
        createCheck(replacementTable);

        replacementTable.addValue("new-key", "new-value");
        updateReplacementTableResponse(replacementTable, token);
        Checker.check(new ReplacementTableCheck(replacementTable));
    }

    @Test(
            description = "Tests replacement table create or replace endpoint."
    )
    @Issue("6052")
    public void testCreateMethod() throws Exception {
        String replacementTableName = Mocks.replacementTable();
        String url = "/replacement-tables/json/" + replacementTableName;
        String token = TokenRepository.getToken(username, HttpMethod.PUT, url);
        ReplacementTable replacementTable = ReplacementTable.of(replacementTableName, SupportedFormat.JSON)
                .addValue("key", "value");

        createResponse(replacementTable, token);
        Checker.check(new ReplacementTableCheck(replacementTable));
    }

    @Test(
            description = "Tests replacement table delete endpoint."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        String replacementTableName = Mocks.replacementTable();
        String url = "/replacement-tables/" + replacementTableName;
        String token = TokenRepository.getToken(username, HttpMethod.DELETE, url);
        ReplacementTable replacementTable = ReplacementTable.of(replacementTableName, SupportedFormat.JSON)
                .addValue("key", "value");
        createCheck(replacementTable);

        deleteReplacementTableResponse(replacementTableName, token);
        Checker.check(new DeletionCheck(new ReplacementTableCheck(replacementTable)));
    }
}
