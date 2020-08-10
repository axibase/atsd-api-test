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

public class TradeAggregationExpressionTest extends SqlTradeTest {
    public static final String SQL = "select open(), {expression} from atsd_trade where {instrument} group by exchange, class, symbol";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z"), new BigDecimal("10.55"), 10));
        trades.add(trade(getUnixTime("2020-03-22T11:00:01Z"), new BigDecimal("15.55"), 15));
        trades.add(trade(getUnixTime("2020-03-22T11:00:01Z"), new BigDecimal("20"), 12));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(SQL);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("sum(price*quantity)").addExpected("10.55", "578.75"),
                test("avg(price*quantity)").addExpected("10.55", "192.91666666666666"),
                test("min(price*quantity)").addExpected("10.55", "105.5"),
                test("max(price*quantity)").addExpected("10.55", "240"),
                test("first(price*quantity)").addExpected("10.55", "105.5"),
                test("last(price*quantity)").addExpected("10.55", "240"),
                test("percentile(90, price*quantity)").addExpected("10.55", "240"),
                test("percentile(50, price*quantity)").addExpected("10.55", "233.25"),
                test("sum(price + quantity)").addExpected("10.55", "83.1"),
                test("sum(price - quantity)").addExpected("10.55", "9.1"),
                test("sum((price + 0.45) / 2)").addExpected("10.55", "23.725"),
                test("sum(ceil(price))").addExpected("10.55", "47"),
                test("sum(floor(price))").addExpected("10.55", "45"),
                test("sum(round(price))").addExpected("10.55", "47"),
                test("sum(round(price, 1))").addExpected("10.55", "46.2"),
                test("sum(abs(-5))").addExpected("10.55", "15"),
                test("sum(mod(price, 0.5))").addExpected("10.55", "0.1"),
                test("sum(price % 0.5)").addExpected("10.55", "0.1"),
                test("sum(power(quantity, 2) + 1)").addExpected("10.55", "472"),
        };
        return TestUtil.convertTo2DimArray(data);
    }


    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("expression", description);
        }
    }
}