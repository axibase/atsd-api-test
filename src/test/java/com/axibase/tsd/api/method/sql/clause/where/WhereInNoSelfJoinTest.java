package com.axibase.tsd.api.method.sql.clause.where;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.TextSample;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

public class WhereInNoSelfJoinTest extends SqlTest {
    private static final String ENTITY_NAME = "br-1211";
    private static final String METRIC_NAME1 = "tv6.pack:r01";
    private static final String METRIC_NAME2 = "tv6.pack:r03";
    private static final String METRIC_NAME3 = "tv6.pack:r04";
    private static final String METRIC_NAME4 = "tv6.elapsed_time";
    private static final String METRIC_NAME5 = "tv6.unit_batchid";
    private static final String METRIC_NAME6 = "tv6.unit_procedure";

    private static final String QUERY_TEMPLATE = "SELECT t1.entity, t1.metric, t1.datetime, \n" +
            "  t1.value, t4.text AS \"Elapsed Time\", t5.text AS \"Unit Batch Id\", t6.text AS \"Unit Procedure\", \n" +
            "CASE interval_number()\n" +
            "  WHEN 0 THEN t5.text\n" +
            "  ELSE CONCAT(t5.text, '.', interval_number()) \n" +
            "END AS \"Unit Batch Number\"\n" +
            "  FROM atsd_series t1\n" +
            "    JOIN \"TV6.Elapsed_Time\" t4\n" +
            "    JOIN \"TV6.Unit_BatchID\" t5\n" +
            "    JOIN \"TV6.Unit_Procedure\" t6\n" +
            "WHERE %s\n" +
            "  AND t1.datetime BETWEEN (SELECT datetime FROM \"TV6.Unit_BatchID\" WHERE entity = " +
            "    'br-1211' AND (text = '800' OR LAG(text)='800'))\n" +
            "  AND t1.entity = 'br-1211'\n" +
            "WITH INTERPOLATE(60 SECOND, AUTO, OUTER, EXTEND, START_TIME)\n" +
            "  ORDER BY t1.metric, t1.datetime";
    String[][] QUERY_RESULT = {
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:01:20.000Z", "79.92743362831858", "26.0", "800", "Proc1", "800.1"},
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:02:20.000Z", "83.69734513274337", "35.0", "800", "Proc3", "800.1"},
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:03:10.000Z", "76.74205607476635", "95.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:04:10.000Z", "66.08785046728973", "155.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:05:10.000Z", "53.97397260273972", "215.0", "800", "Proc2", "800.2"},
            {"br-1211", "tv6.pack:r01", "2016-10-04T02:06:10.000Z", "53.37164179104477", "275.0", "800", "Proc2", "800.2"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:01:20.000Z", "39.23855421686747", "26.0", "800", "Proc1", "800.1"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:02:20.000Z", "44.05826771653543", "35.0", "800", "Proc3", "800.1"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:03:10.000Z", "40.51496062992126", "95.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:04:10.000Z", "36.262992125984255", "155.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:05:10.000Z", "39.530188679245285", "215.0", "800", "Proc2", "800.2"},
            {"br-1211", "tv6.pack:r03", "2016-10-04T02:06:10.000Z", "41.05483870967742", "275.0", "800", "Proc2", "800.2"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:01:20.000Z", "20.77326732673267", "26.0", "800", "Proc1", "800.1"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:02:20.000Z", "21.227906976744187", "35.0", "800", "Proc3", "800.1"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:03:10.000Z", "24.451968503937007", "95.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:04:10.000Z", "24.924409448818896", "155.0", "800", "Proc1", "800.2"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:05:10.000Z", "20.321951219512197", "215.0", "800", "Proc2", "800.2"},
            {"br-1211", "tv6.pack:r04", "2016-10-04T02:06:10.000Z", "20.98048780487805", "275.0", "800", "Proc2", "800.2"},
    };

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_NAME, METRIC_NAME1);
        series1.addSamples(
                new Sample("2016-10-04T01:58:12.000Z", new BigDecimal("90.4")),
                new Sample("2016-10-04T02:00:05.000Z", new BigDecimal("97.7")),
                new Sample("2016-10-04T02:00:35.000Z", new BigDecimal("77.1")),
                new Sample("2016-10-04T02:02:28.000Z", new BigDecimal("84.2")),
                new Sample("2016-10-04T02:04:15.000Z", new BigDecimal("65.2")),
                new Sample("2016-10-04T02:05:28.000Z", new BigDecimal("50.3")),
                new Sample("2016-10-04T02:07:42.000Z", new BigDecimal("60.1")),
                new Sample("2016-10-04T02:08:28.000Z", new BigDecimal("80.3")),
                new Sample("2016-10-04T02:09:16.000Z", new BigDecimal("87.1")),
                new Sample("2016-10-04T02:11:11.000Z", new BigDecimal("99.9"))
        );

        Series series2 = new Series(ENTITY_NAME, METRIC_NAME2);
        series2.addSamples(
                new Sample("2016-10-04T02:00:14.000Z", new BigDecimal("47.7")),
                new Sample("2016-10-04T02:00:55.000Z", new BigDecimal("37.1")),
                new Sample("2016-10-04T02:02:18.000Z", new BigDecimal("44.2")),
                new Sample("2016-10-04T02:04:25.000Z", new BigDecimal("35.2")),
                new Sample("2016-10-04T02:05:18.000Z", new BigDecimal("40.3")),
                new Sample("2016-10-04T02:07:22.000Z", new BigDecimal("42.1")),
                new Sample("2016-10-04T02:08:28.000Z", new BigDecimal("46.3")),
                new Sample("2016-10-04T02:09:26.000Z", new BigDecimal("27.1")),
                new Sample("2016-10-04T02:10:11.000Z", new BigDecimal("49.9"))
        );

        Series series3 = new Series(ENTITY_NAME, METRIC_NAME3);
        series3.addSamples(
                new Sample("2016-10-04T01:59:12.000Z", new BigDecimal("20.0")),
                new Sample("2016-10-04T02:00:14.000Z", new BigDecimal("27.7")),
                new Sample("2016-10-04T02:01:55.000Z", new BigDecimal("17.1")),
                new Sample("2016-10-04T02:02:38.000Z", new BigDecimal("24.2")),
                new Sample("2016-10-04T02:04:45.000Z", new BigDecimal("25.2")),
                new Sample("2016-10-04T02:05:08.000Z", new BigDecimal("20.3")),
                new Sample("2016-10-04T02:07:52.000Z", new BigDecimal("22.1")),
                new Sample("2016-10-04T02:08:18.000Z", new BigDecimal("26.3")),
                new Sample("2016-10-04T02:09:46.000Z", new BigDecimal("17.1")),
                new Sample("2016-10-04T02:10:21.000Z", new BigDecimal("19.9"))
        );

        Series series4 = new Series(ENTITY_NAME, METRIC_NAME4);
        series4.addSamples(
                new TextSample("2016-10-04T02:00:00.000Z", "475.0"),
                new TextSample("2016-10-04T02:01:00.000Z", "26.0"),
                new TextSample("2016-10-04T02:02:00.000Z", "35.0"),
                new TextSample("2016-10-04T02:03:00.000Z", "95.0"),
                new TextSample("2016-10-04T02:04:00.000Z", "155.0"),
                new TextSample("2016-10-04T02:05:00.000Z", "215.0"),
                new TextSample("2016-10-04T02:06:00.000Z", "275.0"),
                new TextSample("2016-10-04T02:07:00.000Z", "335.0"),
                new TextSample("2016-10-04T02:08:00.000Z", "395.0"),
                new TextSample("2016-10-04T02:09:00.000Z", "455.0"),
                new TextSample("2016-10-04T02:10:00.000Z", "51.0")
        );

        Series series5 = new Series(ENTITY_NAME, METRIC_NAME5);
        series5.addSamples(
                new TextSample("2016-10-04T01:52:05.000Z", "700"),
                new TextSample("2016-10-04T02:00:34.000Z", "Inactive"),
                new TextSample("2016-10-04T02:01:20.000Z", "800"),
                new TextSample("2016-10-04T02:03:05.000Z", "Inactive"),
                new TextSample("2016-10-04T02:03:10.000Z", "800"),
                new TextSample("2016-10-04T02:07:05.000Z", "Inactive"),
                new TextSample("2016-10-04T02:09:09.000Z", "900"),
                new TextSample("2016-10-04T02:12:30.000Z", "Inactive")
        );

        Series series6 = new Series(ENTITY_NAME, METRIC_NAME6);
        series6.addSamples(
            new TextSample("2016-10-04T01:57:08.000Z", "Proc3"),
            new TextSample("2016-10-04T02:00:34.000Z", "Inactive"),
            new TextSample("2016-10-04T02:01:20.000Z", "Proc1"),
            new TextSample("2016-10-04T02:01:59.000Z", "Proc2"),
            new TextSample("2016-10-04T02:02:20.000Z", "Proc3"),
            new TextSample("2016-10-04T02:03:05.000Z", "Inactive"),
            new TextSample("2016-10-04T02:03:10.000Z", "Proc1"),
            new TextSample("2016-10-04T02:04:59.000Z", "Proc2"),
            new TextSample("2016-10-04T02:06:20.000Z", "Proc3"),
            new TextSample("2016-10-04T02:07:05.000Z", "Inactive"),
            new TextSample("2016-10-04T02:09:09.000Z", "Proc1"),
            new TextSample("2016-10-04T02:12:30.000Z", "Inactive")
        );

        SeriesMethod.insertSeriesCheck(series1, series2, series3,
                series4, series5, series6);
    }

    /**
     * #4149
     */
    @Test
    public void testWhereInMertricsNoSelfJoin() {
        String sqlQuery = String.format(QUERY_TEMPLATE, "t1.metric IN metrics('br-1211')");
 
        assertSqlQueryRows("Wrong result when metrics(...) contains metric to join with", QUERY_RESULT, sqlQuery);
    }

    /**
     * #4149
     */
    @Test
    public void testWhereLikeNoSelfJoin() {
        String sqlQuery = String.format(QUERY_TEMPLATE, "t1.metric LIKE ('tv6.pack%%')");

        assertSqlQueryRows("Wrong result when LIKE matches metric to join with", QUERY_RESULT, sqlQuery);
    }
}
