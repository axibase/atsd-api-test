package com.axibase.tsd.api.method.version;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import static org.testng.Assert.fail;


public class VersionMethodTest extends VersionMethod {

    @Test
    public static void testQuery() throws JsonProcessingException {
        Version version = new Version();


        System.out.println(new ObjectMapper().writeValueAsString(version));
        try {
            version = queryVersionCheck();
            if (version == null) {
                fail("Empty response");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}