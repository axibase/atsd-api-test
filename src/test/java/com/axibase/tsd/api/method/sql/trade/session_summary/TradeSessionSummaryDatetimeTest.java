package com.axibase.tsd.api.method.sql.trade.session_summary;

import com.axibase.tsd.api.method.sql.trade.SqlTradeTest;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.financial.TradeSessionStage;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.model.financial.TradeSessionType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TradeSessionSummaryDatetimeTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        insert(trade(1000).setExchange("MOEX"));

        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:15:20Z");
        sessionSummary.addTag("rptseq", "2");
        sessionSummary.addTag("offer", "10.5");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:30:30Z");
        sessionSummary.addTag("action", "test");
        sessionSummary.addTag("starttime", "10:15:20");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);
    }

    @Test
    public void test() {
        String query = "select datetime, class, symbol, type, stage, rptseq, offer, action, starttime from atsd_session_summary " +
                "where class='" + clazz() + "'";
        String[][] expectedRows = {
                {"2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", null, null},
                {"2020-09-10T10:30:30.000000Z", clazz(), symbol(), "Morning", "N", null, null, "test", "10:15:20"},
        };
        assertSqlQueryRows(expectedRows, query);
    }
}