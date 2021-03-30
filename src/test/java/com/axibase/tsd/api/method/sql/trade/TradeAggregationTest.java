package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class TradeAggregationTest extends SqlTradeTest {
    private static final String QUERY = "select {fields} from atsd_trade where {instrument} {selectionInterval}" +
            "group by exchange, class, symbol {period} {having}  WITH TIMEZONE = 'UTC'";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setPrice(new BigDecimal("126.99")).setQuantity(22330));
        trades.add(trade(getUnixTime("2020-03-22T10:09:00Z"), new BigDecimal("127.36"), 22330));
        trades.add(trade(getUnixTime("2020-03-22T10:49:00Z"), new BigDecimal("127.02"), 22339));
        trades.add(fromISOString("2020-03-22T10:55:00.654321Z").setPrice(new BigDecimal("127.28")).setQuantity(22330));
        trades.add(trade(getUnixTime("2020-03-22T11:01:05Z"), new BigDecimal("127.20"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:14Z"), new BigDecimal("127.20"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:29Z"), new BigDecimal("127.31"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:49Z"), new BigDecimal("127.10"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:50Z"), new BigDecimal("127.11"), 4137));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Test ohlcv aggregation")
                        .fields("open(), high(), low(), close(), volume()")
                        .addExpected("126.99", "127.36", "126.99", "127.11", "105466")
                ,
                test("Test ohlcv aggregation with price argument")
                        .fields("open(price), high(price), low(price), close(price), volume(price)")
                        .addExpected("126.99", "127.36", "126.99", "127.11", "105466")
                ,
                test("Test ohlcv aggregation with period")
                        .fields("open(), high(), low(), close(), volume()")
                        .period(1, "hour")
                        .addExpected("126.99", "127.36", "126.99", "127.28", "89329")
                        .addExpected("127.2", "127.31", "127.1", "127.11", "16137")
                ,
                test("Test ohlcv aggregation with having")
                        .fields("open(), high(), low(), close(), volume()")
                        .period(1, "hour")
                        .having("volume() > 20000")
                        .addExpected("126.99", "127.36", "126.99", "127.28", "89329")
                ,
                test("Test ohlcv aggregation with having alias")
                        .fields("open(), high(), low(), close(), volume(), close() * count(*) as t")
                        .period(1, "hour")
                        .having("t > 600")
                        .addExpected("127.2", "127.31", "127.1", "127.11", "16137", "635.55"),
                test("Test sum(price*quantity)")
                        .fields("open(), volume(), sum((quantity*price)), vwap(), vwap(price), sum(quantity)")
                        .addExpected("126.99", "105466", "1.341158175E7", "127.16497970910056", "127.16497970910056", "105466")
                ,
                test("Period greater than 1 hour")
                        .fields("datetime, count(*)")
                        .period(2, "hour")
                        .addExpected("2020-03-22T10:00:00.000000Z", "9")
                ,
                test("Period 40 minutes")
                        .fields("datetime, count(*)")
                        .period(40, "minute")
                        .addExpected("2020-03-22T10:00:00.000000Z", "2")
                        .addExpected("2020-03-22T10:40:00.000000Z", "7"),
                test("Period is greater than selection interval")
                        .fields("datetime, count(*)")
                        .period(1, "minute")
                        .selectionInterval("datetime between '2020-03-22T11:01:00.001Z' and '2020-03-22T11:02:00.000Z'")
                        .addExpected("2020-03-22T11:01:00.000000Z", "5"),
                test("Test min/max time HBase side")
                        .fields("date_format(min(time)), date_format(max(time))")
                        .period(1, "hour")
                        .addExpected("2020-03-22T10:01:00.123456Z", "2020-03-22T10:55:00.654321Z")
                        .addExpected("2020-03-22T11:01:05.000000Z", "2020-03-22T11:01:50.000000Z")
        };
        return TestUtil.convertTo2DimArray(data);
    }

    @Test
    public void testClientSideTimeAggregation() {
        String sql = "select date_format(min(time)), date_format(max(time)) from atsd_trade\n" +
                "    WHERE " + instrumentCondition() + "\n" +
                "    GROUP BY class, period(1 hour)  WITH TIMEZONE = 'UTC'";
        String[][] expected = new String[][]{
                {"2020-03-22T10:01:00.123456Z", "2020-03-22T10:55:00.654321Z"},
                {"2020-03-22T11:01:05.000000Z", "2020-03-22T11:01:50.000000Z"},
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testGroupByHour() throws Exception {
        String sql = "SELECT HOUR(time) AS hour_of_day, count(*)\n" +
                "      FROM atsd_trade\n" +
                "    WHERE " + instrumentCondition() + "\n" +
                "    GROUP BY class, HOUR(time)  WITH TIMEZONE = 'UTC'";
        String[][] expected = new String[][]{
                {"10", "4"},
                {"11", "5"},
        };
        assertSqlQueryRows(expected, sql);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("period", "");
            setVariable("having", "");
            setVariable("selectionInterval", "");
        }

        private TestConfig period(int count, String unit) {
            setVariable("period", String.format(", period(%d %s)", count, unit));
            return this;
        }

        private TestConfig having(String having) {
            setVariable("having", "having " + having);
            return this;
        }

        private TestConfig selectionInterval(String selectionInterval) {
            setVariable("selectionInterval", " and " + selectionInterval);
            return this;
        }
    }
}