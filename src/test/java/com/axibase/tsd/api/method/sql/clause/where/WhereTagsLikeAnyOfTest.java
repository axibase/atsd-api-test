package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.TestUtil.TestNames;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

public class WhereTagsLikeAnyOfTest extends SqlTest {
    private static final String METRIC_NAME = TestNames.metric();

    @BeforeClass
    private static void prepareData() throws Exception {
        Registry.Metric.register(METRIC_NAME);

        Series series1 = Mocks.series();
        series1.setMetric(METRIC_NAME);
        series1.setTags(Collections.singletonMap("tag", "ab"));

        Series series2 = Mocks.series();
        series2.setMetric(METRIC_NAME);
        series2.setTags(Collections.singletonMap("tag", "ac"));

        Series series3 = Mocks.series();
        series3.setMetric(METRIC_NAME);
        series3.setTags(Collections.singletonMap("tag", "ad"));

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    /**
     * #4034
     */
    @Test
    public void testWhereTagsAny() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM '%s' " +
                        "WHERE tags.tag LIKE '*'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal LIKE pattern",
                expectedRows, sqlQuery);
    }

    /**
     * #4034
     */
    @Test
    public void testWhereTagsAnyOrAny() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM '%s' " +
                        "WHERE tags.tag LIKE '*' " +
                        "OR tags.tag LIKE '*'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal LIKE pattern twice",
                expectedRows, sqlQuery);
    }

    /**
     * #4034
     */
    @Test
    public void testWhereTagsAnyOrAnyOf() {
        String sqlQuery = String.format(
                "SELECT tags.tag " +
                        "FROM '%s' " +
                        "WHERE tags.tag LIKE '*' " +
                        "OR tags.tag LIKE '*b'",
                METRIC_NAME
        );

        String[][] expectedRows = {
                {"ab"},
                {"ac"},
                {"ad"},
        };

        assertSqlQueryRows("Wrong result when filtering tags by universal and custom LIKE pattern",
                expectedRows, sqlQuery);
    }
}
