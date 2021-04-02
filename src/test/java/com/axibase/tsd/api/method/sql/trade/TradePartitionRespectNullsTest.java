package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Mocks;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradePartitionRespectNullsTest extends SqlTradeTest {
    private final String clazzTwo = Mocks.tradeClass();

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(1612519516093L).setNumber(3572722987L).setMicroSeconds(131));
        trades.add(trade(1612519516093L).setNumber(3572722988L).setMicroSeconds(131));
        trades.add(trade(1612519516093L).setNumber(3572722989L).setMicroSeconds(609));
        trades.add(trade(1612519516093L).setNumber(3572722990L).setMicroSeconds(609));
        trades.add(trade(1612519524303L).setNumber(3572723449L).setMicroSeconds(40));
        trades.add(trade(1612519521062L).setNumber(3572723289L).setMicroSeconds(138).setClazz(clazzTwo));
        insert(trades);
    }

    @Test
    public void test() {
        String sql = "SELECT  trade_num, FIRST(CASE class WHEN '" + clazz() + "' THEN trade_num ELSE NaN END) first_tqbr\n" +
                "FROM atsd_trade\n" +
                "WHERE class IN ('" + clazz() + "', '" + clazzTwo + "')\n" +
                " AND symbol = '" + symbol() + "' " +
                "WITH TIMEZONE = 'Europe/Moscow', ROW_NUMBER(symbol RESPECT NULLS ORDER BY datetime, trade_num) BETWEEN 3 PRECEDING AND CURRENT ROW";
        String[][] expected = new String[][]{
                {"3572722987", "3572722987"},
                {"3572722988", "3572722987"},
                {"3572722989", "3572722987"},
                {"3572722990", "3572722988"},
                {"3572723289", "3572722989"},
                {"3572723449", "3572722990"}
        };

        assertSqlQueryRows(expected, sql);
    }
}
