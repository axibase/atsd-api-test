package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.method.property.PropertyMethod;
import com.axibase.tsd.api.model.property.Property;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;


public class TokenPropertyTest extends PropertyMethod {
    private final String entity = Mocks.entity();
    private final String propertyType = Mocks.propertyType();
    private Property property;
    private static final String TIME = Mocks.ISO_TIME;
    private static final String TAG_NAME = "name";
    private static final String TAG_VALUE = "value";

    private final String username;

    @Factory(
            dataProvider = "users", dataProviderClass = UserCreator.class
    )
    public TokenPropertyTest(String username) {
        this.username = username;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        property = new Property(propertyType, entity)
                .setDate(TIME)
                .addTag(TAG_NAME, TAG_VALUE);
        insertPropertyCheck(property);
    }

    @Test(
            description = "Tests properties get endpoint with tokens."
    )
    @Issue("6052")
    public void testGetMethod() throws Exception {
        String getURL = "/properties/" + entity + "/types/" + propertyType;
        String getToken = TokenRepository.getToken(username, HttpMethod.GET, getURL);
        Response response = PropertyMethod.urlQueryProperty(propertyType, entity, getToken);
        compareJsonString(Util.prettyPrint(property), response.readEntity(String.class), false);
    }
    //TODO other methods
}
