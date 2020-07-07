package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradeIsWorkdayTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-04-16T23:00:00Z").setPrice(new BigDecimal("2"))); // Holiday
        trades.add(fromISOString("2020-04-17T23:00:00Z").setPrice(new BigDecimal("3"))); // Workday
        trades.add(fromISOString("2020-04-18T23:00:00Z")); // Saturday
        trades.add(fromISOString("2018-12-28T10:00:00Z")); // Workday
        trades.add(fromISOString("2018-12-29T10:00:00Z")); // Working saturday in Russia
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery();
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Plain query UTC timezone in is_workday function")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_workday(time, 'usa', 'UTC') WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'rus'")
                        .addExpected("2020-04-17T23:00:00.000000Z"),

                test("Plain query UTC timezone in the WITH clause")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_workday(datetime) WITH TIMEZONE = 'UTC', WORKDAY_CALENDAR = 'usa'")
                        .addExpected("2020-04-17T23:00:00.000000Z"),

                test("Plain query Moscow timezone")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_workday(time) WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'usa'")
                        .addExpected("2020-04-16T23:00:00.000000Z"),

                test("Aggregate query UTC timezone")
                        .sql("select datetime, open() from atsd_trade where {instrument} and {time} and is_workday(time, 'usa', 'UTC') group by exchange, class, symbol, period(1 day, 'UTC')")
                        .addExpected("2020-04-17T00:00:00.000000Z", "3"),

                test("Aggregate query Moscow timezone")
                        .sql("select datetime, close() from atsd_trade where {instrument} and {time} and is_workday(time, 'usa', 'Europe/Moscow') group by exchange, class, symbol, period(1 day, 'UTC')")
                        .addExpected("2020-04-16T00:00:00.000000Z", "2"),

                test("Test working saturday")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_workday(datetime) WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'rus'")
                        .time(" time between '2018-12-28T00:00:00Z' and '2018-12-30T00:00:00Z' ")
                        .addExpected("2018-12-28T10:00:00.000000Z")
                        .addExpected("2018-12-29T10:00:00.000000Z"),

                test("Test saturday")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_workday(datetime) WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'usa'")
                        .time(" time between '2018-12-28T00:00:00Z' and '2018-12-30T00:00:00Z' ")
                        .addExpected("2018-12-28T10:00:00.000000Z"),

                test("Test is_weekday function")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and is_weekday(time, 'usa', 'UTC') WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'rus'")
                        .addExpected("2020-04-16T23:00:00.000000Z")
                        .addExpected("2020-04-17T23:00:00.000000Z"),

                test("Test is_weekday function negate")
                        .sql("select datetime from atsd_trade where {instrument} and {time} and not is_weekday(time, 'usa', 'UTC') WITH TIMEZONE = 'Europe/Moscow', WORKDAY_CALENDAR = 'rus'")
                        .addExpected("2020-04-18T23:00:00.000000Z"),

                test("Test is_weekday function negate aggregation")
                        .sql("select datetime, open() from atsd_trade where {instrument} and {time} and not is_weekday(time, 'usa', 'UTC') group by exchange, class, symbol, period(1 day, 'UTC')")
                        .addExpected("2020-04-18T00:00:00.000000Z", "1"),

                test("Test is_workday function negate aggregation")
                        .sql("select datetime, open() from atsd_trade where {instrument} and {time} and not is_workday(time, 'usa', 'UTC') group by exchange, class, symbol, period(1 day, 'UTC')")
                        .addExpected("2020-04-16T00:00:00.000000Z", "2")
                        .addExpected("2020-04-18T00:00:00.000000Z", "1"),

        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {
        private String sql;

        public TestConfig(String description) {
            super(description);
            time("time between '2020-04-15T00:00:00Z' and '2020-04-19T00:00:00Z'");
        }

        public TestConfig sql(String sql) {
            this.sql = sql;
            return this;
        }

        public TestConfig time(String time) {
            setVariable("time", time);
            return this;
        }

        public String composeQuery() {
            return composeQuery(sql);
        }
    }
}