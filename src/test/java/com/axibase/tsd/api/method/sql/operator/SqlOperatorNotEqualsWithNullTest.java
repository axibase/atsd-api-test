package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SqlOperatorNotEqualsWithNullTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-operator-not-equals-with-null-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY1_NAME = TEST_PREFIX + "entity-1";
    private static final String TEST_ENTITY2_NAME = TEST_PREFIX + "entity-2";

    @BeforeClass
    public static void prepareData() throws Exception {
        Registry.Entity.register(TEST_ENTITY1_NAME);
        Registry.Entity.register(TEST_ENTITY2_NAME);
        Registry.Metric.register(TEST_METRIC_NAME);

        Series series1 = new Series(),
                series2 = new Series();
        series1.setEntity(TEST_ENTITY1_NAME);
        series1.setMetric(TEST_METRIC_NAME);
        series1.addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        series1.addTag("a", "b");

        series2.setEntity(TEST_ENTITY2_NAME);
        series2.setMetric(TEST_METRIC_NAME);
        series2.addData(new Sample("2016-06-29T08:00:00.000Z", "0"));
        series2.addTag("tag", "value");

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    /**
     * #3284
     */
    @Test
    public void testIgnoringNullObjectsComparison() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE tags.a != 'b'",
                TEST_METRIC_NAME
        );
        Response response = executeQuery(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.emptyList();
        assertTableRows(expectedRows, resultTable);
    }

    /**
     * #3284
     */
    @Test
    public void testIgnoringNullObjectsComparison1() {
        String sqlQuery = String.format(
                "SELECT value FROM '%s'%nWHERE tags.a != 'a'",
                TEST_METRIC_NAME
        );
        Response response = executeQuery(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0")
        );
        assertTableRows(expectedRows, resultTable);
    }
}
