package com.axibase.tsd.api.util;


import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import lombok.experimental.UtilityClass;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@UtilityClass
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

    public static void assertErrorMessageContains(String actualMessage, String expectedPattern) {
        String assertMessage = String.format(
                "Error message mismatch!%nActual message:\t\t%s %n%nmust contain:\t%s",
                actualMessage, expectedPattern
        );
        assertTrue(assertMessage, actualMessage.contains(expectedPattern));
    }

    public static void assertCheck(AbstractCheck check) {
        assertCheck(check, DEFAULT_ASSERT_CHECK_MESSAGE);
    }

    public static void assertCheck(AbstractCheck check, String assertMessage) {
        boolean result = true;
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

    /**
     * Create {@link BigDecimal} from the string,
     * and compare it with provided {@link BigDecimal} using {@link BigDecimal#compareTo(BigDecimal)} method.
     */
    public static void assertDecimals(final String expected, final BigDecimal actual) {
        assertDecimals(new BigDecimal(expected), actual);
    }

    /**
     * Assert equality of lists.
     * Series equality is asserted by the {@link #assertEqualSeries(Series, Series, String)}.
     */
    public static void assertEqualLists(List<Series> actual, List<Series> expected) {
        assertEquals(actual.size(), expected.size(),
                String.format("Actual list size %d, expected list size %d.", actual.size(), expected.size()));
        Iterator<Series> itActual = actual.iterator();
        Iterator<Series> itExpected = expected.iterator();
        int index = 0;
        while (itActual.hasNext()) {
            Series expectedSeries = itExpected.next();
            Series actualSeries = itActual.next();
            String msg = String.format("Unexpected series with index %d.%n Expected series: %s.%nActual series: %s.%n",
                    index, expectedSeries, actualSeries);
            assertEqualSeries(actualSeries, expectedSeries, msg);
            index++;
        }
    }

    /**
     * Compare entities, metrics, tags, and data of series.
     * Use {@link BigDecimal#compareTo(BigDecimal)} to compare series values.
     */
    private static void assertEqualSeries(Series actual, Series expected, String msg) {
        if (actual.compareTo(expected) != 0) {
            throw new AssertionError(msg + "Actual series key differs from expected./n");
        }
        assertEqualSamples(actual.getData(), expected.getData(), msg);
    }

    /**
     * The same as {@link #assertEqualSamples(List, List, String, MathContext)},
     * but without rounding.
     */
    private static void assertEqualSamples(List<Sample> actual, List<Sample> expected, String msg) {
        assertEqualSamples(actual, expected, msg, MathContext.UNLIMITED);
    }

    /**
     * For each sample compare its timestamp, value, annotation and version.
     * Compare sample values rounded in specified context.
     */
    public static void assertEqualSamples(List<Sample> actual, List<Sample> expected, String msg, MathContext roundingContext) {
        if (actual.size() != expected.size()) {
            throw new AssertionError(String.format(
                    "%s Actual series size %d differs from expected series size %d.%n",
                    msg, actual.size(), expected.size()));
        }
        Iterator<Sample> itActual = actual.iterator();
        Iterator<Sample> itExpected = expected.iterator();
        int index = 0;
        while (itActual.hasNext()) {
            Sample actualSample = itActual.next();
            Sample expectedSample = itExpected.next();
            if (!actualSample.theSame(expectedSample, roundingContext)) {
                throw new AssertionError(String.format(
                        "%s Series differs at sample with index %d.%nActual sample: %s.%nExpected sample: %s.%n",
                        msg, index, actualSample, expectedSample));
            }
        }
    }

    /** Assert that each series from the list has specified size. */
    public static void assertSeriesSize(List<Series> seriesList, int size) {
        int index = 0;
        for (Series series : seriesList) {
            int actualSize = series.getData().size();
            if (actualSize != size) {
                String msg = "Series with index %d has unexpected size. Expected size %d. Actual size %d. Series %s.%n";
                throw new AssertionError(String.format(msg, index, size, actualSize, series));
            }
            index++;
        }
    }

    /**
     * For each values array assert that there is a series in the list
     * such that the array consists of all the series values.
     * @param seriesList List of series.
     * @param values     Collection of values arrays. Each value will be converted to {@link BigDecimal}.
     *                   (Strings are used to simplify method usage.)
     */
    public static void checkValues(List<Series> seriesList, String[]... values) {
        String msg = "Array of values %s not found in series list.";
        for (String[] valuesArray : values) {
            BigDecimal[] decimals = Arrays.stream(valuesArray).map(BigDecimal::new).toArray(BigDecimal[]::new);
            if (seriesList.stream().noneMatch(series -> hasValues(series, decimals))) {
                throw new AssertionError(String.format(msg, valuesArray));
            }
        }
    }

    /**
     * Assert that the values array consists of all the series values.
     * (Each value of the array is converted to {@link BigDecimal}.)
     */
    public static void checkValues(Series series, String... values) {
        BigDecimal[] decimals = Arrays.stream(values).map(BigDecimal::new).toArray(BigDecimal[]::new);
        if (!hasValues(series, decimals)) {
            throw new AssertionError(String.format("Array %n%s%n doesn't equal to array of series values %n%s%n",
                    Arrays.toString(values), series));
        }
    }

    /** True iff specified array is array of the series values (in the same order). */
    public static boolean hasValues(Series series, BigDecimal... valuesArray) {
        List<Sample> samples = series.getData();
        return samples.size() == valuesArray.length &&
                IntStream.range(0, valuesArray.length)
                        .allMatch(i -> valuesArray[i].compareTo(samples.get(i).getValue()) == 0);
    }

    /**
     * Assert that provide json node is an array, and it's size is as expected.
     */
    public static void assertArraySize(JsonNode actualNode, int expectedArraySize) {
        assertEquals(actualNode.getNodeType(), JsonNodeType.ARRAY, "Array is expected.");
        assertEquals(actualNode.size(), expectedArraySize, "Unexpected array size.");
    }
}
