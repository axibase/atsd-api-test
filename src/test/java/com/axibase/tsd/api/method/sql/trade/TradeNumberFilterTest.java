package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeNumberFilterTest extends SqlTradeTest {
    public static final String QUERY = "select sum(quantity) from atsd_trade where {instrument} and {numberCondition} group by exchange, class, symbol";

    @BeforeClass
    public void prepareData() throws Exception {
        long timestamp = Util.getUnixTime("2020-05-21T10:15:14Z");
        long number = 1234567890;
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(timestamp).setNumber(number++).setQuantity(1));
        trades.add(trade(timestamp).setNumber(number++).setQuantity(2));
        trades.add(trade(timestamp).setNumber(number++).setQuantity(3));
        trades.add(trade(timestamp).setNumber(number++).setQuantity(4));
        trades.add(trade(timestamp).setNumber(number).setQuantity(5));
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
                test("trade_num > 1234567892").addExpected("9"),
                test("trade_num >= 1234567892").addExpected("12"),
                test("trade_num < 1234567891").addExpected("1"),
                test("trade_num <= 1234567891").addExpected("3"),
                test("trade_num > 1234567891 and trade_num < 1234567894").addExpected("7"),
                test("trade_num >= 1234567891 and trade_num <= 1234567894").addExpected("14"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("numberCondition", description);
        }
    }
}