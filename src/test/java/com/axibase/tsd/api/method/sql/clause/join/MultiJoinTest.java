package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class MultiJoinTest extends SqlTest {
    private static final String TEST_ENTITY_NAME = entity();
    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_METRIC3_NAME = metric();
    private static final String TEST_METRIC4_NAME = metric();
    private static final String TEST_METRIC5_NAME = metric();
    private static final int TAGS_COUNT = 20;

    @BeforeClass
    public static void prepareData() throws Exception {
        String[] metrics = {
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME,
                TEST_METRIC4_NAME,
                TEST_METRIC5_NAME
        };

        List<Series> seriesList = new ArrayList<>(TAGS_COUNT * 5);
        for (int i = 0; i < TAGS_COUNT; i++) {
            for (String metric : metrics) {
                Series series = new Series(TEST_ENTITY_NAME, metric, "tag", String.valueOf(i));
                series.addSamples(new Sample("2010-01-01T00:00:00.000Z", 1));

                seriesList.add(series);
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * 3935
     */
    @Test
    public void testSimpleMultiJoinRequest() {
        String sqlQuery = String.format(
                "SELECT t1.tags\n" +
                        "FROM '%s' t1\n" +
                        "JOIN USING entity '%s' t2\n" +
                        "JOIN USING entity '%s' t3\n" +
                        "JOIN USING entity '%s' t4\n" +
                        "JOIN USING entity '%s' t5\n" +
                        "WHERE \n" +
                        "    t1.tags.tag = '1' AND\n" +
                        "    t2.tags.tag = '1' AND\n" +
                        "    t3.tags.tag = '1' AND\n" +
                        "    t4.tags.tag = '1' AND\n" +
                        "    t5.tags.tag = '1'",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                TEST_METRIC3_NAME,
                TEST_METRIC4_NAME,
                TEST_METRIC5_NAME
        );

        String[][] expectedRows = {
                {"tag=1"}
        };

        assertSqlQueryRows("Query with multiple joins gives wrong result", expectedRows, sqlQuery);
    }
}
