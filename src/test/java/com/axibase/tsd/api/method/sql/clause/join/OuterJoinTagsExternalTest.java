package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class OuterJoinTagsExternalTest extends SqlTest {
    private static final String METRIC_NO_TAGS_1 = metric();
    private static final String METRIC_NO_TAGS_2 = metric();
    private static final String METRIC_WITH_TAGS = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        String entityName = entity();

        /* Create two metrics, because self-join is disallowed*/
        Series series1 = new Series(entityName, METRIC_NO_TAGS_1);
        series1.addSamples(Mocks.SAMPLE);

        Series series2 = new Series(entityName, METRIC_NO_TAGS_2);
        series2.addSamples(Mocks.SAMPLE);

        Series series3 = new Series(entityName, METRIC_WITH_TAGS, "tag1", "abc");
        series3.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series1, series2, series3);
    }

    /**
     * #4058
     */
    @Test
    public void testOuterJoinEmptyTagsExternal() {
        String sqlQuery = String.format(
                "SELECT \"%1$s\".tags FROM \"%1$s\" OUTER JOIN \"%2$s\" " +
                        "OPTION (ROW_MEMORY_THRESHOLD 0)",
                METRIC_NO_TAGS_1,
                METRIC_NO_TAGS_2
        );

        String[][] expectedRows = {{"null"}};

        assertSqlQueryRows(
                "Incorrect result for metric.tags in outer join with empty tags (external memory)",
                expectedRows,
                sqlQuery
        );
    }


    /**
     * #4058
     */
    @Test
    public void testOuterJoinPartiallyEmptyTagsExternal() {
        String sqlQuery = String.format(
                "SELECT \"%1$s\".tags FROM \"%1$s\" OUTER JOIN \"%2$s\" " +
                        "OPTION (ROW_MEMORY_THRESHOLD 0)",
                METRIC_WITH_TAGS,
                METRIC_NO_TAGS_1
        );

        String[][] expectedRows = {{"null"}, {"tag1=abc"}};

        assertSqlQueryRows(
                "Incorrect result for metric.tags in outer join with empty and non-empty tags (external memory)",
                expectedRows,
                sqlQuery
        );
    }
}
