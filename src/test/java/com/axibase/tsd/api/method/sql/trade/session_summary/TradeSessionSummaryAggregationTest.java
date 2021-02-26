package com.axibase.tsd.api.method.sql.trade.session_summary;

import com.axibase.tsd.api.method.sql.trade.SqlTradeTest;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.financial.TradeSessionStage;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.model.financial.TradeSessionType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeSessionSummaryAggregationTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        insert(trade(1000).setExchange("MOEX"), trade(1000).setSymbol(symbolTwo()).setExchange("MOEX"));
        List<TradeSessionSummary> list = new ArrayList<>();
        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:45:20Z");
        sessionSummary.addTag("value", "5.5");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T12:15:20Z");
        sessionSummary.addTag("value", "6.5");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbolTwo(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:45:20Z");
        sessionSummary.addTag("value", "7.5");

        list.add(sessionSummary);

        TradeSessionSummaryMethod.importStatistics(list);
    }

    @Test
    public void test() throws Exception {
        String query = "SELECT exchange, class, symbol, MAX(value), \n" +
                "date_format(MAX_VALUE_TIME(value)) AS mxdt, \n" +
                "date_format(FIRST(time)) AS fdt \n" +
                "FROM atsd_session_summary \n" +
                "WHERE exchange = 'MOEX' AND class = '" + clazz() + "'\n" +
                "AND type='MORNING' and stage='N' \n" +
                "GROUP BY symbol, class, exchange";
        String[][] expectedRows = {
                {"MOEX", clazz(), symbol(), "6.5", "2020-09-10T12:15:20.000Z", "2020-09-10T10:45:20.000Z"},
                {"MOEX", clazz(), symbolTwo(), "7.5", "2020-09-10T10:45:20.000Z", "2020-09-10T10:45:20.000Z"}
        };
        assertSqlQueryRows(expectedRows, query);
    }
}