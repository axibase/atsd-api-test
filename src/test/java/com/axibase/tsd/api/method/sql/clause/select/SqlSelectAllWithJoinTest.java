package com.axibase.tsd.api.method.sql.clause.select;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.Registry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SqlSelectAllWithJoinTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-select-all-join-";
    private static final String TEST_METRIC1_NAME = TEST_PREFIX + "metric-1";
    private static final String TEST_METRIC2_NAME = TEST_PREFIX + "metric-2";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";

    @BeforeClass
    public static void prepareData() throws Exception {
        List<Series> seriesList = new ArrayList<>();
        seriesList.add(new Series(TEST_ENTITY_NAME, TEST_METRIC1_NAME, "a", "b") {{
            addSamples(
                    new Sample("2016-06-03T09:23:00.000Z", 7),
                    new Sample("2016-06-03T09:24:00.000Z", 0),
                    new Sample("2016-06-03T09:25:00.000Z", 12),
                    new Sample("2016-06-03T09:26:00.000Z", new BigDecimal("10.3")),
                    new Sample("2016-06-03T09:27:00.000Z", 10)
            );
        }});

        seriesList.add(new Series(TEST_ENTITY_NAME, TEST_METRIC2_NAME, "a", "b", "b", "c") {{
            addSamples(
                    new Sample("2016-06-03T09:23:00.000Z", 5),
                    new Sample("2016-06-03T09:24:00.000Z", 7),
                    new Sample("2016-06-03T09:25:00.000Z", -2),
                    new Sample("2016-06-03T09:26:00.000Z", new BigDecimal("-2.1"))
            );
        }});

        SeriesMethod.insertSeriesCheck(seriesList);
    }

    /**
     * #3033
     */
    @Test
    public void testSelectAllColumnsWithAlias() {
        String sqlQuery = String.format(
                "SELECT * FROM '%s' t1 %nJOIN '%s' t2",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "t1.time",
                "t1.datetime",
                "t1.value",
                "t1.text",
                "t1.metric",
                "t1.entity",
                "t1.tags",
                "t2.time",
                "t2.datetime",
                "t2.value",
                "t2.text",
                "t2.metric",
                "t2.entity",
                "t2.tags");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3033
     */
    @Test
    public void testSelectAllColumnsWithoutAlias() {
        String sqlQuery =
                "SELECT * FROM 'sql-select-all-join-metric-1' " +
                        "JOIN 'sql-select-all-join-metric-2'";

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                TEST_METRIC1_NAME + ".time",
                TEST_METRIC1_NAME + ".datetime",
                TEST_METRIC1_NAME + ".value",
                TEST_METRIC1_NAME + ".text",
                TEST_METRIC1_NAME + ".metric",
                TEST_METRIC1_NAME + ".entity",
                TEST_METRIC1_NAME + ".tags",
                TEST_METRIC2_NAME + ".time",
                TEST_METRIC2_NAME + ".datetime",
                TEST_METRIC2_NAME + ".value",
                TEST_METRIC2_NAME + ".text",
                TEST_METRIC2_NAME + ".metric",
                TEST_METRIC2_NAME + ".entity",
                TEST_METRIC2_NAME + ".tags");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }


    /**
     * #3033
     */
    @Test
    public void testSelectAllColumnsFromTableAlias() {
        String sqlQuery = String.format(
                "SELECT t1.* FROM '%s' t1  %n JOIN '%s' t2",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "t1.time",
                "t1.datetime",
                "t1.value",
                "t1.text",
                "t1.metric",
                "t1.entity",
                "t1.tags");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }

    /**
     * #3033
     */
    @Test
    public void testSelectAllColumnsFromSeveralTableAliases() {
        String sqlQuery = String.format(
                "SELECT t1.*, t2.* FROM '%s' t1 JOIN '%s' t2",
                TEST_METRIC1_NAME, TEST_METRIC2_NAME
        );

        StringTable resultTable = queryResponse(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnNames = Arrays.asList(
                "t1.time",
                "t1.datetime",
                "t1.value",
                "t1.text",
                "t1.metric",
                "t1.entity",
                "t1.tags",
                "t2.time",
                "t2.datetime",
                "t2.value",
                "t2.text",
                "t2.metric",
                "t2.entity",
                "t2.tags");

        assertTableColumnsNames(expectedColumnNames, resultTable, true);
    }

}
