package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.axibase.tsd.api.util.TestUtil.ISOFormat;
import static com.axibase.tsd.api.util.TestUtil.parseDate;

public class SqlPeriodDayAlignTest extends SqlTest {
    private static final String TEST_PREFIX = "sql-period-day-align-";
    private static final String TEST_METRIC_NAME = TEST_PREFIX + "metric";
    private static final String TEST_ENTITY_NAME = TEST_PREFIX + "entity";
    private static final String DAY_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final String START_TIME = "2016-06-19T00:00:00.000Z";
    private static final String END_TIME = "2016-06-23T00:00:00.000Z";
    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.ssS'Z'";
    private static final Long DELTA = 900000L;
    private static final Long DAY_LENGTH = 86400000L;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(TEST_ENTITY_NAME, TEST_METRIC_NAME);
        Long firstTime = parseDate(START_TIME).getTime(),
                lastTime = parseDate(END_TIME).getTime(),
                time = firstTime;
        while (time < lastTime) {
            series.addData(new Sample(ISOFormat(time), "0"));
            time += DELTA;
        }
        SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
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

        Response response = queryResponse(sqlQuery);

        StringTable resultTable = response.readEntity(StringTable.class);

        List<List<String>> expectedRows = generateExpectedRows();

        assertTableRowsExist(expectedRows, resultTable);
    }


    private List<List<String>> generateExpectedRows() {
        List<List<String>> resultRows = new ArrayList<>();
        final String LOCAL_START_DATE = TestUtil.formatDate(TestUtil.parseDate(START_TIME), ISO_PATTERN);
        final String LOCAL_END_DATE = TestUtil.formatDate(TestUtil.parseDate(END_TIME), ISO_PATTERN);
        Long startTime = TestUtil.parseDate(LOCAL_START_DATE).getTime();
        Long endTime = TestUtil.parseDate(LOCAL_END_DATE).getTime();
        Long time = startTime;

        int daySeriesCount = 0;
        while (time < endTime) {
            if (isDayStart(time) && daySeriesCount > 0) {
                resultRows.add(formatRow(time - DAY_LENGTH, daySeriesCount));
                daySeriesCount = 0;
            }
            time += DELTA;
            daySeriesCount++;
        }
        if (daySeriesCount > 0) {
            resultRows.add(formatRow(time - DELTA, daySeriesCount));
        }
        return resultRows;
    }

    private List<String> formatRow(Long time, Integer count) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DAY_FORMAT_PATTERN);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return Arrays.asList(dateFormat.format(new Date(time)), Integer.toString(count));
    }

    private boolean isDayStart(Long time) {
        return time % DAY_LENGTH == 0;
    }

}
