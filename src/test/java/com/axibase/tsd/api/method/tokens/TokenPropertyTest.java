package com.axibase.tsd.api.method.tokens;

import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Factory;

public class TokenPropertyTest extends TokenWorkTest {
    private final String entity = Mocks.entity();
    private final String propertyType = Mocks.propertyType();
    private static final String TIME = Mocks.ISO_TIME;

    private final String username;

    @Factory(
            dataProvider = "users"
    )
    public TokenPropertyTest(String username) {
        this.username = username;
    }


    public void prepareData() throws Exception {

    }
}
