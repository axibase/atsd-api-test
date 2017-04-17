package com.axibase.tsd.api.method.sql.function.period.align;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.sql.StringTable;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.axibase.tsd.api.util.TestUtil.ISOFormat;
import static com.axibase.tsd.api.util.TestUtil.TestNames.entity;
import static com.axibase.tsd.api.util.TestUtil.TestNames.metric;
import static com.axibase.tsd.api.util.TestUtil.parseDate;

public class SqlPeriodDayAlignTest extends SqlTest {
    private static final String TEST_METRIC_NAME = metric();
    private static final String DAY_FORMAT_PATTERN = "yyyy-MM-dd";
    private static final String START_TIME = "2016-06-19T00:00:00.000Z";
    private static final String END_TIME = "2016-06-23T00:00:00.000Z";
    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.ssS'Z'";
    private static final Long DELTA = 900000L;
    private static final Long DAY_LENGTH = 86400000L;

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series = new Series(entity(), TEST_METRIC_NAME);
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
                "SELECT DATE_FORMAT(time,'%s'), COUNT(*) " +
                        "FROM '%s' " +
                        "GROUP BY PERIOD(1 DAY)",
                DAY_FORMAT_PATTERN, TEST_METRIC_NAME
        );

        StringTable resultTable = queryTable(sqlQuery);

        List<List<String>> expectedRows = generateExpectedRows(null);

        assertTableRowsExist(expectedRows, resultTable);
    }

    /**
     * #4100
     */
    @Test
    public void testDayAlignWithTimezone() {
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Kathmandu");
        String sqlQuery = String.format(
                "SELECT DATE_FORMAT(time,'%s'), COUNT(*) " +
                        "FROM '%s' " +
                        "GROUP BY PERIOD(1 DAY, '%s')",
                DAY_FORMAT_PATTERN,
                TEST_METRIC_NAME,
                timeZone.getID()
        );

        StringTable resultTable = queryTable(sqlQuery);

        List<List<String>> expectedRows = generateExpectedRows(timeZone);

        assertTableRowsExist(expectedRows, resultTable);
    }


    private List<List<String>> generateExpectedRows(TimeZone timeZone) {
        List<List<String>> resultRows = new ArrayList<>();
        final String localStartDate, localEndDate;

        if (timeZone == null) {
            localStartDate = TestUtil.formatDate(TestUtil.parseDate(START_TIME), ISO_PATTERN);
            localEndDate = TestUtil.formatDate(TestUtil.parseDate(END_TIME), ISO_PATTERN);
        } else {
            localStartDate = TestUtil.formatDate(TestUtil.parseDate(START_TIME), ISO_PATTERN, timeZone);
            localEndDate = TestUtil.formatDate(TestUtil.parseDate(END_TIME), ISO_PATTERN, timeZone);
        }

        Long startTime = TestUtil.parseDate(localStartDate).getTime();
        Long endTime = TestUtil.parseDate(localEndDate).getTime();
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
