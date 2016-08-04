package com.axibase.tsd.api.method.sql.date_format;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTimezoneFormatTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-date-format-timezone-";


    @BeforeClass
    public static void prepareData() throws IOException {
        Series testSeries = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        testSeries.addTag("a", "b");
        testSeries.addData(new Sample("2016-06-03T09:23:00.000Z", "7"));
        SeriesMethod.insertSeriesCheck(testSeries);
    }

    /**
     * Following tests related to issue #2904
     */

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatWithZeroZone() {
        String sqlQuery = "SELECT time, date_format(time, \"yyyy-MM-dd'T'HH:mm:ssZ\") AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Arrays.asList("2016-06-03T09:23:00+0000");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatWithoutMs() {
        String sqlQuery = "SELECT time, date_format(time, \"yyyy-MM-dd'T'HH:mm:ss\") AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Arrays.asList("2016-06-03T09:23:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatPST() {
        String sqlQuery = "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'PST') AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Arrays.asList("2016-06-03 02:23:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatGMT() {
        String sqlQuery = "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'GMT-08:00') AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Arrays.asList("2016-06-03 01:23:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatZeroZoneAndGMT() {
        String sqlQuery = "SELECT time,  date_format(time,'yyyy-MM-dd HH:mm:ss ZZ','PST') AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<String> expectedColumnValues = Arrays.asList("2016-06-03 02:23:00 -07:00");

        assertTableContainsColumnValues(expectedColumnValues, resultTable, "f-date");
    }
}
