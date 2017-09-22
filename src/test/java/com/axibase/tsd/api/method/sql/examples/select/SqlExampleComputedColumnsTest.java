package com.axibase.tsd.api.method.sql.examples.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlExampleComputedColumnsTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-select-computed-columns-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public void prepareData() throws Exception {
        Series series1 = new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME);
        series1.addSamples(
                Sample.ofDateDecimal("2016-08-15T07:24:02.000Z", new BigDecimal("4.3")),
                Sample.ofDateDecimal("2016-08-15T07:24:46.000Z", new BigDecimal("4.3")),
                Sample.ofDateDecimal("2016-08-15T07:25:02.000Z", new BigDecimal("5.4"))
        );

        Series series2 = new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME);
        series2.addSamples(
                Sample.ofDateDecimal("2016-08-15T07:24:46.000Z", new BigDecimal("10.1")),
                Sample.ofDateDecimal("2016-08-15T07:25:02.000Z", new BigDecimal("12.2")),
                Sample.ofDateDecimal("2016-08-15T07:25:46.000Z", new BigDecimal("10.1"))
        );

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    @Issue("3073")
    @Test(description = "Test for alias documentation example. " +
            "https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/select-computed-columns.md")
    public void testExample1() {
        String sqlQuery = String.format(
                "SELECT t1.datetime, t1.entity AS \"entity\", t1.value, t2.value, t1.value + t2.value AS total_cpu %n" +
                        "FROM \"%s\" t1 %nJOIN \"%s\" t2 %n" +
                        "WHERE t1.datetime >= '2016-08-15T07:24:00.000Z' AND t1.datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "AND t2.datetime >= '2016-08-15T07:24:00.000Z' AND t2.datetime < '2016-08-15T07:26:00.000Z' %n",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-08-15T07:24:46.000Z", TEST_ENTITY_NAME, "4.3", "10.1", "14.399999999999999"),
                Arrays.asList("2016-08-15T07:25:02.000Z", TEST_ENTITY_NAME, "5.4", "12.2", "17.6")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3073")
    @Test
    public void testExample2() {
        String sqlQuery = String.format(
                "SELECT t1.datetime, t1.entity, max(t1.value), max(t2.value), max(t1.value) + max(t2.value), max(t1.value + t2.value) AS max_total_cpu %n" +
                        "FROM \"%s\" t1 %nJOIN \"%s\" t2 %n" +
                        "WHERE t1.datetime >= '2016-08-15T07:24:00.000Z' AND t1.datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "AND t2.datetime >= '2016-08-15T07:24:00.000Z' AND t2.datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "GROUP BY t1.entity, t1.datetime %n",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-08-15T07:24:46.000Z", TEST_ENTITY_NAME, "4.3", "10.1", "14.399999999999999", "14.399999999999999"),
                Arrays.asList("2016-08-15T07:25:02.000Z", TEST_ENTITY_NAME, "5.4", "12.2", "17.6", "17.6")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3073")
    @Test
    public void testExample3() {
        String sqlQuery = String.format(
                "SELECT t1.datetime, t1.entity, max(t1.value), max(t2.value), max(t1.value) + max(t2.value), max(t1.value + t2.value) AS max_total_cpu %n" +
                        "FROM \"%s\" t1 %nJOIN \"%s\" t2 %n" +
                        "WHERE t1.datetime >= '2016-08-15T07:24:00.000Z' AND t1.datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "AND t2.datetime >= '2016-08-15T07:24:00.000Z' AND t2.datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "GROUP BY t1.entity, t1.datetime %nORDER BY max(t1.value) - min(t2.value) DESC",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-08-15T07:24:46.000Z", TEST_ENTITY_NAME, "4.3", "10.1", "14.399999999999999", "14.399999999999999"),
                Arrays.asList("2016-08-15T07:25:02.000Z", TEST_ENTITY_NAME, "5.4", "12.2", "17.6", "17.6")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }


    @Issue("3073")
    @Test
    public void testExample4() {
        String sqlQuery = String.format(
                "SELECT entity, min(value), max(value), max(value) - min(value) %n" +
                        "FROM \"%s\" %n" +
                        "WHERE datetime >= '2016-08-15T07:24:00.000Z' AND datetime < '2016-08-15T07:26:00.000Z' %n" +
                        "GROUP BY entity %nHAVING max(value) - min(value) > 1 %nORDER BY max(value) - min(value) DESC",
                TEST_METRIC1_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Collections.singletonList(
                Arrays.asList(TEST_ENTITY_NAME, "4.3", "5.4", "1.1000000000000005")
        );

        assertTableRowsExist(expectedRows, resultTable);
    }
}
