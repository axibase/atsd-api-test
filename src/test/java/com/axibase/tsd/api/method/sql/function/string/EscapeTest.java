package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.axibase.tsd.api.method.series.SeriesMethod.insertSeriesCheck;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static org.apache.commons.lang3.ArrayUtils.toArray;
import static org.testng.AssertJUnit.fail;

public class EscapeTest extends SqlTest {
    private static final String METRIC_NAME = metric();
    private static final String ENTITY_NAME = entity();

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
                        "2018-08-20T09:00:00.000Z",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER()))
                )
                .addTag(EscapeCharacter.NEW_LINE.name(), "hello\nworld")
                .addTag(EscapeCharacter.CARRIAGE_RET.name(), "hello\rworld")
                .addTag(EscapeCharacter.HORIZONTAL_TAB.name(), "hello\tworld")
                .addTag(EscapeCharacter.BACKSLASH.name(), "hello\\world")
                .addTag(EscapeCharacter.BACKSLASH_N.name(), "hello\\nworld")
                .addTag(EscapeCharacter.BACKSLASH_N_NEW_LINE.name(), "hello\\n\nworld")
                .addTag(EscapeCharacter.BACKSPACE.name(), "hello\bworld")
                .addTag(EscapeCharacter.DOUBLE_QUOTE.name(), "hello\"world")
                .addTag(EscapeCharacter.SINGLE_QUOTE.name(), "hello\'world")
                .addTag(EscapeCharacter.BELL.name(), String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER()));

        insertSeriesCheck(series);
    }

    private static Object[] testCase(final String query, final String... results) {
        return toArray(query, Arrays.stream(results).map(ArrayUtils::toArray).toArray(String[][]::new));
    }

    @DataProvider
    public static Object[][] provideRegex() {
        return toArray(
                testCase("'(?s).+\\n.+'", "hello\nworld", "hello\\n\nworld"),
                testCase("'(?s).+\r.+'", "hello\rworld"),
                testCase("'(?s).+\t.+'", "hello\tworld"),
                testCase("'(?s).+\\\\\\\\.+'", "hello\\world", "hello\\nworld", "hello\\n\nworld"),
                testCase("'(?s).+\\\\\\\\n.+'", "hello\\nworld", "hello\\n\nworld"),
                testCase("'(?s).+\\\\\\\\n\\n.+'", "hello\\n\nworld"),
                testCase("'(?s).+\b.+'", "hello\bworld"),
                testCase("'(?s).+\"\".+'", "hello\"world"),
                testCase("'(?s).+\'\'.+'", "hello\'world"),
                testCase(String.format("'.+%s.+'", EscapeCharacter.BELL.getCHARACTER()),
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                )
        );
    }

    @DataProvider
    public static Object[][] provideReplace() {
        return toArray(
                testCase("'\n', 'Y'",
                        "helloYworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\nYworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\r', 'Y'",
                        "hello\nworld",
                        "helloYworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\t', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "helloYworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\\', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "helloYworld",
                        "helloYnworld",
                        "helloYn\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\\\\n', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "helloYworld",
                        "helloY\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\\\\n\n', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "helloYworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\b', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "helloYworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\"\"', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "helloYworld",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("'\'\'', 'Y'",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "helloYworld",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase(String.format("'%s', 'Y'", EscapeCharacter.BELL.getCHARACTER()),
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        "helloYworld"
                )
        );
    }

    @DataProvider
    public static Object[][] provideSimpleFunctions() {
        return toArray(
                testCase("UPPER(text)",
                        "HELLO\nWORLD",
                        "HELLO\rWORLD",
                        "HELLO\tWORLD",
                        "HELLO\\WORLD",
                        "HELLO\\NWORLD",
                        "HELLO\\N\nWORLD",
                        "HELLO\bWORLD",
                        "HELLO\"WORLD",
                        "HELLO\'WORLD",
                        String.format("HELLO%sWORLD", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("LOWER(text)",
                        "hello\nworld",
                        "hello\rworld",
                        "hello\tworld",
                        "hello\\world",
                        "hello\\nworld",
                        "hello\\n\nworld",
                        "hello\bworld",
                        "hello\"world",
                        "hello\'world",
                        String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("LENGTH(text)", "11", "11", "11", "11", "12", "13", "11", "11", "11", "11"),
                testCase("CONCAT(text, 'Y')",
                        "hello\nworldY",
                        "hello\rworldY",
                        "hello\tworldY",
                        "hello\\worldY",
                        "hello\\nworldY",
                        "hello\\n\nworldY",
                        "hello\bworldY",
                        "hello\"worldY",
                        "hello\'worldY",
                        String.format("hello%sworldY", EscapeCharacter.BELL.getCHARACTER())
                ),
                testCase("SUBSTR(text, 0, 8)",
                        "hello\nwo",
                        "hello\rwo",
                        "hello\two",
                        "hello\\wo",
                        "hello\\nw",
                        "hello\\n\n",
                        "hello\bwo",
                        "hello\"wo",
                        "hello\'wo",
                        String.format("hello%swo", EscapeCharacter.BELL.getCHARACTER())
                )
        );
    }

    @DataProvider
    public static Object[][] provideLocate() {
        return toArray(
                testCase("'\n'", "6", "0", "0", "0", "0", "8", "0", "0", "0", "0"),
                testCase("'\r'", "0", "6", "0", "0", "0", "0", "0", "0", "0", "0"),
                testCase("'\t'", "0", "0", "6", "0", "0", "0", "0", "0", "0", "0"),
                testCase("'\\'", "0", "0", "0", "6", "6", "6", "0", "0", "0", "0"),
                testCase("'\\\\n'", "0", "0", "0", "0", "6", "6", "0", "0", "0", "0"),
                testCase("'\\\\n\n'", "0", "0", "0", "0", "0", "6", "0", "0", "0", "0"),
                testCase("'\b'", "0", "0", "0", "0", "0", "0", "6", "0", "0", "0"),
                testCase("'\"\"'", "0", "0", "0", "0", "0", "0", "0", "6", "0", "0"),
                testCase("'\'\''", "0", "0", "0", "0", "0", "0", "0", "0", "6", "0"),
                testCase(String.format("'%s'", EscapeCharacter.BELL.getCHARACTER()),
                        "0", "0", "0", "0", "0", "0", "0", "0", "0", "6")
        );
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideReplace",
            description = "Test REPLACE() function with escaped characters"
    )
    public void testReplace(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT REPLACE(text, %s) FROM \"%s\"", query, METRIC_NAME);
        assertSqlQueryRows(String.format("Fail to use REPLACE(text, %s) function", query), results, sqlQuery);
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
        final String sqlQuery = String.format("SELECT LOCATE(%s, text) FROM \"%s\"", query, METRIC_NAME);
        assertSqlQueryRows(String.format("Fail to use LOCATE(%s, text) function", query), results, sqlQuery);
    }

    @Issue("5600")
    @Test(
            description = "Test special characters in entity tags"
    )
    public void testEntityTags() {
        final Entity beforeUpdate = EntityMethod.getEntity(ENTITY_NAME);
        beforeUpdate.addTag(EscapeCharacter.NEW_LINE.name().toLowerCase(), "hello\nworld")
                .addTag(EscapeCharacter.CARRIAGE_RET.name().toLowerCase(), "hello\rworld")
                .addTag(EscapeCharacter.HORIZONTAL_TAB.name().toLowerCase(), "hello\tworld")
                .addTag(EscapeCharacter.BACKSLASH.name().toLowerCase(), "hello\\world")
                .addTag(EscapeCharacter.BACKSLASH_N.name().toLowerCase(), "hello\\nworld")
                .addTag(EscapeCharacter.BACKSLASH_N_NEW_LINE.name().toLowerCase(), "hello\\n\nworld")
                .addTag(EscapeCharacter.BACKSPACE.name().toLowerCase(), "hello\bworld")
                .addTag(EscapeCharacter.DOUBLE_QUOTE.name().toLowerCase(), "hello\"world")
                .addTag(EscapeCharacter.SINGLE_QUOTE.name().toLowerCase(), "hello\'world")
                .addTag(EscapeCharacter.BELL.name().toLowerCase(), String.format("hello%sworld", EscapeCharacter.BELL.getCHARACTER()));
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

    @RequiredArgsConstructor
    @Getter
    enum EscapeCharacter {
        NEW_LINE("\n"),
        BELL(Character.toString((char) 7)),
        CARRIAGE_RET("\r"),
        HORIZONTAL_TAB("\t"),
        BACKSLASH("\\"),
        BACKSLASH_N("\\n"),
        BACKSLASH_N_NEW_LINE("\\n\n"),
        BACKSPACE("\b"),
        DOUBLE_QUOTE("\""),
        SINGLE_QUOTE("\'");
        private final String CHARACTER;
    }
}
