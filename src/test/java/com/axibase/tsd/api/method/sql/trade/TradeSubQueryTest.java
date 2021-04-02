package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class TradeSubQueryTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setPrice(new BigDecimal("126.99")).setQuantity(22330).setSide(Trade.Side.BUY));
        trades.add(fromISOString("2020-03-22T10:09:00.654321Z").setPrice(new BigDecimal("127.36")).setQuantity(22330).setSide(Trade.Side.SELL).setSession(Trade.Session.S));
        trades.add(fromISOString("2020-03-22T10:49:00Z").setPrice(new BigDecimal("127.02")).setQuantity(22339).setSession(Trade.Session.N));
        trades.add(fromISOString("2020-03-22T10:49:00Z").setPrice(new BigDecimal("127.02")).setQuantity(22339).setSession(Trade.Session.N).setSymbol(symbolTwo()));
        insert(trades);
    }

    @Test
    public void testSimpleSubQuery() throws Exception {
        String sql = "select exchange, class, symbol, datetime, price, quantity, side, session from (" +
                "select exchange, class, symbol, datetime, price, quantity, side, trade_num, session from atsd_trade where " + instrumentCondition() + ")";
        String[][] expectedRows = {
                {exchange(), clazz(), symbol(), "2020-03-22T10:01:00.123456Z", "126.99", "22330", "B", null},
                {exchange(), clazz(), symbol(), "2020-03-22T10:09:00.654321Z", "127.36", "22330", "S", "S"},
                {exchange(), clazz(), symbol(), "2020-03-22T10:49:00.000000Z", "127.02", "22339", null, "N"},
        };

        assertSqlQueryRows("Wrong result in simple subquery", expectedRows, sql);
    }

    @Test
    public void testSelectAllSubQuery() throws Exception {
        String sql = "select * from (" +
                "select datetime, exchange, class, symbol, price, quantity, side, session from atsd_trade where " + instrumentCondition() + ")";
        String[][] expectedRows = {
                {"2020-03-22T10:01:00.123456Z", exchange(), clazz(), symbol(), "126.99", "22330", "B", null},
                {"2020-03-22T10:09:00.654321Z", exchange(), clazz(), symbol(), "127.36", "22330", "S", "S"},
                {"2020-03-22T10:49:00.000000Z", exchange(), clazz(), symbol(), "127.02", "22339", null, "N"},
        };

        assertSqlQueryRows("Wrong result in select * subquery", expectedRows, sql);
    }

    @Test
    public void testSubQueryWithAggregation() throws Exception {
        String sql = "select datetime, exchange, class, symbol, cnt from (" +
                "select datetime, exchange, class, symbol, count(*) as cnt from atsd_trade where " + instrumentCondition() +
                " group by exchange, class, symbol, period(10 minute)) where cnt > 1";

        String[][] expectedRows = {
                {"2020-03-22T10:00:00.000000Z", exchange(), clazz(), symbol(), "2"},

        };
        assertSqlQueryRows("Wrong result in aggregation subquery", expectedRows, sql);
    }

    @Test
    public void testSubQueryWithAggregationAndAlias() throws Exception {
        String sql = "select t1.datetime, t1.exchange, t1.class, t1.symbol, t1.cnt from (" +
                "select datetime, exchange, class, symbol, count(*) as cnt from atsd_trade where " + instrumentCondition() +
                " group by exchange, class, symbol, period(10 minute)) t1 where t1.cnt > 1";

        String[][] expectedRows = {
                {"2020-03-22T10:00:00.000000Z", exchange(), clazz(), symbol(), "2"},

        };
        assertSqlQueryRows("Wrong result in aggregation subquery with alias", expectedRows, sql);
    }


    @Test
    public void testSubQueryWithAggregationMultipleInstruments() throws Exception {
        String sql = "select datetime, exchange, class, symbol, cnt from (" +
                "select datetime, exchange, class, symbol, count(*) as cnt from atsd_trade where " + classCondition() +
                " group by exchange, class, symbol, period(10 minute) order by symbol) where cnt < 2";

        String[][] expectedRows = {
                {"2020-03-22T10:40:00.000000Z", exchange(), clazz(), symbol(), "1"},
                {"2020-03-22T10:40:00.000000Z", exchange(), clazz(), symbolTwo(), "1"},

        };
        assertSqlQueryRows("Wrong result in aggregation subquery multiple instruments", expectedRows, sql);
    }

    @Test
    public void testAggregateOfAggregationSubQuery() throws Exception {
        String sql = "select sum(cnt) from (" +
                "select datetime, exchange, class, symbol, count(*) as cnt from atsd_trade where " + instrumentCondition() +
                " group by exchange, class, symbol, period(10 minute))";

        String[][] expectedRows = {
                {"3"},

        };
        assertSqlQueryRows("Wrong result in aggregation subquery", expectedRows, sql);
    }

    @Test
    public void testSubQueryWithRowNumberFunction() {
        String sql = "SELECT * FROM (\n" +
                "SELECT datetime, " +
                " ROW_NUMBER() AS rn\n" +
                " FROM atsd_trade\n" +
                "WHERE " + instrumentCondition() +
                " WITH ROW_NUMBER(symbol ORDER BY trade_num) >= 0\n" +
                ") WHERE rn > 1 and rn < 3";

        String[][] expectedRows = {
                {"2020-03-22T10:09:00.654321Z", "2"},
        };
        assertSqlQueryRows("Wrong result in row number subquery", expectedRows, sql);
    }


    @Test
    public void testNestedSubQuery() {
        String nested = "SELECT rn FROM (\n" +
                "SELECT datetime, " +
                " ROW_NUMBER() AS rn\n" +
                " FROM atsd_trade\n" +
                "WHERE " + instrumentCondition() +
                " WITH ROW_NUMBER(symbol ORDER BY trade_num) >= 0\n" +
                ") WHERE rn > 1 and rn <= 3";
        String sql = "SELECT * from (" + nested + ") where rn < 3";
        String[][] expectedRows = {
                {"2"},
        };
        assertSqlQueryRows("Wrong result in nested subquery", expectedRows, sql);
    }


}