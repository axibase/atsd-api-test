package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TradeCastFunctionTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        Trade trade = fromISOString("2020-05-19T10:21:49.123456Z").setNumber(Long.MAX_VALUE);
        insert(trade);
    }

    @Test
    public void testCastTimeAsNumber() {
        String sql = "select datetime, cast(datetime as number) from atsd_trade where " + instrumentCondition();
        String[][] expected = new String[][]{
                {"2020-05-19T10:21:49.123456Z", String.valueOf(Util.getUnixTime("2020-05-19T10:21:49.123Z"))}
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testCastTimeAsNumberCaseOperator() {
        String sql = "select datetime, cast(case when class = 'TQBR' then NULL else datetime end as number) from atsd_trade where " + instrumentCondition();
        String[][] expected = new String[][]{
                {"2020-05-19T10:21:49.123456Z", String.valueOf(Util.getUnixTime("2020-05-19T10:21:49.123Z"))}
        };
        assertSqlQueryRows(expected, sql);
    }

    @Test
    public void testCastCaseOperatorAsDate() {
        String sql = "select datetime, cast(case when class = 'TQBR' then NULL else datetime end as date) from atsd_trade where " + instrumentCondition();
        String[][] expected = new String[][]{
                {"2020-05-19T10:21:49.123456Z", "2020-05-19T10:21:49.123456Z"}
        };
        assertSqlQueryRows(expected, sql);
    }
}