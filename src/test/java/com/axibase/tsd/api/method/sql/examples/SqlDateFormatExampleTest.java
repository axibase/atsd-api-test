package com.axibase.tsd.api.method.sql.examples;

import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @authtor Igor Shmagrinskiy
 */
public class SqlDateFormatExampleTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-example-date-format-";

    @BeforeClass
    public static void prepareData() {
        Series series = new Series(TEST_PREFIX + "entity", TEST_PREFIX + "metric");
        sendSamplesToSeries(series,
                new Sample("2016-04-09T14:00:01Z", "3.8"),
                new Sample("2016-04-09T14:00:18Z", "14.0"),
                new Sample("2016-04-09T14:00:34Z", "16.83"),
                new Sample("2016-04-09T14:00:50Z", "10.2"),
                new Sample("2016-04-09T14:01:06Z", "4.04"),
                new Sample("2016-04-09T14:01:22Z", "9.0"),
                new Sample("2016-04-09T14:01:38Z", "2.0"),
                new Sample("2016-04-09T14:01:54Z", "8.0"),
                new Sample("2016-04-09T14:02:10Z", "10.23"),
                new Sample("2016-04-09T14:02:26Z", "14.0"),
                new Sample("2016-04-09T14:02:42Z", "20.2")
        );
    }

    /**
     * Issue #3047
     * Test for date format documentation example.
     *
     * @see <a href="Date format">https://github.com/axibase/atsd-docs/blob/master/api/sql/examples/datetime-format.md</a>
     */
    @Test
    public void testDateTimeFormat() {
        String sqlQuery =
                "SELECT datetime, time, value\n" +
                        "FROM 'sql-example-date-format-metric' \n" +
                        "WHERE entity = 'sql-example-date-format-entity'\n" +
                        "AND datetime BETWEEN '2016-04-09T14:00:00Z' AND '2016-04-09T14:05:00Z'";

        StringTable resultTable = executeQuery(sqlQuery).readEntity(StringTable.class);

        List<List<String>> expectedRows = Arrays.asList(
                Arrays.asList("2016-04-09T14:00:01Z", "1428588001000", "3.8"),
                Arrays.asList("2016-04-09T14:00:18Z", "1428588018000", "14"),
                Arrays.asList("2016-04-09T14:00:34Z", "1428588034000", "16.83"),
                Arrays.asList("2016-04-09T14:00:50Z", "1428588050000", "10.2"),
                Arrays.asList("2016-04-09T14:01:06Z", "1428588066000", "4.04"),
                Arrays.asList("2016-04-09T14:01:22Z", "1428588082000", "9"),
                Arrays.asList("2016-04-09T14:01:38Z", "1428588098000", "2"),
                Arrays.asList("2016-04-09T14:01:54Z", "1428588114000", "8"),
                Arrays.asList("2016-04-09T14:02:10Z", "1428588130000", "10.23"),
                Arrays.asList("2016-04-09T14:02:26Z", "1428588146000", "14"),
                Arrays.asList("2016-04-09T14:02:42Z", "1428588162000", "20.2")
        );

        assertTableRows(expectedRows, resultTable);
    }
}
