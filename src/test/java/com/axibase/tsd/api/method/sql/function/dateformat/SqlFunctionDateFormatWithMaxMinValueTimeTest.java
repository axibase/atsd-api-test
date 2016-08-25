package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlFunctionDateFormatWithMaxMinValueTimeTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-function-date-format-with-max-min-value-time-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        series.addData(new Sample("2015-06-03T09:23:01.000Z", "2"));
        series.addData(new Sample("2016-06-03T09:23:02.000Z", "1"));
        SeriesMethod.insertSeriesCheck(series);
    }


    /**
     * Issues #3184
     */
    @Test
    public void testMaxValueTime() {
        String sqlQuery = String.format(
                "SELECT date_format(MAX_VALUE_TIME(value),'yyyy') FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("2015")
        );

        assertTableRows(expectedRows, resultTable);
    }


    /**
     * Issues #3184
     */
    @Test
    public void testMinValueTime() {
        String sqlQuery = String.format(
                "SELECT date_format(MIN_VALUE_TIME(value),'yyyy') FROM '%s'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery)
                .readEntity(StringTable.class);
        List<List<String>> expectedRows = Collections.singletonList(
                Collections.singletonList("2016")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
