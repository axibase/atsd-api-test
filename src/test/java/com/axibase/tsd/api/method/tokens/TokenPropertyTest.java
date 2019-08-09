package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.DeletionCheck;
import com.axibase.tsd.api.method.checks.PropertyCheck;
import com.axibase.tsd.api.method.property.PropertyTest;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.model.property.PropertyQuery;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.testng.AssertJUnit.assertTrue;

public class TokenPropertyTest extends PropertyTest {
    private final String entity = Mocks.entity();
    private final String propertyType = Mocks.propertyType();
    private static final String ISO_TIME = Mocks.ISO_TIME;
    private static final String TAG_NAME = "name";
    private static final String TAG_VALUE = "value";

    private final String username;
    private Property property;

    @Factory(
            dataProvider = "users", dataProviderClass = UserCreator.class
    )
    public TokenPropertyTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        property = new Property(propertyType, entity)
                .setDate(ISO_TIME)
                .addTag(TAG_NAME, TAG_VALUE);
        insertPropertyCheck(property);
    }

    @Test(
            description = "Tests properties get endpoint with tokens."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String url = "/properties/" + entity + "/types/" + propertyType;
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Response response = urlQueryProperty(propertyType, entity, token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(property)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests properties get types  endpoint with tokens."
    )
    @Issue("6052")
    public void testGetTypesMethod() throws Exception {
        String url = "/properties/" + entity + "/types";
        String token = TokenRepository.getToken(username, HttpMethod.GET, url);
        Response response = typeQueryProperty(entity, token);
        String expectedResponse = String.format("[\"%s\"]", propertyType);
        assertTrue(compareJsonString(expectedResponse, response.readEntity(String.class)));
    }

    @Test(
            description = "Tests properties query  endpoint with tokens."
    )
    @Issue("6052")
    public void testQueryMethod() throws Exception {
        String url = "/properties/query";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        PropertyQuery query = new PropertyQuery(propertyType, entity)
                .setStartDate(ISO_TIME)
                .setEndDate(Util.MAX_QUERYABLE_DATE);
        Response response = queryProperty(Collections.singletonList(query), token);
        assertTrue(compareJsonString(Util.prettyPrint(Collections.singletonList(property)), response.readEntity(String.class)));
    }

    @Test(
            description = "Tests properties insert endpoint with tokens."
    )
    @Issue("6052")
    public void testInsertMethod() throws Exception {
        String url = "/properties/insert";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        Property insertedProperty = new Property(Mocks.propertyType(), Mocks.entity())
                .addTag(TAG_NAME, TAG_VALUE)
                .setDate(ISO_TIME);
        insertProperty(Collections.singletonList(insertedProperty), token);
        Checker.check(new PropertyCheck(insertedProperty));
    }

    @Test(
            description = "Tests properties delete endpoint with tokens."
    )
    @Issue("6052")
    public void testDeleteMethod() throws Exception {
        Property propertyToDelete = new Property(Mocks.propertyType(), Mocks.entity()) //creating data for deletion
                .addTag(TAG_NAME, TAG_VALUE)
                .setDate(ISO_TIME);
        insertPropertyCheck(propertyToDelete);

        String url = "/properties/delete";
        String token = TokenRepository.getToken(username, HttpMethod.POST, url);
        PropertyQuery query = new PropertyQuery(propertyToDelete.getType(), propertyToDelete.getEntity())
                .setStartDate(ISO_TIME)
                .setEndDate(Util.MAX_QUERYABLE_DATE);
        deleteProperty(Collections.singletonList(query), token);
        Checker.check(new DeletionCheck(new PropertyCheck(propertyToDelete)));
    }
}
