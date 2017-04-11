package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;

public class LastAndFirstFunctionTagsTest extends SqlTest {
    private static final String TEST_METRIC = metric();

    @BeforeClass
    public void prepareData() throws Exception {
        String testEntity = entity();
        Entity entity = new Entity(testEntity);
        entity.addTag("literal_tag", "value");
        entity.addTag("numeric_tag", "123");
        EntityMethod.createOrReplaceEntityCheck(entity);

        Registry.Metric.register(TEST_METRIC);
        Series series = new Series();
        series.setEntity(testEntity);
        series.setMetric(TEST_METRIC);
        series.addData(Mocks.SAMPLE);
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3856
     */
    @Test
    public void testLastFunctionWithLiteralTags() {
        String sqlQuery = String.format(
                "SELECT LAST(entity.tags.literal_tag) FROM '%s'",
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
                "SELECT LAST(entity.tags.numeric_tag) FROM '%s'",
                TEST_METRIC);

        String[][] expectedRows = {{"123"}};

        assertSqlQueryRows("Wrong result for LAST function with numeric tag value", expectedRows, sqlQuery);
    }

    /**
     * #3856
     */
    @Test
    public void testFirstFunctionWithLiteralTags() {
        String sqlQuery = String.format(
                "SELECT FIRST(entity.tags.literal_tag) FROM '%s'",
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
                "SELECT FIRST(entity.tags.numeric_tag) FROM '%s'",
                TEST_METRIC);

        String[][] expectedRows = {{"123"}};

        assertSqlQueryRows("Wrong result for FIRST function with numeric tag value", expectedRows, sqlQuery);
    }
}
