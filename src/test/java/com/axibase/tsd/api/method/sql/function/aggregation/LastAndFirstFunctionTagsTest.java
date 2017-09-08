package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class LastAndFirstFunctionTagsTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        String testEntity = entity();
        Entity entity = new Entity(testEntity);
        entity.addTag("literal_tag", "value");
        entity.addTag("numeric_tag", "123");

        Series series = new Series(testEntity, TEST_METRIC);
        series.addSamples(
                Sample.ofDateInteger("2017-01-01T09:30:00.000Z", 1),
                Sample.ofDateInteger("2017-01-01T10:30:00.000Z", 1),
                Sample.ofDateInteger("2017-01-01T11:30:00.000Z", 2),
                Sample.ofDateInteger("2017-01-01T12:30:00.000Z", 2),
                Sample.ofDateInteger("2017-01-01T13:30:00.000Z", 2)
        );

        EntityMethod.createOrReplaceEntityCheck(entity);

        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3856
     */
    @Test
    public void testLastFunctionWithLiteralTags() {
        String sqlQuery = String.format(
                "SELECT LAST(entity.tags.literal_tag) FROM \"%s\"",
                TEST_METRIC);

        String[][] expectedRows = {{"NaN"}};

        assertSqlQueryRows("Wrong result for LAST function with literal tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testLastFunctionWithNumericTags() {
        String sqlQuery = String.format(
                "SELECT LAST(entity.tags.numeric_tag) FROM \"%s\"",
                TEST_METRIC);

        String[][] expectedRows = {{"123"}};

        assertSqlQueryRows("Wrong result for LAST function with numeric tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testLastFunctionWithGroupBy() {
        String sqlQuery = String.format(
                "SELECT " +
                "LAST(entity.tags.literal_tag), " +
                "LAST(entity.tags.numeric_tag), " +
                "COUNT(*) " +
                "FROM \"%s\" " +
                "GROUP BY value",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN", "123", "2"},
                {"NaN", "123", "3"}
        };

        assertSqlQueryRows("Wrong result for LAST function with numeric tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testFirstFunctionWithLiteralTags() {
        String sqlQuery = String.format(
                "SELECT FIRST(entity.tags.literal_tag) FROM \"%s\"",
                TEST_METRIC);

        String[][] expectedRows = {{"NaN"}};

        assertSqlQueryRows("Wrong result for FIRST function with literal tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testFirstFunctionWithNumericTags() {
        String sqlQuery = String.format(
                "SELECT FIRST(entity.tags.numeric_tag) FROM \"%s\"",
                TEST_METRIC);

        String[][] expectedRows = {{"123"}};

        assertSqlQueryRows("Wrong result for FIRST function with numeric tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testFirstFunctionWithGroupBy() {
        String sqlQuery = String.format(
                "SELECT " +
                        "FIRST(entity.tags.literal_tag), " +
                        "FIRST(entity.tags.numeric_tag), " +
                        "COUNT(*) " +
                        "FROM \"%s\" " +
                        "GROUP BY value",
                TEST_METRIC);

        String[][] expectedRows = {
                {"NaN", "123", "2"},
                {"NaN", "123", "3"}
        };

        assertSqlQueryRows("Wrong result for LAST function with numeric tag value", expectedRows, sqlQuery);
    }
}
