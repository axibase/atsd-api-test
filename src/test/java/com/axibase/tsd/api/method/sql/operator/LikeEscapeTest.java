package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class LikeEscapeTest extends SqlTest {
    private final String prefix = "_%*?\"=!@&^";
    private final String TEST_ENTITY = prefix + entity();
    private final String TEST_METRIC = prefix + metric();

    @BeforeClass
    public void prepareTest() throws Exception {
        Map<String, String> testTags = Collections.singletonMap(prefix + "tag", prefix + "value");

        Entity entity = new Entity(TEST_ENTITY)
                .setLabel(prefix + Mocks.LABEL)
                .setTags(testTags);

        Metric metric = new Metric(TEST_METRIC)
                .setLabel(prefix + Mocks.LABEL)
                .setDescription(prefix + Mocks.DESCRIPTION)
                .setTags(testTags);

        Series series = new Series(TEST_ENTITY, TEST_METRIC, testTags);
        series.addSamples(Sample.ofDateIntegerText(Mocks.ISO_TIME, 1, prefix + Mocks.TEXT_VALUE));

        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);

        // using custom checker due to a bug #4640
        SeriesMethod.insertSeriesCheck(
                Collections.singletonList(series),
                new AbstractCheck() {
                    @Override
                    public boolean isChecked() {
                        String query = String.format(
                                "SELECT COUNT(*) AS \"count\" FROM \"%s\"",
                                escapeName(TEST_METRIC));
                        StringTable table = SqlMethod.queryTable(query);
                        List<String> values = table.columnValues("count");
                        if (values.size() > 0 && values.get(0).equals("1")) {
                            return true;
                        }
                        return false;
                    }
                });
    }

    @DataProvider
    public Object[][] provideFields() {
        return new Object[][] {
                {"entity"},
                {"entity.name"},
                {"entity.label"},
                {"entity.tags"},
                {"entity.tags.\"" + escapeName(prefix + "tag") + "\""},
                {"metric"},
                {"metric.name"},
                {"metric.label"},
                {"metric.description"},
                {"metric.tags"},
                {"metric.tags.\"" + escapeName(prefix + "tag") + "\""},
                {"text"}
        };
    }

    @Issue("4544")
    @Test(dataProvider = "provideFields")
    public void testEscapeSyntax(String fieldName) {
        String query = String.format("SELECT datetime, value " +
                "FROM \"%s\" " +
                "WHERE %s LIKE '`_`%%`*`?`\"`=`!`@`&`^%%' ESCAPE '`'",
                escapeName(TEST_METRIC), fieldName
        );

        String[][] expectedRows = {
                {Mocks.ISO_TIME, "1"}
        };

        assertSqlQueryRows(expectedRows, query);
    }

    @Issue("4544")
    @Test
    public void testEscapeSyntaxRegexMetaCharacters() {
        String query = String.format("SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE entity LIKE '%%*=%%' ESCAPE '`'",
                escapeName(TEST_METRIC)
        );

        String[][] expectedRows = {
                {Mocks.ISO_TIME, "1"}
        };

        assertSqlQueryRows(expectedRows, query);
    }

    @Issue("4544")
    @Test
    public void testEscapeSyntaxIncorrectSequence() {
        String query = String.format("SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE text LIKE '`_`%%`*`?%%' ESCAPE '123'",
                escapeName(TEST_METRIC)
        );

        Response response = queryResponse(query);
        String errorMessage =
                "Invalid escape character: '123'. The escape character must be a character string of length 1";

        assertBadRequest(errorMessage, response);
    }

    @Issue("4544")
    @Test
    public void testEscapeSyntaxEmptySequence() {
        String query = String.format("SELECT datetime, value " +
                        "FROM \"%s\" " +
                        "WHERE text LIKE '`_`%%`*`?%%' ESCAPE ''",
                escapeName(TEST_METRIC)
        );

        Response response = queryResponse(query);
        String errorMessage =
                "Invalid escape character: ''. The escape character must be a character string of length 1";

        assertBadRequest(errorMessage, response);
    }

    private static String escapeName(String name) {
        return name.replace("\"", "\"\"");
    }
}
