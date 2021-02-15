package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeQueryWithRowNumberTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        String[] timestamps = {
                "2020-05-19T10:21:49.123001Z",
                "2020-05-19T10:59:59.999999Z",
                "2020-05-20T10:35:15.123001Z",
                "2020-05-20T10:43:03.456002Z",
                "2020-05-20T11:00:00.000003Z",
                "2020-05-20T11:05:00.000004Z",
        };

        // first instrument
        List<Trade> trades = new ArrayList<>(fromISO(timestamps));
        insert(trades);
    }

    @Test
    public void testTimestampPrecision() throws Exception {
        String sql = "SELECT datetime, count(*) \n" +
                " FROM atsd_trade\n" +
                "WHERE " + instrumentCondition() + "\n" +
                "  WITH ROW_NUMBER(symbol ORDER BY datetime, trade_num) BETWEEN 2 PRECEDING AND CURRENT ROW\n" +
                "WITH TIMEZONE = 'UTC'\n";
        String[][] expectedRows = {
                {"2020-05-19T10:21:49.123001Z", "1"},
                {"2020-05-19T10:59:59.999999Z", "2"},
                {"2020-05-20T10:35:15.123001Z", "2"},
                {"2020-05-20T10:43:03.456002Z", "2"},
                {"2020-05-20T11:00:00.000003Z", "2"},
                {"2020-05-20T11:05:00.000004Z", "2"},
        };
        assertSqlQueryRows(expectedRows, sql);
    }
}