package com.axibase.tsd.api.util;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


import javax.ws.rs.core.Response;

import java.math.BigDecimal;

import static org.testng.AssertJUnit.assertTrue;

public class CommonAssertions {
    private static final String DEFAULT_ASSERT_CHECK_MESSAGE = "Failed to check condition!";
    private static final String OBJECTS_ASSERTION_TEMPLATE = "%s %nexpected:<%s> but was:<%s>";

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

    public static <T> void jsonAssert(final T expected, final T actual) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), Util.prettyPrint(actual), JSONCompareMode.LENIENT);
    }

    public static <T> void jsonAssert(final String assertMessage, final T expected, final T actual) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), Util.prettyPrint(actual), JSONCompareMode.LENIENT);
    }

    public static <T> void jsonAssert(final T expected, final Response response) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), response.readEntity(String.class), JSONCompareMode.LENIENT);
    }

    public static <T> void jsonAssert(final String assertMessage, final T expected, final Response response) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), response.readEntity(String.class), JSONCompareMode.LENIENT);
    }

    /**
     * Compare {@link BigDecimal} instances using {@link BigDecimal#compareTo(BigDecimal)} method.
     *
     * @param assertMessage assert message.
     * @param expected      {@link BigDecimal} expected value.
     * @param actual        {@link BigDecimal} actual value.
     */
    public static void assertDecimals(final String assertMessage, final BigDecimal expected, final BigDecimal actual) {
        final boolean result = expected != null && actual != null && expected.compareTo(actual) == 0;
        if (!result) {
            throw new AssertionError(String.format(OBJECTS_ASSERTION_TEMPLATE, assertMessage,
                    expected, actual));
        }
    }

    /**
     * Compare {@link BigDecimal} instances using {@link BigDecimal#compareTo(BigDecimal)} method.
     *
     * @param expected {@link BigDecimal} expected value.
     * @param actual   {@link BigDecimal} actual value.
     */
    public static void assertDecimals(final BigDecimal expected, final BigDecimal actual) {
        final boolean result = expected != null && actual != null && expected.compareTo(actual) == 0;
        if (!result) {
            throw new AssertionError(String.format(OBJECTS_ASSERTION_TEMPLATE, null,
                    expected, actual));
        }
    }
}
