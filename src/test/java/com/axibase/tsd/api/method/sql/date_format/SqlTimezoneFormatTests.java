package com.axibase.tsd.api.method.sql.date_format;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * @author Igor Shmagrinskiy
 */
public class SqlTimezoneFormatTests extends SqlTest {
    private static final String TEST_PREFIX = "sql-date-format-timezone-";


    @BeforeClass
    public static void prepareData() {
        Series testSeries = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        testSeries.addTag("a", "b");
        sendSamplesToSeries(testSeries,
                new Sample(Util.parseDate("2016-06-03T09:23:00.000Z").getTime(), "7")
        );
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

        assertResponseDateFormat(executeQuery(sqlQuery), "2016-06-03T09:23:00+0000");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatWithoutMs() {
        String sqlQuery = "SELECT time, date_format(time, \"yyyy-MM-dd'T'HH:mm:ss\") AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        assertResponseDateFormat(executeQuery(sqlQuery), "2016-06-03T09:23:00");

    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatPST() {
        String sqlQuery = "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'PST') AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        assertResponseDateFormat(executeQuery(sqlQuery), "2016-06-03 02:23:00");
    }

    /**
     * issue #2904
     */
    @Test
    public void testSimpleDateFormatGMT() {
        String sqlQuery = "SELECT time, date_format(time, 'yyyy-MM-dd HH:mm:ss', 'GMT-08:00') AS 'f-date'" +
                "FROM 'sql-date-format-timezone-metric' " +
                "WHERE datetime = '2016-06-03T09:23:00.000Z'";

        assertResponseDateFormat(executeQuery(sqlQuery), "2016-06-03 01:23:00");
    }


    /**
     * Assert that Response contains table with date in expected format
     *
     * @param response           Sql execute response
     * @param expectedFormatDate Expected formatted date
     */
    private void assertResponseDateFormat(Response response, String expectedFormatDate) {
        String resultFormattedDate = response
                .readEntity(StringTable.class)
                .getValueAt(1, 0);
        assertEquals("Incorrect date formatting", expectedFormatDate, resultFormattedDate);
    }
}
