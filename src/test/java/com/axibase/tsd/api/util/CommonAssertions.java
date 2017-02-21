package com.axibase.tsd.api.util;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;

import static org.testng.AssertJUnit.assertTrue;

public class CommonAssertions {
    private static final String DEFAULT_ASSERT_CHECK_MESSAGE = "Failed to check condition!";

    public static void assertErrorMessageStart(String actualMessage, String expectedMessageStart) {
        String assertMessage = String.format(
                "Error message mismatch!%nActual message:\t\t%s %n%nmust start with:\t%s",
                actualMessage, expectedMessageStart
        );
        assertTrue(assertMessage, actualMessage.startsWith(expectedMessageStart));
    }

    public static void assertCheck(AbstractCheck check) {
        assertCheck(check, DEFAULT_ASSERT_CHECK_MESSAGE);
    }

    public static void assertCheck(AbstractCheck check, String assertMessage) {
        Boolean result = true;
        try {
            Checker.check(check);
        } catch (NotCheckedException e) {
            result = false;
        }
        assertTrue(assertMessage, result);
    }
}
