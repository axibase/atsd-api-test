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

public class TradeClosePriceAggregationTest extends SqlTradeTest {
    private static final String QUERY = "select {fields} from atsd_trade where {instrument} " +
            " and datetime between '2020-03-22T00:00:00Z' and '2020-03-24T00:00:00Z'" +
            "group by exchange, class, symbol {period} {orderBy}";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z"), new BigDecimal("1"), 1));
        trades.add(trade(getUnixTime("2020-03-22T10:20:15Z"), new BigDecimal("2"), 1));
        trades.add(trade(getUnixTime("2020-03-22T11:20:15Z"), new BigDecimal("3"), 1));
        trades.add(trade(getUnixTime("2020-03-22T15:20:15Z"), new BigDecimal("4"), 1));
        trades.add(trade(getUnixTime("2020-03-22T23:59:59.999999Z"), new BigDecimal("5"), 1));
        trades.add(trade(getUnixTime("2020-03-23T10:20:15Z"), new BigDecimal("6"), 1));
        trades.add(trade(getUnixTime("2020-03-23T11:20:15Z"), new BigDecimal("7"), 1));
        trades.add(trade(getUnixTime("2020-03-23T15:20:15Z"), new BigDecimal("8"), 1));

        trades.add(trade(getUnixTime("2020-03-22T00:00:00Z"), new BigDecimal("9"), 1).setSymbol(symbolTwo()));
        trades.add(trade(getUnixTime("2020-03-22T10:20:15Z"), new BigDecimal("10"), 1).setSymbol(symbolTwo()));
        trades.add(trade(getUnixTime("2020-03-22T11:20:15Z"), new BigDecimal("11"), 1).setSymbol(symbolTwo()));
        trades.add(trade(getUnixTime("2020-03-22T15:20:15Z"), new BigDecimal("12"), 1).setSymbol(symbolTwo()));

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
                test("Test daily both instruments")
                        .instrument(classCondition())
                        .fields("datetime, close()")
                        .period(1, "day")
                        .addExpected("2020-03-22T00:00:00.000000Z", "12")
                        .addExpected("2020-03-23T00:00:00.000000Z", "8")
                        .addExpected("2020-03-22T00:00:00.000000Z", "5")
                ,
                test("Test daily first instrument")
                        .instrument(instrumentCondition())
                        .fields("datetime, close()")
                        .period(1, "day")
                        .addExpected("2020-03-23T00:00:00.000000Z", "8")
                        .addExpected("2020-03-22T00:00:00.000000Z", "5")
                ,
                test("Test daily second instrument")
                        .instrument(instrumentTwoCondition())
                        .fields("datetime, close()")
                        .period(1, "day")
                        .addExpected("2020-03-22T00:00:00.000000Z", "12")
                ,
                test("Test daily first instrument order by time")
                        .instrument(instrumentCondition())
                        .fields("datetime, close()")
                        .orderBy("order by time")
                        .period(1, "day")
                        .addExpected("2020-03-22T00:00:00.000000Z", "5")
                        .addExpected("2020-03-23T00:00:00.000000Z", "8")
                ,
                test("Test two hours interval both instruments")
                        .instrument(classCondition())
                        .fields("datetime, close()")
                        .period(2, "hour")
                        .addExpected("2020-03-22T14:00:00.000000Z", "12")
                        .addExpected("2020-03-22T10:00:00.000000Z", "11")
                        .addExpected("2020-03-22T00:00:00.000000Z", "9")
                        .addExpected("2020-03-23T14:00:00.000000Z", "8")
                        .addExpected("2020-03-23T10:00:00.000000Z", "7")
                        .addExpected("2020-03-22T22:00:00.000000Z", "5")
                        .addExpected("2020-03-22T14:00:00.000000Z", "4")
                        .addExpected("2020-03-22T10:00:00.000000Z", "3")
                ,
                test("Test two hours interval first instrument")
                        .instrument(instrumentCondition())
                        .fields("datetime, close()")
                        .period(2, "hour")
                        .addExpected("2020-03-23T14:00:00.000000Z", "8")
                        .addExpected("2020-03-23T10:00:00.000000Z", "7")
                        .addExpected("2020-03-22T22:00:00.000000Z", "5")
                        .addExpected("2020-03-22T14:00:00.000000Z", "4")
                        .addExpected("2020-03-22T10:00:00.000000Z", "3")
                ,
                test("Test two hours interval second instrument")
                        .instrument(instrumentTwoCondition())
                        .fields("datetime, close()")
                        .period(2, "hour")
                        .addExpected("2020-03-22T14:00:00.000000Z", "12")
                        .addExpected("2020-03-22T10:00:00.000000Z", "11")
                        .addExpected("2020-03-22T00:00:00.000000Z", "9")
                ,
                test("Test 1 hour interval both instruments")
                        .instrument(classCondition())
                        .fields("datetime, close()")
                        .period(1, "hour")
                        .addExpected("2020-03-22T15:00:00.000000Z", "12")
                        .addExpected("2020-03-22T11:00:00.000000Z", "11")
                        .addExpected("2020-03-22T10:00:00.000000Z", "10")
                        .addExpected("2020-03-22T00:00:00.000000Z", "9")
                        .addExpected("2020-03-23T15:00:00.000000Z", "8")
                        .addExpected("2020-03-23T11:00:00.000000Z", "7")
                        .addExpected("2020-03-23T10:00:00.000000Z", "6")
                        .addExpected("2020-03-22T23:00:00.000000Z", "5")
                        .addExpected("2020-03-22T15:00:00.000000Z", "4")
                        .addExpected("2020-03-22T11:00:00.000000Z", "3")
                        .addExpected("2020-03-22T10:00:00.000000Z", "2")
                ,
                test("Test 1 hour interval first instrument")
                        .instrument(instrumentCondition())
                        .fields("datetime, close()")
                        .period(1, "hour")
                        .addExpected("2020-03-23T15:00:00.000000Z", "8")
                        .addExpected("2020-03-23T11:00:00.000000Z", "7")
                        .addExpected("2020-03-23T10:00:00.000000Z", "6")
                        .addExpected("2020-03-22T23:00:00.000000Z", "5")
                        .addExpected("2020-03-22T15:00:00.000000Z", "4")
                        .addExpected("2020-03-22T11:00:00.000000Z", "3")
                        .addExpected("2020-03-22T10:00:00.000000Z", "2")
                ,
                test("Test 1 hour interval second instrument")
                        .instrument(instrumentTwoCondition())
                        .fields("datetime, close()")
                        .period(1, "hour")
                        .addExpected("2020-03-22T15:00:00.000000Z", "12")
                        .addExpected("2020-03-22T11:00:00.000000Z", "11")
                        .addExpected("2020-03-22T10:00:00.000000Z", "10")
                        .addExpected("2020-03-22T00:00:00.000000Z", "9")
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
            setVariable("orderBy", "");
        }

        private TestConfig period(int count, String unit) {
            setVariable("period", String.format(", period(%d %s)", count, unit));
            return this;
        }

        private TestConfig orderBy(String orderBy) {
            setVariable("orderBy", orderBy);
            return this;
        }
    }
}