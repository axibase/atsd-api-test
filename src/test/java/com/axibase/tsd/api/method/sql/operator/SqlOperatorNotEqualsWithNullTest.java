package com.axibase.tsd.api.method.sql.operator;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import io.qameta.allure.Issue;
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
        Series series1 = new Series(TEST_ENTITY1_NAME, TEST_METRIC_NAME, "a", "b");
        series1.addSamples(Sample.ofDateInteger("2016-06-29T08:00:00.000Z", 0));

        Series series2 = new Series(TEST_ENTITY2_NAME, TEST_METRIC_NAME, "tag", "value");
        series2.addSamples(Sample.ofDateInteger("2016-06-29T08:00:00.000Z", 0));

        SeriesMethod.insertSeriesCheck(Arrays.asList(series1, series2));
    }

    @Issue("3284")
    @Test
    public void testIgnoringNullObjectsComparison() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE tags.a != 'b'",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.emptyList();
        assertTableRowsExist(expectedRows, resultTable);
    }

    @Issue("3284")
    @Test
    public void testIgnoringNullObjectsComparison1() {
        String sqlQuery = String.format(
                "SELECT value FROM \"%s\"%nWHERE tags.a != 'a'",
                TEST_METRIC_NAME
        );
        Response response = queryResponse(sqlQuery);
        StringTable resultTable = response.readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("0")
        );
        assertTableRowsExist(expectedRows, resultTable);
    }
}
