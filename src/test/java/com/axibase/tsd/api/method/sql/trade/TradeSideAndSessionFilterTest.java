package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class TradeSideAndSessionFilterTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z")).setNumber(1).setSide(Trade.Side.BUY).setSession(Trade.Session.E));
        trades.add(trade(getUnixTime("2020-03-22T10:00:49Z")).setNumber(2).setSide(Trade.Side.SELL).setSession(Trade.Session.S));
        trades.add(trade(getUnixTime("2020-03-22T10:00:55Z")).setNumber(3).setSide(Trade.Side.SELL));
        trades.add(trade(getUnixTime("2020-03-22T11:01:05Z")).setNumber(4).setSession(Trade.Session.N));
        trades.add(trade(getUnixTime("2020-03-22T11:01:49Z")).setNumber(5).setSide(Trade.Side.BUY).setSession(Trade.Session.L));
        trades.add(trade(getUnixTime("2020-03-22T11:01:50Z")).setNumber(6));
        trades.add(trade(getUnixTime("2020-03-22T11:01:50Z")).setNumber(7).setSession(Trade.Session.O));
        insert(trades);
    }

    @Test
    public void testSessionFilter() throws Exception {
        String sql = "select trade_num, side, session from atsd_trade where " + instrumentCondition() + " and session in ('E', 'S')";
        String[][] expected = {
                {"1", "B", "E"},
                {"2", "S", "S"}
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testSessionNegation() throws Exception {
        String sql = "select trade_num, side, session from atsd_trade where " + instrumentCondition() + " and session != 'E'";
        String[][] expected = {
                {"2", "S", "S"},
                {"4", "null", "N"},
                {"5", "B", "L"},
                {"7", "null", "O"},
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testSessionNotIn() throws Exception {
        String sql = "select trade_num, side, session from atsd_trade where " + instrumentCondition() + " and session not in ('E', 'N')";
        String[][] expected = {
                {"2", "S", "S"},
                {"5", "B", "L"},
                {"7", "null", "O"},
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testSideFilter() {
        String sql = "select trade_num, side, session from atsd_trade where " + instrumentCondition() + " and side = 'S'";
        String[][] expected = {
                {"2", "S", "S"},
                {"3", "S", "null"}
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testSideAndSessionFilter() {
        String sql = "select trade_num, side, session from atsd_trade where " + instrumentCondition()
                + " and side = 'B' and session = 'E'";
        String[][] expected = {
                {"1", "B", "E"},
        };
        assertSqlQueryRows(expected, sql);
    }

}