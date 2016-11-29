package com.axibase.tsd.api.method.sql.function.period.filtering;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


public class SqlPeriodDataFilteringTest extends SqlMethod {
    private static final String TEST_PREFIX = "sql-period-data-filtering";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";


    @BeforeClass
    public static void prepareDataSet() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
            setData(Arrays.asList(
                    new Sample("2016-06-27T14:20:00.000Z", "4.0"),
                    new Sample("2016-06-27T14:22:00.000Z", "4.0"),
                    new Sample("2016-06-27T14:22:01.000Z", "4.0")
            ));
        }};
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
    }


    /*
      #2967
      period filtering different from series query API
     */

    /**
     * #2967
     */
    @Test
    public void testFirstValueOutOfBounds() {
        final String sqlQuery = String.format(
                "SELECT datetime, count(value) FROM '%s' %nWHERE entity = '%s' " +
                        "AND datetime >= '2016-06-27T14:20:01Z' and datetime < '2016-06-27T14:21:01Z' %n" +
                        "GROUP BY PERIOD(1 MINUTE)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();

        final List<List<String>> expectedTableRows = new ArrayList<>();
        /*
        Expected empty set, because period begin out of [start time; end time]
         */
        assertEquals(expectedTableRows, resultTableRows);
    }

    /**
     * #2967
     */
    @Test
    public void testLastValueOutOfBounds() {
        final String sqlQuery = String.format(
                "SELECT datetime, count(value) FROM '%s' %nWHERE entity = '%s' %n" +
                        "AND datetime >= '2016-06-27T14:21:00Z' and datetime < '2016-06-27T14:22:01Z' %n" +
                        "GROUP BY PERIOD(1 MINUTE)",
                TEST_METRIC_NAME, TEST_ENTITY_NAME
        );

        final List<List<String>> resultTableRows =
                queryResponse(sqlQuery)
                        .readEntity(StringTable.class)
                        .getRows();
        final List<List<String>> expectedTableRows = Collections.singletonList(
                Arrays.asList("2016-06-27T14:22:00.000Z", "1")
        );
        assertEquals(expectedTableRows, resultTableRows);
    }
}
