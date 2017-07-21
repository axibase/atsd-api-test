package com.axibase.tsd.api.method.sql.meta;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMetaTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.Test;

import javax.ws.rs.ProcessingException;

public class MetaTest extends SqlMetaTest {
    private static final String NON_EXISTENT_METRIC_1 = Mocks.metric();
    private static final String NON_EXISTENT_METRIC_2 = Mocks.metric();
    private static final String NON_EXISTENT_METRIC_3 = Mocks.metric();

    /**
     * #4363, #4374
     */
    @Test
    public void testAtsdSeriesMeta() {
        String sqlQuery = "SELECT * from 'atsd_series'";

        String[] expectedNames = {
                "time",
                "datetime",
                "value",
                "text",
                "metric",
                "entity",
                "tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                null,
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testSelectConstMeta() {
        String sqlQuery = "SELECT 1, 'a', 1.0, case 0 when 1 then 'a' else 5 end FROM 'atsd_series'";

        String[] expectedNames = {
                "1",
                "'a'",
                "1.0",
                "case 0\nwhen 1 then 'a'\nelse 5\nend"
        };

        String[] expectedTypes = {
                "bigint",
                "string",
                "double",
                "java_object"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testValueTimeAtsdSeriesMeta() {
        String sqlQuery = "SELECT time, datetime, value FROM 'atsd_series'";

        String[] expectedNames = {
                "time",
                "datetime",
                "value"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                null
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testValueTimeNonExistentMeta() {
        String sqlQuery = String.format(
                "SELECT time, datetime, value FROM '%s'",
                NON_EXISTENT_METRIC_1
        );

        String[] expectedNames = {
                "time",
                "datetime",
                "value"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testValueTimeExistentMeta() {
        String metric = Mocks.metric();
        Series seriesA = new Series(Mocks.entity(), metric);
        seriesA.addSamples(new Sample("2017-07-18T12:00:00.000Z", 1));

        String sqlQuery = String.format(
                "SELECT time, datetime, value FROM '%s'",
                NON_EXISTENT_METRIC_1
        );

        String[] expectedNames = {
                "time",
                "datetime",
                "value"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testNonExistentMeta() {
        String sqlQuery = String.format("SELECT * from '%s'", NON_EXISTENT_METRIC_1);

        String[] expectedNames = {
                "time",
                "datetime",
                "value",
                "text",
                "metric",
                "entity",
                "tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testNonExistentFromAtsdSeries() {
        String sqlQuery = String.format(
                "SELECT * FROM atsd_series " +
                        "WHERE metric='%s'",
                NON_EXISTENT_METRIC_1
        );

        String[] expectedNames = {
                "time",
                "datetime",
                "value",
                "text",
                "metric",
                "entity",
                "tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testNonExistentCustomColumnsMeta() {
        String sqlQuery = String.format("SELECT value, text, tags from '%s'", NON_EXISTENT_METRIC_1);

        String[] expectedNames = {
                "value",   "text",   "tags"
        };

        String[] expectedTypes = {
                "float",   "string", "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testNonExistentTagsExpansionMeta() {
        String sqlQuery = String.format("SELECT tags.* from '%s'", NON_EXISTENT_METRIC_1);

        String[] expectedNames = {};
        String[] expectedTypes = {};

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testMetaNonExistentMetricIn() {
        String sqlQuery = String.format(
                "SELECT *, tags.* FROM atsd_series " +
                        "WHERE metric in ('%s', '%s', '%s')",
                NON_EXISTENT_METRIC_1,
                NON_EXISTENT_METRIC_2,
                NON_EXISTENT_METRIC_3
        );

        String[] expectedNames = {
                "time",
                "datetime",
                "value",
                "text",
                "metric",
                "entity",
                "tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testMetaEmptyExistentTagsExpansion() throws Exception {
        Metric existentMetric = new Metric(Mocks.metric());
        MetricMethod.createOrReplaceMetricCheck(existentMetric);

        String sqlQuery = String.format("SELECT tags.* from '%s'", NON_EXISTENT_METRIC_1);

        String[] expectedNames = {};
        String[] expectedTypes = {};

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testMetaNonEmptyTagsExpansion() throws Exception {
        String metricA = Mocks.metric();
        Series seriesA = new Series(Mocks.entity(), metricA, "t1", "a");
        seriesA.addSamples(new Sample("2017-07-18T12:00:00.000Z", 1));

        String metricB  =Mocks.metric();
        Series seriesB = new Series(Mocks.entity(), metricB, "t2", "b");
        seriesB.addSamples(new Sample("2017-07-18T13:00:00.000Z", 2));

        SeriesMethod.insertSeriesCheck(seriesB, seriesA);

        String sqlQuery = String.format(
                "SELECT *, tags.* FROM atsd_series WHERE metric in ('%s', '%s')",
                seriesA.getMetric(),
                seriesB.getMetric()
        );

        String[] expectedNames = {
                "time",
                "datetime",
                "value",
                "text",
                "metric",
                "entity",
                "tags",
                /* Do we always have this order? */
                metricB + ".tags.t2",
                metricA + ".tags.t1"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test(expectedExceptions = ProcessingException.class)
    public void testMetaNonExistentSelfJoin() {
        String sqlQuery = String.format(
                "SELECT * from '%s' JOIN '%s'",
                NON_EXISTENT_METRIC_1,
                NON_EXISTENT_METRIC_1);

        queryMetaData(sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testMetaNonExistentJoin() {
        String sqlQuery = String.format(
                "SELECT * from '%s' JOIN '%s'",
                NON_EXISTENT_METRIC_1,
                NON_EXISTENT_METRIC_2
        );

        String[] expectedNames = {
                NON_EXISTENT_METRIC_1 + ".time",
                NON_EXISTENT_METRIC_1 + ".datetime",
                NON_EXISTENT_METRIC_1 + ".value",
                NON_EXISTENT_METRIC_1 + ".text",
                NON_EXISTENT_METRIC_1 + ".metric",
                NON_EXISTENT_METRIC_1 + ".entity",
                NON_EXISTENT_METRIC_1 + ".tags",

                NON_EXISTENT_METRIC_2 + ".time",
                NON_EXISTENT_METRIC_2 + ".datetime",
                NON_EXISTENT_METRIC_2 + ".value",
                NON_EXISTENT_METRIC_2 + ".text",
                NON_EXISTENT_METRIC_2 + ".metric",
                NON_EXISTENT_METRIC_2 + ".entity",
                NON_EXISTENT_METRIC_2 + ".tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string",

                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }

    /**
     * #4363, #4374
     */
    @Test
    public void testMetaNonExistentJoinUsingEntity() {
        String sqlQuery = String.format(
                "SELECT * from '%s' JOIN USING ENTITY '%s'",
                NON_EXISTENT_METRIC_1,
                NON_EXISTENT_METRIC_2
        );

        String[] expectedNames = {
                NON_EXISTENT_METRIC_1 + ".time",
                NON_EXISTENT_METRIC_1 + ".datetime",
                NON_EXISTENT_METRIC_1 + ".value",
                NON_EXISTENT_METRIC_1 + ".text",
                NON_EXISTENT_METRIC_1 + ".metric",
                NON_EXISTENT_METRIC_1 + ".entity",
                NON_EXISTENT_METRIC_1 + ".tags",

                NON_EXISTENT_METRIC_2 + ".time",
                NON_EXISTENT_METRIC_2 + ".datetime",
                NON_EXISTENT_METRIC_2 + ".value",
                NON_EXISTENT_METRIC_2 + ".text",
                NON_EXISTENT_METRIC_2 + ".metric",
                NON_EXISTENT_METRIC_2 + ".entity",
                NON_EXISTENT_METRIC_2 + ".tags"
        };

        String[] expectedTypes = {
                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string",

                "bigint",
                "xsd:dateTimeStamp",
                "float",
                "string",
                "string",
                "string",
                "string"
        };

        assertSqlMetaNamesAndTypes("", expectedNames, expectedTypes, sqlQuery);
    }
}
