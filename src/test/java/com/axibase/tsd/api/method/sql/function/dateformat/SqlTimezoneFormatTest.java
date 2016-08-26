package com.axibase.tsd.api.method.sql.function.dateformat;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;


public class SqlTimezoneFormatTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-date-format-timezone-";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";

    @BeforeClass
    public static void prepareData() throws Exception {
        SeriesMethod.insertSeriesCheck(
                new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME) {{
                    addTag("a", "b");
                    addData(new Sample("2016-06-03T09:23:00.000Z", "7"));
                }}
        );
    }

    /*
      #2904
     */

    /**
     * #2904
     */
    @Test
    public void testSimpleDateFormatWithZeroZone() throws JSONException {
        String sqlQuery = String.format(
                "SELECT time, date_format(time, \"yyyy-MM-dd'T'HH:mm:ssZ\") AS 'f-date'" +
                        "FROM '%s' WHERE datetime = '2016-06-03T09:23:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Collections.singletonList(
                Util.formatDate(Util.parseDate("2016-06-03T09:23:00.000Z"), "yyyy-MM-dd'T'HH:mm:ssZ")
        );

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * #2904
     */
    @Test
    public void testSimpleDateFormatWithoutMs() {
        String sqlQuery = String.format(
                "SELECT time, date_format(time, \"yyyy-MM-dd'T'HH:mm:ss\") AS 'f-date' FROM '%s' %n" +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Collections.singletonList(
                Util.formatDate(Util.parseDate("2016-06-03T09:23:00.000Z"), "yyyy-MM-dd'T'HH:mm:ss")
        );

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * #2904
     */
    @Test
    public void testSimpleDateFormatPST() {
        String sqlQuery = String.format(
                "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'PST') AS 'f-date' FROM '%s' %n" +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Collections.singletonList("2016-06-03 02:23:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * #2904
     */
    @Test
    public void testSimpleDateFormatGMT() {
        String sqlQuery = String.format(
                "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'GMT-08:00') AS 'f-date' FROM '%s' %n" +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Collections.singletonList("2016-06-03 01:23:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * #2904
     */
    @Test
    public void testSimpleDateFormatZeroZoneAndGMT() {
        String sqlQuery = String.format(
                "SELECT time,  date_format(time,'yyyy-MM-dd HH:mm:ss ZZ','PST') AS 'f-date'FROM '%s' %n" +
                        "WHERE datetime = '2016-06-03T09:23:00.000Z'",
                TEST_METRIC_NAME
        );

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Collections.singletonList("2016-06-03 02:23:00 -07:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }


}
