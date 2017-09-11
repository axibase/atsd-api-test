package com.axibase.tsd.api.method.sql.clause.join;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SqlOuterJoinWithTagsTest extends SqlTest {

    private static final String TEST_METRIC1_NAME = metric();
    private static final String TEST_METRIC2_NAME = metric();
    private static final String TEST_ENTITY_NAME = entity();

    @BeforeClass
    public static void prepareData() throws Exception {
        String[] allTags = {"tag1", "tag2"};
        String[] allMetrics = {TEST_METRIC1_NAME, TEST_METRIC2_NAME};
        List<Series> seriesList = new ArrayList<>();

        for (String tagName : allTags) {
            for (String metricName : allMetrics) {
                Series series = new Series(TEST_ENTITY_NAME, metricName, tagName, tagName);
                series.addSamples(Mocks.SAMPLE);

                seriesList.add(series);
            }
        }

        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "t1", "tag");
        series.addSamples(Sample.ofDateInteger("2017-01-03T12:00:00.000Z", 3));
        seriesList.add(series);

        series = new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME);
        series.addSamples(
                Sample.ofDateInteger("2017-01-02T12:00:00.000Z", 2),
                Sample.ofDateInteger("2017-01-04T12:00:00.000Z", 4)
        );
        seriesList.add(series);

        series = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "t2", "tag");
        series.addSamples(Sample.ofDateInteger("2017-01-03T12:00:00.000Z", 5));
        seriesList.add(series);

        series = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME);
        series.addSamples(
                Sample.ofDateInteger("2017-01-04T12:00:00.000Z", 6),
                Sample.ofDateInteger("2017-01-05T12:00:00.000Z", 7)
        );
        seriesList.add(series);

        SeriesMethod.insertSeriesCheck(seriesList);
    }


    @Issue("3945")
    @Test
    public void testJoinUsingEntityWithTags() {
        String sqlQuery = String.format(
                "SELECT t1.tags, t2.tags " +
                "FROM \"%s\" t1 JOIN USING ENTITY \"%s\" t2 " +
                "WHERE t1.datetime = '%s' ",
                TEST_METRIC1_NAME,
                TEST_METRIC2_NAME,
                Mocks.ISO_TIME
        );

        String[][] expectedRows = {
                {"tag1=tag1", "tag1=tag1"},
                {"tag1=tag1", "tag2=tag2"},
                {"tag2=tag2", "tag1=tag1"},
                {"tag2=tag2", "tag2=tag2"}
        };

        assertSqlQueryRows("JOIN USING ENTITY with tags gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("4157")
    @Test
    public void testOuterJoinUsingEntity() throws Exception {
        String sqlQuery = String.format(
                "SELECT " +
                "    t1.value, t2.value, " +
                "    t1.tags, t2.tags, " +
                "    t1.datetime, t2.datetime " +
                "FROM \"%s\" t1 " +
                "OUTER JOIN USING ENTITY \"%s\" t2 " +
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
