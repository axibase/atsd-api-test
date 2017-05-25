package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.*;

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
                series.addSamples(Mocks.SAMPLE);

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

    /**
     * #4157
     */
    @Test
    public void testOuterJoinUsingEntity() throws Exception {
        Series series1 = new Series();
        series1.setEntity(TEST_ENTITY_NAME);
        series1.setMetric(TEST_METRIC1_NAME);
        series1.addSamples(new Sample("2017-01-03T12:00:00.000Z", "3"));
        series1.addTag("t1", "tag");

        Series series2 = new Series();
        series2.setEntity(TEST_ENTITY_NAME);
        series2.setMetric(TEST_METRIC1_NAME);
        series2.addSamples(
                new Sample("2017-01-02T12:00:00.000Z", "2"),
                new Sample("2017-01-04T12:00:00.000Z", "4")
        );

        Series series3 = new Series();
        series3.setEntity(TEST_ENTITY_NAME);
        series3.setMetric(TEST_METRIC2_NAME);
        series3.addSamples(new Sample("2017-01-03T12:00:00.000Z", "5"));
        series3.addTag("t2", "tag");

        Series series4 = new Series();
        series4.setEntity(TEST_ENTITY_NAME);
        series4.setMetric(TEST_METRIC2_NAME);
        series4.addSamples(
                new Sample("2017-01-04T12:00:00.000Z", "6"),
                new Sample("2017-01-05T12:00:00.000Z", "7")
        );

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);

        String sqlQuery = String.format(
                "SELECT " +
                "    t1.value, t2.value, " +
                "    t1.tags, t2.tags, " +
                "    t1.datetime, t2.datetime " +
                "FROM '%s' t1 " +
                "OUTER JOIN USING ENTITY '%s' t2 " +
                "WHERE t1.datetime BETWEEN '2017-01-02T12:00:00.000Z' AND '2017-01-06T12:00:00.000Z'",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME);

        String[][] expectedRows = new String[][] {
                {"2", "null", "null", "null", "2017-01-02T12:00:00.000Z", "null"},
                {"3", "5", "t1=tag", "t2=tag", "2017-01-03T12:00:00.000Z", "2017-01-03T12:00:00.000Z"},
                {"4", "6", "null", "null", "2017-01-04T12:00:00.000Z", "2017-01-04T12:00:00.000Z"},
                {"null", "7", "null", "null", "null", "2017-01-05T12:00:00.000Z"}
        };

        assertSqlQueryRows(expectedRows, sqlQuery);
    }
}
