package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.method.version.Version;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.axibase.tsd.api.Util.*;

public class SqlPeriodDayAlignTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-day-align-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String DAY_FORMAT_PATTERN = "yyyy-MM-dd";

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        Long firstTime = parseDate("2016-06-19T00:00:00.000Z").getTime(),
                lastTime = parseDate("2016-06-23T00:00:00.000Z").getTime(),
                time = firstTime,
                delta = 900000L;
        while (time < lastTime) {
            series.addData(new Sample(ISOFormat(time), "0"));
            time += delta;
        }
        SeriesMethod.insertSeriesCheck(series);
    }

    /**
     * #3241
     */
    @Test
    public void testDayAlign() {
        String sqlQuery = String.format(
                "SELECT DATE_FORMAT(time,'%s'), COUNT(*) FROM '%s' %nGROUP BY PERIOD(1 DAY)",
                DAY_FORMAT_PATTERN, TEST_METRIC_NAME
        );

        Response response = executeQuery(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = generateExpectedRows(Arrays.asList(
                "2016-06-19T00:00:00.000Z",
                "2016-06-20T00:00:00.000Z",
                "2016-06-21T00:00:00.000Z",
                "2016-06-22T00:00:00.000Z"
        ));

        assertTableRows(expectedRows, resultTable);
    }


    private List<List<String>> generateExpectedRows(List<String> dates) {
        List<List<String>> resultRows = new ArrayList<>();
        Version version = VersionMethod.queryVersionCheck();
        Integer offsetMinutes = version.
                getDate()
                .getTimeZone()
                .getOffsetMinutes();

        int offsetSeriesCount = Math.abs(offsetMinutes / 15);

        for (int i = 0; i < dates.size(); i++) {
            String d = dates.get(i);
            int daySeriesCount = (i == 0) ? 96 - Math.abs(offsetSeriesCount) : 96;
            Date date = parseDate(d);
            resultRows.add(
                    Arrays.asList(
                            formatDate(date, DAY_FORMAT_PATTERN),
                            Integer.toString(daySeriesCount)
                    )
            );
            if ((i + 1) == dates.size() && offsetMinutes != 0) {
                Date nextDayAfterLast = new Date(date.getTime() + 24 * 60 * 60 * 1000);
                resultRows.add(
                        Arrays.asList(
                                formatDate(nextDayAfterLast, DAY_FORMAT_PATTERN),
                                Integer.toString(offsetSeriesCount)
                        )
                );

            }
        }
        return resultRows;
    }
}
