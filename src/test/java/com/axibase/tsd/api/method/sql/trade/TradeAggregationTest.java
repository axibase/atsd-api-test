package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.TradeSender;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class TradeAggregationTest extends SqlTradeTest {
    private static final String QUERY = "select {fields} from atsd_trade where {instrument} " +
            "group by exchange, class, symbol {period} {having}";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z"), new BigDecimal("126.99"), 22330));
        trades.add(trade(getUnixTime("2020-03-22T10:00:09Z"), new BigDecimal("127.36"), 22330));
        trades.add(trade(getUnixTime("2020-03-22T10:00:49Z"), new BigDecimal("127.02"), 22339));
        trades.add(trade(getUnixTime("2020-03-22T10:00:55Z"), new BigDecimal("127.28"), 22330));
        trades.add(trade(getUnixTime("2020-03-22T11:01:05Z"), new BigDecimal("127.20"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:14Z"), new BigDecimal("127.20"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:29Z"), new BigDecimal("127.31"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:49Z"), new BigDecimal("127.10"), 3000));
        trades.add(trade(getUnixTime("2020-03-22T11:01:50Z"), new BigDecimal("127.11"), 4137));
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
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
                        .addExpected("127.2", "127.31", "127.1", "127.11", "16137", "635.55")

                ,
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("period", "");
            setVariable("having", "");
        }

        private TestConfig period(int count, String unit) {
            setVariable("period", String.format(", period(%d %s)", count, unit));
            return this;
        }

        private TestConfig having(String having) {
            setVariable("having", "having " + having);
            return this;
        }
    }
}