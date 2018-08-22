package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;

public class BackSlashTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();
    private static final String FORMAT = "hello%sworld";
    private static final String[] CHARACTERS = toArray("\n", "\r", "\t", "\\", "\\n", "\\n\n", "\b",
            // "\a",
            "\"", "\'"
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME);
        ZonedDateTime dateTime = ZonedDateTime.parse("2018-08-20T00:00:00.000Z");
        for (final String character : CHARACTERS) {
            series.addSamples(Sample.ofJavaDateInteger(dateTime, 1, String.format(FORMAT, character)));
            series.addTag(String.valueOf(character.hashCode()), "hello\nworld");
            dateTime = dateTime.plusHours(1);
        }

        insertSeriesCheck(series);
    }

    @DataProvider
    public static Object[][] provideReplace() {
        final Object[][] result = new Object[CHARACTERS.length][CHARACTERS.length];
        final String[] queries = Stream.of(CHARACTERS)
                .map((character) -> String.format("REPLACE(text, '%s', 'Y')", character))
                .toArray(String[]::new);
        final Map<String, String[][]> results = new HashMap<>();
        for (final String character : CHARACTERS) {
            final String[][] strings = new String[CHARACTERS.length][1];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = toArray(String.format(FORMAT, CHARACTERS[i]).replace(character, "Y"));
            }
            results.put(character, strings);
        }
        for (int i = 0; i < CHARACTERS.length; i++) {
            result[i] = toArray(queries[i], results.get(CHARACTERS[i]));
        }
        return result;
    }

    @DataProvider
    public static Object[][] provideSingleArg() {
        return toArray(
                toArray("UPPER", Stream.of(CHARACTERS).map((s) -> String.format(FORMAT, s).toUpperCase())
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("LOWER", Stream.of(CHARACTERS).map((s) -> String.format(FORMAT, s).toLowerCase())
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("LENGTH", Stream.of(CHARACTERS).map((s) -> String.format(FORMAT, s).length())
                        .map(String::valueOf)
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                )
        );
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideReplace",
            description = "Test REPLACE() function with escaped characters"
    )
    public void testReplace(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT %s FROM \"%s\"", query, METRIC_NAME);
        assertSqlQueryRows("Fail to replace an escaped character", results, sqlQuery);
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideSingleArg",
            description = "Test single argument with escaped characters"
    )
    public void testSingleArg(final String func, final String[][] results) {
        final String query = String.format("SELECT %s(text) FROM \"%s\"", func, METRIC_NAME);
        assertSqlQueryRows("Fail to use an espaced character in a single argument function", results, query);
    }
}
