package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;


public class SqlOuterJoinWithTagsTest extends SqlTest {

    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY_NAME);
        Registry.Metric.register(TEST_METRIC1_NAME);
        Registry.Metric.register(TEST_METRIC2_NAME);

        String[] allTags = {"tag1", "tag2"};
        String[] allMetrics = {TEST_METRIC1_NAME, TEST_METRIC2_NAME};
        List<Series> seriesList = new ArrayList<>();

        for (String tagName : allTags) {
            for (String metricName : allMetrics) {
                Series series = new Series();
                series.setEntity(TEST_ENTITY_NAME);
                series.setMetric(metricName);
                Map<String, String> tags = new HashMap<>();
                tags.put(tagName, tagName);
                series.setTags(tags);
                series.addData(Mocks.SAMPLE);

                seriesList.add(series);
            }
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    /**
     * #3945
     */
    @Test
    public void testJoinUsingEntityWithTags() {
        String sqlQuery = String.format(
                "SELECT t1.tags, t2.tags " +
                "FROM '%1$s' t1 JOIN USING ENTITY'%2$s' t2 ",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        String[][] expectedRows = {
                {"tag1=tag1", "tag1=tag1"},
                {"tag1=tag1", "tag2=tag2"},
                {"tag2=tag2", "tag1=tag1"},
                {"tag2=tag2", "tag2=tag2"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with tags gives wrong result", expectedRows, sqlQuery);
    }
}
