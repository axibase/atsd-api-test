package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.TradeSender;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TradeDateTimeTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        Trade trade = fromISOString("2020-05-19T10:21:49.123456Z").setNumber(Long.MAX_VALUE);
        TradeSender.send(Collections.singletonList(trade)).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @Test(dataProvider = "testDateFormatDataProvider")
    public void testDateFormat(SqlTestConfig testConfig) throws Exception {
        String template = "select {fields} from atsd_trade where {instrument} " +
                "and time between '2020-05-19T10:21:49.123Z' and '2020-05-19T10:21:49.124Z' " +
                "with timezone='UTC'";
        String sql = testConfig.composeQuery(template);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);

    }

    @DataProvider
    public Object[][] testDateFormatDataProvider() {
        SqlTestConfig[] data = {
                test("datetime field").fields("datetime").addExpected("2020-05-19T10:21:49.123456Z"),
                test("Format year").fields("date_format(time, 'yyyy')").addExpected("2020"),
                test("Format date").fields("date_format(time, 'yyyy-MM-dd')").addExpected("2020-05-19"),
                test("Format datetime").fields("date_format(time, 'yyyy-MM-dd HH:mm:ss.SSSSSS')").addExpected("2020-05-19 10:21:49.123456"),
                test("Extract year").fields("extract(year from time)").addExpected("2020"),
                test("Extract year").fields("year(time)").addExpected("2020"),
                test("Extract second").fields("second(time)").addExpected("49"),
                test("Workday function").fields("WORKDAY(datetime, -1, 'usa')").addExpected("2020-05-18T00:00:00.000000Z"),
                test("Workday_count function").fields("workday_count(time, time + 86400 * 1000, 'usa')").addExpected("1"),
                test("IS_WORKDAY function").fields("IS_WORKDAY(datetime, 'usa')").addExpected("true"),
                test("IS_WEEKDAY function").fields("IS_WEEKDAY(datetime, 'usa')").addExpected("true"),
                test("Dateadd function").fields("DATEADD(DAY, 1, datetime)").addExpected("2020-05-20T10:21:49.123456Z"),
                test("date_round function").fields("date_round(datetime, 5 minute)").addExpected("2020-05-19T10:20:00.000000Z"),
                test("Arithmetic plus").fields("date_format(time + 1000 * 2, 'yyyy-MM-ddTHH:mm:ss.SSSSSSZ')").addExpected("2020-05-19T10:21:51.123456Z"),
                test("Arithmetic minus").fields("date_format(time - 1000 * 2 - 3, 'yyyy-MM-ddTHH:mm:ss.SSSSSSZ')").addExpected("2020-05-19T10:21:47.120456Z"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    @Test(dataProvider = "testDateFormatAggregationDataProvider")
    public void testDateFormatAggregation(SqlTestConfig testConfig) throws Exception {
        String template = "select {fields} from atsd_trade where {instrument} " +
                "and time between '2020-05-19T10:20:00.000Z' and '2020-05-19T10:21:49.124Z' " +
                "group by exchange, class, symbol, period(5 minute) " +
                "with timezone='UTC'";
        String sql = testConfig.composeQuery(template);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testDateFormatAggregationDataProvider() {
        SqlTestConfig[] data = {
                test("Min time").fields("datetime, date_format(min(time))").addExpected("2020-05-19T10:20:00.000000Z", "2020-05-19T10:21:49.123456Z"),
                test("Min datetime").fields("min(datetime)").addExpected("2020-05-19T10:21:49.123456Z"),
                test("Max time").fields("date_format(max(time))").addExpected("2020-05-19T10:21:49.123456Z"),
                test("Max datetime").fields("max(datetime)").addExpected("2020-05-19T10:21:49.123456Z"),
                test("Min value time").fields("date_format(min_value_time(price))").addExpected("2020-05-19T10:21:49.123456Z"),
                test("Max value time").fields("date_format(max_value_time(quantity))").addExpected("2020-05-19T10:21:49.123456Z"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TradeTestConfig<TradeTestConfig> test(String description) {
        return new TradeTestConfig<>(description);
    }

}