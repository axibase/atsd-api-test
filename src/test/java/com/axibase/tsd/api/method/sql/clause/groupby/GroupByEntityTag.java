package com.axibase.tsd.api.method.sql.clause.groupby;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.TestNames.entity;
import static com.axibase.tsd.api.util.Util.TestNames.metric;

public class GroupByEntityTag extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        String[] tags = {"1", "-1", "-3.14", "word", "word1 word2_"};

        Metric metric = new Metric(TEST_METRIC_NAME);
        MetricMethod.createOrReplaceMetricCheck(metric);

        for (int i = 0; i < tags.length; i++) {
            String testEntityNameTagsCase = entity();
            Entity entity = new Entity(testEntityNameTagsCase);
            entity.addTag("tagname", tags[i]);
            EntityMethod.createOrReplaceEntityCheck(entity);

            Series series = new Series();
            series.setEntity(testEntityNameTagsCase);
            series.setMetric(TEST_METRIC_NAME);
            series.addData(new Sample(Util.ISOFormat(1485525209086L + i), i));
            series.addData(new Sample(Util.ISOFormat(1485525289086L + i), i + 1));
            seriesList.add(series);
        }

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3795
     */
    @Test
    public void testGroupByWithoutAggregations() throws Exception {
        String sqlQuery = String.format(
                        "SELECT entity.tags.tagname " +
                        "FROM '%s' " +
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

    /**
     * #3795
     */
    @Test
    public void testGroupByWithSum() throws Exception {
        String sqlQuery = String.format(
                        "SELECT entity.tags.tagname, sum(value) " +
                        "FROM '%s' " +
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
