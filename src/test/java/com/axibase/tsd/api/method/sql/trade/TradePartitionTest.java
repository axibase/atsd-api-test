package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradePartitionTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2021-03-05T10:00:00Z"));
        trades.add(fromISOString("2021-03-05T11:00:00Z"));
        trades.add(fromISOString("2021-03-05T12:00:00Z"));
        trades.add(fromISOString("2021-03-05T10:00:00Z").setSymbol(symbolTwo()));
        trades.add(fromISOString("2021-03-05T11:00:00Z").setSymbol(symbolTwo()));
        trades.add(fromISOString("2021-03-05T12:00:00Z").setSymbol(symbolTwo()));
        insert(trades);
    }

    @Test
    public void testTimeWindow() {
        String sql = "SELECT symbol, datetime, count(*)  \n" +
                "  FROM atsd_trade  WHERE " + classCondition() +
                "WITH ROW_NUMBER(symbol ORDER BY time) BETWEEN 2 HOUR PRECEDING AND CURRENT ROW";
        String[][] expected = new String[][]{
                {symbol(), "2021-03-05T10:00:00.000000Z", "1"},
                {symbol(), "2021-03-05T11:00:00.000000Z", "2"},
                {symbol(), "2021-03-05T12:00:00.000000Z", "2"},
                {symbolTwo(), "2021-03-05T10:00:00.000000Z", "1"},
                {symbolTwo(), "2021-03-05T11:00:00.000000Z", "2"},
                {symbolTwo(), "2021-03-05T12:00:00.000000Z", "2"},
        };
        assertSqlQueryRows(expected, sql);
    }
}