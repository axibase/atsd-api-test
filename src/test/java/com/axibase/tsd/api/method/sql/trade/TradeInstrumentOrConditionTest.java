package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class TradeInstrumentOrConditionTest extends SqlTradeTest {
    private static final String QUERY = "select exchange, class, symbol from atsd_trade where {where} " +
            "and datetime between '2020-03-22T09:00:00Z' and '2020-03-22T10:00:02Z' {groupBy} order by exchange, class, symbol";
    private final String classTwo = Mocks.tradeClass();
    private final String exchangeTwo = Mocks.tradeExchange();

    private String classTwo() {
        return classTwo;
    }

    private String exchangeTwo() {
        return exchangeTwo;
    }

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        final long timestamp = getUnixTime("2020-03-22T10:00:01Z");
        trades.add(trade(timestamp).setSide(Trade.Side.BUY));
        trades.add(trade(timestamp).setSymbol(symbolTwo()).setSide(Trade.Side.SELL));
        trades.add(trade(timestamp).setSymbol(symbolThree()).setSide(Trade.Side.BUY));
        trades.add(trade(timestamp).setClazz(classTwo()).setSide(Trade.Side.SELL));
        trades.add(trade(timestamp).setClazz(classTwo()).setSymbol(symbolTwo()).setSide(Trade.Side.BUY));
        trades.add(trade(timestamp).setClazz(classTwo()).setSymbol(symbolTwo()).setExchange(exchangeTwo()).setSide(Trade.Side.SELL));
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
                test("(class = '{classOne}' AND symbol IN('{symbolOne}', '{symbolTwo}') OR class = '{classTwo}' AND symbol IN('{symbolTwo}'))")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo())
                        .addExpected(exchange(), classTwo(), symbolTwo())
                        .addExpected(exchangeTwo(), classTwo(), symbolTwo()),
                test("class = '{classOne}' AND (symbol = '{symbolOne}' or symbol = '{symbolTwo}')")
                        .groupBy("group by exchange, class, symbol")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo()),
                test("(class = '{classOne}' AND symbol IN('{symbolOne}', '{symbolTwo}') OR class = '{classTwo}' AND symbol IN('{symbolTwo}')) and exchange='{exchangeOne}'")
                        .groupBy("group by exchange, class, symbol")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo())
                        .addExpected(exchange(), classTwo(), symbolTwo()),
                test("side = 'B' and (class = '{classOne}' AND symbol IN('{symbolOne}', '{symbolTwo}') OR class = '{classTwo}' AND symbol IN('{symbolTwo}')) and exchange='{exchangeOne}'")
                        .groupBy("group by exchange, class, symbol")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), classTwo(), symbolTwo()),
                test("class = '{classOne}' and (class = '{classOne}' AND symbol IN('{symbolOne}', '{symbolTwo}') OR class = '{classTwo}' AND symbol IN('{symbolTwo}'))")
                        .groupBy("group by exchange, class, symbol")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo()),
                test("(class = '{classOne}' and symbol = '{symbolOne}' OR class = '{classOne}' and symbol = '{symbolTwo}' OR exchange='{exchangeTwo}' and class='{classTwo}' and symbol='{symbolTwo}')")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo())
                        .addExpected(exchangeTwo(), classTwo(), symbolTwo()),
                test("(class = '{classOne}' and (symbol like '{symbolOne}%' OR symbol like '{symbolTwo}%'))")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), clazz(), symbolTwo()),
                test("((class = '{classOne}' and symbol = '{symbolOne}') or (class = '{classTwo}' and symbol like '{symbolTwo}%'))")
                        .addExpected(exchange(), clazz(), symbol())
                        .addExpected(exchange(), classTwo(), symbolTwo())
                        .addExpected(exchangeTwo(), classTwo(), symbolTwo()),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String where) {
        return new TestConfig(where);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String where) {
            super(where);
            setVariable("groupBy", "");
            setVariable("exchangeOne", exchange());
            setVariable("exchangeTwo", exchangeTwo());
            setVariable("classOne", clazz());
            setVariable("classTwo", classTwo());
            setVariable("symbolOne", symbol());
            setVariable("symbolTwo", symbolTwo());
            setVariable("symbolThree", symbolThree());
            setVariable("where", composeQuery(where));
        }

        private TestConfig groupBy(String groupBy) {
            setVariable("groupBy", groupBy);
            return this;
        }

        @Override
        public TestConfig addExpected(String... row) {
            for (int i = 0; i < row.length; i++) {
                row[i] = row[i].toUpperCase();
            }
            super.addExpected(row);
            return this;
        }
    }
}