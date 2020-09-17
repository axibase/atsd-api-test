package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TradeDateFormatFilterTest extends SqlTradeTest {
    public static final String QUERY =
            "select datetime, open() from atsd_trade " +
                    "where {instrument} and {time} and {date_format} " +
                    "group by exchange, class, symbol, period(1 day) " +
                    "WITH TIMEZONE = 'UTC'";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-04-16T15:55:15Z").setPrice(new BigDecimal("1")));
        trades.add(fromISOString("2020-04-17T23:15:55Z").setPrice(new BigDecimal("2")));
        trades.add(fromISOString("2020-04-18T23:00:00Z").setPrice(new BigDecimal("3")));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Day pattern less than date_format function")
                        .dateFormat("date_format(time, 'dd') < '17'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1"),
                test("Day pattern less or equal date_format function")
                        .dateFormat("date_format(time, 'dd') <= '16'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1"),
                test("Day pattern  equal date_format function")
                        .dateFormat("date_format(time, 'dd') = '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Day pattern  not equal date_format function")
                        .dateFormat("date_format(time, 'dd') != '18'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Day pattern greater than date_format function")
                        .dateFormat("date_format(time, 'dd') > '17'")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
                test("Day pattern greater or equal date_format function")
                        .dateFormat("date_format(time, 'dd') >= '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),

                test("Day pattern less than extract function")
                        .dateFormat("extract(day from time) < '17'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1"),
                test("Day pattern less or equal extract function")
                        .dateFormat("extract(day from time) <= '16'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1"),
                test("Day pattern  equal extract function")
                        .dateFormat("extract(day from time) = '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Day pattern  not equal extract function")
                        .dateFormat("extract(day from time) != '18'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Day pattern greater than extract function")
                        .dateFormat("extract(day from time) > '17'")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
                test("Day pattern greater or equal extract function")
                        .dateFormat("extract(day from time) >= '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),

                test("HH:mm pattern not equal")
                        .dateFormat("date_format(time, 'HH:mm') != '23:15'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),

                test("Minute pattern less than")
                        .dateFormat("date_format(time, 'mm') < '15'")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
                test("Minute pattern less or equal")
                        .dateFormat("date_format(time, 'mm') <= '15'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
                test("Minute pattern  equal")
                        .dateFormat("date_format(time, 'mm') = '15'")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Minute pattern not equal")
                        .dateFormat("date_format(time, 'mm') != '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
                test("Minute pattern greater than")
                        .dateFormat("date_format(time, 'mm') > '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1"),
                test("Minute pattern greater or equal")
                        .dateFormat("date_format(time, 'mm') >= '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z", "1")
                        .addExpected("2020-04-17T00:00:00.000000Z", "2"),
                test("Day of week pattern")
                        .dateFormat("date_format(time, 'u') = '6'")
                        .addExpected("2020-04-18T00:00:00.000000Z", "3"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            time("time between '2020-04-15T00:00:00Z' and '2020-04-19T00:00:00Z'");
        }

        public TestConfig time(String time) {
            setVariable("time", time);
            return this;
        }

        public TestConfig dateFormat(String dateFormat) {
            setVariable("date_format", dateFormat);
            return this;
        }

    }

}