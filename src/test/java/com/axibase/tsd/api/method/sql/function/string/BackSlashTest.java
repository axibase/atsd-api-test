package com.axibase.tsd.api.method.sql.function.string;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
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
    public static Object[][] provideReplaceResults() {
        final String replaceFormat = "REPLACE(text, '%s', 'Y')";
        final String[] queries = Stream.of(CHARACTERS).map((character) -> String.format(replaceFormat, character))
                .toArray(String[]::new);
        final Map<String, String[][]> results = new HashMap<>();
        for (final String character : CHARACTERS) {
            final String[][] strings = new String[CHARACTERS.length][1];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = toArray(String.format(FORMAT, CHARACTERS[i]).replace(character, "Y"));
            }
            results.put(character, strings);
        }
        final Object[][] result = new Object[CHARACTERS.length][CHARACTERS.length];
        for (int i = 0; i < CHARACTERS.length; i++) {
            result[i] = new Object[]{
                    queries[i], results.get(CHARACTERS[i])
            };
        }
        return result;
    }

    @Issue("5600")
    @Test(
            dataProvider = "provideReplaceResults"
    )
    public void testReplace(final String query, final String[][] results) {
        final String sqlQuery = String.format("SELECT %s FROM \"%s\"", query, METRIC_NAME);
        assertSqlQueryRows("Fail to replace an escaped character", results, sqlQuery);
    }
}
