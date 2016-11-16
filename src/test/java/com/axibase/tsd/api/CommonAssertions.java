package com.axibase.tsd.api.util;


import static org.testng.AssertJUnit.assertTrue;

public class CommonAssertions {
    public static void assertErrorMessageStart(String actualMessage, String expectedMessageStart) {
        String assertMessage = String.format(
                "Error message mismatch!%nActual message:\t\t%s %n%nmust start with:\t%s",
                actualMessage, expectedMessageStart
        );
        assertTrue(assertMessage, actualMessage.startsWith(expectedMessageStart));
    }
}
