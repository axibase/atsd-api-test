package com.axibase.tsd.api.method.sql.trade.session_summary;

import com.axibase.tsd.api.method.sql.trade.SqlTradeTest;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.financial.TradeSessionStage;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.model.financial.TradeSessionType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeSessionSummaryTest extends SqlTradeTest {
    private static final String QUERY = "select datetime, class, symbol, type, stage, rptseq, offer, assured, action, starttime, snapshot_datetime from atsd_session_summary " +
            "where {where} and time between '{startTime}' and '{endTime}'   WITH TIMEZONE = 'UTC'";
    private final String clazzTwo = Mocks.tradeClass();


    @BeforeClass
    public void prepareData() throws Exception {
        insert(trade(1000).setExchange("MOEX"), trade(1000).setClazz(clazzTwo).setSymbol(symbolTwo()).setExchange("MOEX"));
        List<TradeSessionSummary> list = new ArrayList<>();
        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-09-10T10:15:20Z");
        sessionSummary.addTag("rptseq", "2");
        sessionSummary.addTag("offer", "10.5");
        sessionSummary.addTag("assured", "true");
        sessionSummary.addTag("action", "test");
        sessionSummary.addTag("starttime", "10:15:20");
        sessionSummary.addTag("snapshot_datetime", "2020-09-10T10:15:20Z");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.DAY, TradeSessionStage.C, "2020-09-10T12:15:20Z");
        sessionSummary.addTag("rptseq", "3");
        sessionSummary.addTag("offer", "11.25");
        sessionSummary.addTag("assured", "false");
        sessionSummary.addTag("action", "test2");
        sessionSummary.addTag("starttime", "16:15:20");
        sessionSummary.addTag("snapshot_datetime", "2020-09-10T10:25:20Z");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazzTwo, symbolTwo(), TradeSessionType.MORNING, TradeSessionStage.E, "2020-09-10T10:45:20Z");
        sessionSummary.addTag("rptseq", "4");
        sessionSummary.addTag("offer", "21.25");
        sessionSummary.addTag("assured", "true");
        sessionSummary.addTag("action", "test3");
        sessionSummary.addTag("starttime", "17:15:21");
        sessionSummary.addTag("snapshot_datetime", "2020-09-11T10:25:20Z");

        list.add(sessionSummary);

        TradeSessionSummaryMethod.importStatistics(list);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("class='" + clazz() + "' and symbol='" + symbol() + "'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T11:00:00Z")
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "REGULAR", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z")
                ,
                test("class='" + clazz() + "'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "REGULAR", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z")
                        .addExpected("2020-09-10T12:15:20.000000Z", clazz(), symbol(), "Day", "CLOSED", "3", "11.25", "false", "test2", "16:15:20", "2020-09-10T10:25:20.000000Z")
                ,
                test("class='" + clazz() + "' and type='Day'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T12:15:20.000000Z", clazz(), symbol(), "Day", "CLOSED", "3", "11.25", "false", "test2", "16:15:20", "2020-09-10T10:25:20.000000Z")
                ,
                test("class='" + clazz() + "' and stage='REGULAR'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "REGULAR", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z")
                , test("(class='" + clazz() + "' and symbol='" + symbol() + "' or class = '" + clazzTwo + "' and symbol='" + symbolTwo() + "')")
                .startTime("2020-09-10T10:00:00Z")
                .endTime("2020-09-10T11:00:00Z")
                .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "REGULAR", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z")
                .addExpected("2020-09-10T10:45:20.000000Z", clazzTwo, symbolTwo(), "Morning", "CLOSING_AUCTION_POST_CROSSING", "4", "21.25", "true", "test3", "17:15:21", "2020-09-11T10:25:20.000000Z")

        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends SqlTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            where(description);
        }

        private TestConfig where(String where) {
            setVariable("where", where);
            return this;
        }

        private TestConfig startTime(String startTime) {
            setVariable("startTime", startTime);
            return this;
        }

        private TestConfig endTime(String endTime) {
            setVariable("endTime", endTime);
            return this;
        }
    }
}