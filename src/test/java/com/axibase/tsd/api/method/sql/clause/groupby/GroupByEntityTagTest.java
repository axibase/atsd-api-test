package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class GroupByEntityTagTest extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] tags = {"1", "-1", "-3.14", "word", "word1 word2_"};

        Metric metric = new Metric(TEST_METRIC_NAME);
        MetricMethod.createOrReplaceMetricCheck(metric);

        ZonedDateTime seriesStartDate = ZonedDateTime.parse("2016-01-01T00:00:00Z");

        for (int i = 0; i < tags.length; i++) {
            String testEntityNameTagsCase = entity();
            Entity entity = new Entity(testEntityNameTagsCase);
            entity.addTag("tagname", tags[i]);
            EntityMethod.createOrReplaceEntityCheck(entity);

            Series series = new Series();
            series.setEntity(testEntityNameTagsCase);
            series.setMetric(TEST_METRIC_NAME);
            series.addSamples(
                    Sample.ofDateInteger(seriesStartDate.plusSeconds(i).toString(), i),
                    Sample.ofDateInteger(seriesStartDate.plusSeconds(i + 60).toString(), i + 1));
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    @Issue("3795")
    @Test
    public void testGroupByWithoutAggregations() throws Exception {
        String sqlQuery = String.format(
                        "SELECT entity.tags.tagname " +
                        "FROM \"%s\" " +
                        "GROUP BY entity.tags.tagname " +
                        "ORDER BY 1 ASC",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"-1"},
                {"-3.14"},
                {"1"},
                {"word"},
                {"word1 word2_"}
        };

        assertSqlQueryRows("GROUP BY entity tag without aggregate function gives wrong result", expectedRows, sqlQuery);
    }

    @Issue("3795")
    @Test
    public void testGroupByWithSum() throws Exception {
        String sqlQuery = String.format(
                        "SELECT entity.tags.tagname, sum(value) " +
                        "FROM \"%s\" " +
                        "GROUP BY entity.tags.tagname " +
                        "ORDER BY 1 ASC",
                TEST_METRIC_NAME
        );

        String[][] expectedRows = {
                {"-1", "3"},
                {"-3.14", "5"},
                {"1", "1"},
                {"word", "7"},
                {"word1 word2_", "9"}
        };

        assertSqlQueryRows("GROUP BY entity tag with aggregate function gives wrong result", expectedRows, sqlQuery);
    }
}
