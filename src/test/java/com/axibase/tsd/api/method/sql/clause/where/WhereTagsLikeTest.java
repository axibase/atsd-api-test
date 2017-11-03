package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WhereTagsLikeTest extends SqlTest {
    private static final String METRIC_NAME1 = Mocks.metric();
    private static final String METRIC_NAME2 = Mocks.metric();
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();

    @BeforeClass
    public void prepareData() throws Exception {
        Entity entity1 = new Entity(ENTITY_NAME1).addTag("t1", "Tag1").addTag("t2", "Tag2");
        Entity entity2 = new Entity(ENTITY_NAME2).addTag("t3", "Tag3").addTag("t4", "Tag4");

        Metric metric1 = new Metric(METRIC_NAME1).addTag("t5", "Tag5").addTag("t6", "Tag6");

        Metric metric2 = new Metric(METRIC_NAME2).addTag("t7", "Tag7").addTag("t8", "Tag8");

        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME1).addTag("t9", "Tag9").addTag("t10", "Tag10");
        series1.addSamples(Sample.ofDateInteger("2017-10-01T00:00:00.000Z", 1));

        Series series2 = new Series(ENTITY_NAME1, METRIC_NAME2).addTag("t11", "Tag11").addTag("t12", "Tag12");
        series2.addSamples(Sample.ofDateInteger("2017-10-02T00:00:00.000Z", 2));

        Series series3 = new Series(ENTITY_NAME2, METRIC_NAME1).addTag("t13", "Tag13").addTag("t14", "Tag14");
        series3.addSamples(Sample.ofDateInteger("2017-10-03T00:00:00.000Z", 3));

        Series series4 = new Series(ENTITY_NAME2, METRIC_NAME2).addTag("t15", "Tag15").addTag("t16", "Tag16");
        series4.addSamples(Sample.ofDateInteger("2017-10-04T00:00:00.000Z", 4));

        EntityMethod.createOrReplaceEntityCheck(entity1);
        EntityMethod.createOrReplaceEntityCheck(entity2);

        MetricMethod.createOrReplaceMetricCheck(metric1);
        MetricMethod.createOrReplaceMetricCheck(metric2);

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE tags LIKE works"
    )
    public void testWhereTagsLike() {
        String sqlQuery = String.format(
                "SELECT metric, value, tags " +
                        "FROM \"%s\" " +
                        "WHERE tags LIKE '%%Tag9%%'",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {METRIC_NAME1, "1", "t10=Tag10;t9=Tag9"}
        };

        assertSqlQueryRows("Wrong result for tags LIKE filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE tags REGEX works"
    )
    public void testWhereTagsRegex() {
        String sqlQuery = String.format(
                "SELECT metric, value, tags " +
                        "FROM \"%s\" " +
                        "WHERE tags REGEX '.*Tag(10|13).*' " +
                        "ORDER BY value",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {METRIC_NAME1, "1", "t10=Tag10;t9=Tag9"},
                {METRIC_NAME1, "3", "t13=Tag13;t14=Tag14"}
        };

        assertSqlQueryRows("Wrong result for tags REGEX filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE tags LIKE works for atsd_series"
    )
    public void testAtsdSeriesWhereTagsLike() {
        String sqlQuery = String.format(
                "SELECT metric, value, tags " +
                        "FROM atsd_series " +
                        "WHERE metric IN ('%s', '%s') AND tags LIKE '%%Tag11%%'",
                METRIC_NAME1, METRIC_NAME2
        );

        String[][] expectedRows = {
                {METRIC_NAME2, "2", "t11=Tag11;t12=Tag12"}
        };

        assertSqlQueryRows("Wrong result for tags LIKE filter with atsd_series", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE tags REGEX works for atsd_series"
    )
    public void testAtsdSeriesWhereTagsRegex() {
        String sqlQuery = String.format(
                "SELECT metric, value, tags " +
                        "FROM atsd_series " +
                        "WHERE metric IN ('%s', '%s') AND tags REGEX '.*Tag(11|13).*' " +
                        "ORDER BY value",
                METRIC_NAME1, METRIC_NAME2
        );

        String[][] expectedRows = {
                {METRIC_NAME2, "2", "t11=Tag11;t12=Tag12"},
                {METRIC_NAME1, "3", "t13=Tag13;t14=Tag14"}
        };

        assertSqlQueryRows("Wrong result for tags REGEX filter with atsd_series", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE metric.tags LIKE works for atsd_series"
    )
    public void testAtsdSeriesWhereMetricTagsLike() {
        String sqlQuery = String.format(
                "SELECT metric, value, metric.tags " +
                        "FROM atsd_series " +
                        "WHERE metric IN ('%s', '%s') AND metric.tags LIKE '%%Tag6%%' " +
                        "ORDER BY value",
                METRIC_NAME1, METRIC_NAME2
        );

        String[][] expectedRows = {
                {METRIC_NAME1, "1", "t5=Tag5;t6=Tag6"},
                {METRIC_NAME1, "3", "t5=Tag5;t6=Tag6"}
        };

        assertSqlQueryRows("Wrong result for metric.tags LIKE filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE metric.tags REGEX works for atsd_series"
    )
    public void testAtsdSeriesWhereMetricTagsRegex() {
        String sqlQuery = String.format(
                "SELECT metric, value, metric.tags " +
                        "FROM atsd_series " +
                        "WHERE metric IN ('%s', '%s') AND metric.tags Regex '.*Tag(5|6).*' " +
                        "ORDER BY value",
                METRIC_NAME1, METRIC_NAME2
        );

        String[][] expectedRows = {
                {METRIC_NAME1, "1", "t5=Tag5;t6=Tag6"},
                {METRIC_NAME1, "3", "t5=Tag5;t6=Tag6"}
        };

        assertSqlQueryRows("Wrong result for metric.tags REGEX filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE entity.tags LIKE works"
    )
    public void testWhereEntityTagsLike() {
        String sqlQuery = String.format(
                "SELECT entity, value, entity.tags " +
                        "FROM \"%s\" " +
                        "WHERE entity.tags LIKE '%%Tag2%%' ",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, "1", "t1=Tag1;t2=Tag2"}
        };

        assertSqlQueryRows("Wrong result for entity.tags LIKE filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE entity.tags REGEX works"
    )
    public void testWhereEntityTagsRegex() {
        String sqlQuery = String.format(
                "SELECT entity, value, entity.tags " +
                        "FROM \"%s\" " +
                        "WHERE entity.tags REGEX '.*Tag(1|2).*' ",
                METRIC_NAME1
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, "1", "t1=Tag1;t2=Tag2"}
        };

        assertSqlQueryRows("Wrong result for entity.tags LIKE filter", expectedRows, sqlQuery);
    }

    @Issue("4648")
    @Test(
            description = "Test that WHERE entity.tags REGEX works for atsd_series"
    )
    public void testAtsdSeriesWhereEntityTagsRegex() {
        String sqlQuery = String.format(
                "SELECT entity, value, entity.tags " +
                        "FROM atsd_series " +
                        "WHERE metric IN ('%s', '%s') AND entity.tags REGEX '.*Tag(1|2).*' " +
                        "ORDER BY value",
                METRIC_NAME1, METRIC_NAME2
        );

        String[][] expectedRows = {
                {ENTITY_NAME1, "1", "t1=Tag1;t2=Tag2"},
                {ENTITY_NAME1, "2", "t1=Tag1;t2=Tag2"}
        };

        assertSqlQueryRows("Wrong result for entity.tags LIKE filter", expectedRows, sqlQuery);
    }
}
