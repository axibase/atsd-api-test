package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeGroupingByInstrumentAndTimestampTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();

        trades.add(fromISOString("2020-03-22T10:01:00.123456Z"));
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z"));
        trades.add(fromISOString("2020-03-22T10:05:00.654321Z"));
        trades.add(fromISOString("2020-03-22T10:10:00.123456Z"));
        trades.add(fromISOString("2020-03-22T10:10:00.123456Z"));

        trades.add(fromISOString("2020-03-22T10:00:01.654321Z").setSymbol(symbolTwo()));
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setSymbol(symbolTwo()));
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setSymbol(symbolTwo()));

        insert(trades);
    }

    @Test
    public void test() {
        String sql = "select symbol, datetime, count(*) from atsd_trade where class = '" + clazz() +
                "' group by exchange, class, symbol, datetime order by symbol, datetime";
        String[][] expected = new String[][]{
                {symbol(), "2020-03-22T10:01:00.123456Z", "2"},
                {symbol(), "2020-03-22T10:05:00.654321Z", "1"},
                {symbol(), "2020-03-22T10:10:00.123456Z", "2"},
                {symbolTwo(), "2020-03-22T10:00:01.654321Z", "1"},
                {symbolTwo(), "2020-03-22T10:01:00.123456Z", "2"}
        };
        assertSqlQueryRows(expected, sql);
    }
}