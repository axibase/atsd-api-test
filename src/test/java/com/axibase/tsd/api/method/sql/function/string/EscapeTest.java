package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;
import static org.testng.AssertJUnit.fail;

public class EscapeTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();
    private static final String ALARM_CHARACTER = Character.toString((char) 7); // \a
    private static final String FORMAT = "hello%sworld";
    private static final String[] CHARACTERS = toArray(
            "\n", "\r", "\t", "\\", "\\n", "\\n\n", "\b", "\"", "\'", Character.toString((char) 7 /* \a */)
    );

    @BeforeClass
    public static void prepareData() throws Exception {
        final Series series = new Series(ENTITY_NAME, METRIC_NAME)
                .addSamples(Sample.ofDateText("2018-08-20T00:00:00.000Z", "hello\nworld"))
                .addSamples(Sample.ofDateText("2018-08-20T01:00:00.000Z", "hello\rworld"))
                .addSamples(Sample.ofDateText("2018-08-20T02:00:00.000Z", "hello\tworld"))
                .addSamples(Sample.ofDateText("2018-08-20T03:00:00.000Z", "hello\\world"))
                .addSamples(Sample.ofDateText("2018-08-20T04:00:00.000Z", "hello\\nworld"))
                .addSamples(Sample.ofDateText("2018-08-20T05:00:00.000Z", "hello\\n\nworld"))
                .addSamples(Sample.ofDateText("2018-08-20T06:00:00.000Z", "hello\bworld"))
                .addSamples(Sample.ofDateText("2018-08-20T07:00:00.000Z", "hello\"world"))
                .addSamples(Sample.ofDateText("2018-08-20T08:00:00.000Z", "hello\'world"))
                .addSamples(Sample.ofDateText(
                        "2018-08-20T09:00:00.000Z", String.format("hello%sworld", ALARM_CHARACTER))
                )
                .addTag(String.valueOf("\n".hashCode()), "hello\nworld")
                .addTag(String.valueOf("\r".hashCode()), "hello\rworld")
                .addTag(String.valueOf("\t".hashCode()), "hello\tworld")
                .addTag(String.valueOf("\\".hashCode()), "hello\\world")
                .addTag(String.valueOf("\\n".hashCode()), "hello\\nworld")
                .addTag(String.valueOf("\\n\n".hashCode()), "hello\\n\nworld")
                .addTag(String.valueOf("\b".hashCode()), "hello\bworld")
                .addTag(String.valueOf("\"".hashCode()), "hello\"world")
                .addTag(String.valueOf("\'".hashCode()), "hello\'world")
                .addTag(String.valueOf(ALARM_CHARACTER.hashCode()), String.format("hello%sworld", ALARM_CHARACTER));

        insertSeriesCheck(series);
    }

    private static Object[] testCase(final String query, final String... results) {
        return toArray(query, Arrays.stream(results).map(ArrayUtils::toArray).toArray(String[][]::new));
    }

    @DataProvider
    public static Object[][] provideRegex() {
        return toArray(
                testCase("'.+\n.+'", "hello\nworld", "hello\\n\nworld"),
                testCase("'.+\r.+'", "hello\rworld"),
                testCase("'.+\t.+'", "hello\tworld"),
                testCase("'.+\\.+'", "hello\\world"),
                testCase("'.+\\n.+'", "hello\\nworld", "hello\\n\nworld"),
                testCase("'.+\\n\n.+'", "hello\\n\nworld"),
                testCase("'.+\b.+'", "hello\bworld"),
                testCase("'.+\"\".+'", "hello\"world"),
                testCase("'.+\'\'.+'", "hello\'world"),
                testCase(String.format("'.+%s.+'", ALARM_CHARACTER), String.format("hello%sworld", ALARM_CHARACTER))
        );
    }

    @DataProvider
    public static Object[][] provideReplace() {
        final String[] queries = Arrays.stream(CHARACTERS)
                .map((character) -> (character.equals("\'") || character.equals("\"")) ?
                        character.concat(character) : character)
                .map((character) -> (character.equals("\\n\n") || character.equals("\\n")) ?
                        escapeJava(character) : character)
                .map((character) -> String.format("REPLACE(text, '%s', 'Y')", character))
                .toArray(String[]::new);
        final Map<String, String[][]> results = new HashMap<>();
        for (int i = 0; i < queries.length; i++) {
            final String toReplace = CHARACTERS[i];
            final String[][] strings = Arrays.stream(CHARACTERS)
                    .map((symbol) -> String.format(FORMAT, symbol))
                    .map((str) -> str.replace(toReplace, "Y"))
                    .map(ArrayUtils::toArray)
                    .toArray(String[][]::new);
            results.put(queries[i], strings);
        }
        return results.entrySet().stream()
                .map((entry) -> toArray(entry.getKey(), entry.getValue()))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public static Object[][] provideSimpleFunctions() {
        return toArray(
                toArray("UPPER(text)", Arrays.stream(CHARACTERS)
                        .map((s) -> String.format(FORMAT, s).toUpperCase())
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("LOWER(text)", Arrays.stream(CHARACTERS)
                        .map((s) -> String.format(FORMAT, s).toLowerCase())
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("LENGTH(text)", Arrays.stream(CHARACTERS)
                        .map((s) -> String.format(FORMAT, s).length())
                        .map(String::valueOf)
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("CONCAT(text, 'Y')", Arrays.stream(CHARACTERS)
                        .map((s) -> String.format(FORMAT, s).concat("Y"))
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                ),
                toArray("SUBSTR(text, 0, 8)", Arrays.stream(CHARACTERS)
                        .map((s) -> String.format(FORMAT, s).substring(0, 8))
                        .map(ArrayUtils::toArray)
                        .toArray(String[][]::new)
                )
        );
    }

    @DataProvider
    public static Object[][] provideLocate() {
        final String[] queries = Arrays.stream(CHARACTERS)
                .map((character) -> (character.equals("\'") || character.equals("\"")) ?
                        character.concat(character) : character)
                .map((character) -> (character.equals("\\n\n") || character.equals("\\n")) ?
                        escapeJava(character) : character)
                .map((character) -> String.format("LOCATE('%s', text)", character))
                .toArray(String[]::new);
        final Map<String, String[][]> results = new HashMap<>();
        for (int i = 0; i < queries.length; i++) {
            final String toFind = CHARACTERS[i];
            final String[][] strings = Arrays.stream(CHARACTERS)
                    .map((symbol) -> String.format(FORMAT, symbol))
                    .map((str) -> str.indexOf(toFind) + 1)
                    .map(String::valueOf)
                    .map(ArrayUtils::toArray)
                    .toArray(String[][]::new);
            results.put(queries[i], strings);
        }
        return results.entrySet().stream()
                .map((entry) -> toArray(entry.getKey(), entry.getValue()))
                .toArray(Object[][]::new);
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
            dataProvider = "provideSimpleFunctions",
            description = "Test single argument with escaped characters"
    )
    public void testSimpleFunctions(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT %s FROM \"%s\"", query, METRIC_NAME);
        final String assertMessage = String.format("Fail to use an escaped character in %s", query);
        assertSqlQueryRows(assertMessage, results, sqlQuery);
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideLocate",
            description = "Test LOCATE() function with escaped characters"
    )
    public void testLocate(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT %s FROM \"%s\"", query, METRIC_NAME);
        assertSqlQueryRows("Fail to locate an escaped character", results, sqlQuery);
    }

    @Issue("5600")
    @Test(
            description = "Test special characters in entity tags"
    )
    public void testEntityTags() {
        final Entity beforeUpdate = EntityMethod.getEntity(ENTITY_NAME);
        for (final String character : CHARACTERS) {
            beforeUpdate.addTag(String.valueOf(character.hashCode()), String.format(FORMAT, character));
        }
        EntityMethod.updateEntity(beforeUpdate);
        final Entity afterUpdate = EntityMethod.getEntity(ENTITY_NAME);
        if (!afterUpdate.getTags().equals(beforeUpdate.getTags())) {
            fail("Failed to insert entity tags values with special characters");
        }
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideRegex",
            description = "Test escaped characters in REGEX"
    )
    public void testRegex(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT text FROM \"%s\" WHERE text REGEX %s", METRIC_NAME, query);
        assertSqlQueryRows(String.format("Fail to filter records using REGEX %s", query), results, sqlQuery);
    }
}
