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
    private static final String REASONED_MESSAGE_TEMPLATE = "Reason: %s%n%s%nexpected:<%s> but was:<%s>";

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


    /**
     * Make assertion with specified message. Compare objects serialized to JSON.
     *
     * @param expected - expected object.
     * @param actual   - actual object.
     * @param <T>      any Type
     * @throws JSONException can be thrown in case of deserialization problem.
     */
    public static <T> void jsonAssert(final T expected, final T actual) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), Util.prettyPrint(actual), JSONCompareMode.LENIENT);
    }

    /**
     * Make assertion. Compare objects serialized to JSON.
     *
     * @param assertMessage - assert message.
     * @param expected      - expected object.
     * @param actual        - actual object.
     * @param <T>           any Type
     * @throws JSONException can be thrown in case of deserialization problem.
     */
    public static <T> void jsonAssert(final String assertMessage, final T expected, final T actual) throws JSONException {
        final String expectedJSON = Util.prettyPrint(expected);
        final String actualJSON = Util.prettyPrint(actual);
        try {
            JSONAssert.assertEquals(expectedJSON, actualJSON, JSONCompareMode.LENIENT);
        } catch (AssertionError assertionError) {
            final String reasonedMessage = String.format(REASONED_MESSAGE_TEMPLATE,
                    assertMessage, assertionError.getMessage(), expectedJSON, actualJSON);
            throw new AssertionError(reasonedMessage);
        }
    }

    /**
     * Make assertion. Compare object serialized to JSON with JSON retreived in Response.
     *
     * @param expected - expected object.
     * @param response - actual response.
     * @param <T>      any Type
     * @throws JSONException can be thrown in case of deserialization problem.
     */
    public static <T> void jsonAssert(final T expected, final Response response) throws JSONException {
        JSONAssert.assertEquals(Util.prettyPrint(expected), response.readEntity(String.class), JSONCompareMode.LENIENT);
    }

    /**
     * Make assertion with specified message. Compare object serialized to JSON with JSON retreived in Response.
     *
     * @param assertMessage - assert message.
     * @param expected      - expected object.
     * @param response      - actual response.
     * @param <T>           any Type
     * @throws JSONException can be thrown in case of deserialization problem.
     */
    public static <T> void jsonAssert(final String assertMessage, final T expected, final Response response) throws JSONException {
        final String expectedJSON = Util.prettyPrint(expected);
        final String actualJSON = response.readEntity(String.class);
        try {
            JSONAssert.assertEquals(Util.prettyPrint(expected), response.readEntity(String.class), JSONCompareMode.LENIENT);
        } catch (AssertionError assertionError) {
            final String reasonedMessage = String.format(REASONED_MESSAGE_TEMPLATE,
                    assertMessage, assertionError.getMessage(), expectedJSON, actualJSON);
            throw new AssertionError(reasonedMessage);
        }
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
