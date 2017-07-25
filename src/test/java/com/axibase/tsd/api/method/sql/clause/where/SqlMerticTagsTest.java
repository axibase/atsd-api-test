package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Mocks.entity;

public class SqlMerticTagsTest extends SqlTest {
    private final String TEST_METRIC = metric();

    @BeforeTest
    public void prepareData() throws Exception {
        String entity = entity();

        String[] tagValues = new String[] { null, "value1", "value2", "value3", "otherValue" };

        List<Series> seriesList = new ArrayList<>();
        for (int i = 0; i < tagValues.length; i++) {
            Series series = new Series(entity, TEST_METRIC);
            String tagValue = tagValues[i];
            if (tagValue != null) {
                series.addTag("tag", tagValue);
            }
            series.addSamples(new Sample(String.format("2017-01-01T00:0%S:00Z", i), i));

            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #4180
     */
    @Test
    public void testNoTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' ",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"null"},
                {"value1"},
                {"value2"},
                {"value3"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testIsNullTagFilter() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s' WHERE tags.tag IS NULL",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"0"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testIsNotNullTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag IS NOT NULL",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"value2"},
                {"value3"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testLikeTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag LIKE 'value?'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testLikeTagFilterAsterisk() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag LIKE '*2'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value2"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testNotLikeTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag NOT LIKE 'value?'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testNotLikeTagFilterAsterisk() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag NOT LIKE '*2'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"value3"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testRegexTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag REGEX 'value[1,2]{1}|.*Value'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"value2"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testEqualsTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag = 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testNotEqualsTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag != 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value2"},
                {"value3"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testGreaterTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag > 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testLessOrEqualsTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag <= 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"otherValue"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testNotNullAndLikeTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag LIKE 'value?' AND tags.tag IS NOT NULL",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"},
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testLikeAndGreaterTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag LIKE 'value?' AND tags.tag > 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testEqualsAndNotNullTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag IS NOT NULL AND tags.tag = 'value1'",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"value1"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }

    /**
     * #4180
     */
    @Test
    public void testIsNullAndLikeTagFilter() {
        String sqlQuery = String.format(
                "SELECT tags.tag FROM '%s' WHERE tags.tag LIKE 'value?' OR tags.tag IS NULL",
                TEST_METRIC
        );

        String[][] expectedRows = {
                {"null"},
                {"value1"},
                {"value2"},
                {"value3"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
