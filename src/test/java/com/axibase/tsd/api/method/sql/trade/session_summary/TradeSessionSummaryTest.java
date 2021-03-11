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
    private static final String QUERY = "select datetime, class, symbol, type, stage, rptseq, offer, assured, action, starttime, snapshot_datetime, \"time\", value from atsd_session_summary " +
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
        sessionSummary.addTag("time", "10:00:00");
        sessionSummary.addTag("value", "5.5");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.DAY, TradeSessionStage.C, "2020-09-10T12:15:20Z");
        sessionSummary.addTag("rptseq", "3");
        sessionSummary.addTag("offer", "11.25");
        sessionSummary.addTag("assured", "false");
        sessionSummary.addTag("action", "test2");
        sessionSummary.addTag("starttime", "16:15:20");
        sessionSummary.addTag("snapshot_datetime", "2020-09-10T10:25:20Z");
        sessionSummary.addTag("time", "12:00:00");
        sessionSummary.addTag("value", "6.5");
        sessionSummary.addTag("iopen", "1298.93");

        list.add(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazzTwo, symbolTwo(), TradeSessionType.MORNING, TradeSessionStage.E, "2020-09-10T10:45:20Z");
        sessionSummary.addTag("rptseq", "4");
        sessionSummary.addTag("offer", "21.25");
        sessionSummary.addTag("assured", "true");
        sessionSummary.addTag("action", "test3");
        sessionSummary.addTag("starttime", "17:15:21");
        sessionSummary.addTag("snapshot_datetime", "2020-09-11T10:25:20Z");
        sessionSummary.addTag("time", "12:30:15");
        sessionSummary.addTag("value", "7.5");

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
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z", "10:00:00", "5.5")
                ,
                test("class='" + clazz() + "'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z", "10:00:00", "5.5")
                        .addExpected("2020-09-10T12:15:20.000000Z", clazz(), symbol(), "Day", "C", "3", "11.25", "false", "test2", "16:15:20", "2020-09-10T10:25:20.000000Z", "12:00:00", "6.5")
                ,
                test("class='" + clazz() + "' and type='Day'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T12:15:20.000000Z", clazz(), symbol(), "Day", "C", "3", "11.25", "false", "test2", "16:15:20", "2020-09-10T10:25:20.000000Z", "12:00:00", "6.5")
                ,
                test("class='" + clazz() + "' and stage='N'")
                        .startTime("2020-09-10T10:00:00Z")
                        .endTime("2020-09-10T13:00:00Z")
                        .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z", "10:00:00", "5.5")
                , test("(class='" + clazz() + "' and symbol='" + symbol() + "' or class = '" + clazzTwo + "' and symbol='" + symbolTwo() + "')")
                .startTime("2020-09-10T10:00:00Z")
                .endTime("2020-09-10T11:00:00Z")
                .addExpected("2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z", "10:00:00", "5.5")
                .addExpected("2020-09-10T10:45:20.000000Z", clazzTwo, symbolTwo(), "Morning", "E", "4", "21.25", "true", "test3", "17:15:21", "2020-09-11T10:25:20.000000Z", "12:30:15", "7.5")

        };
        return TestUtil.convertTo2DimArray(data);
    }

    @Test
    public void testSubQuery() throws Exception {
        String subQuery = "select datetime, class, symbol, type, stage, rptseq, offer, assured, action, starttime, snapshot_datetime from atsd_session_summary " +
                "where class= '" + clazz() + "' and symbol = '" + symbol() + "'  WITH TIMEZONE = 'UTC'";
        String query = "select * from (" + subQuery + ") where stage = 'N'";
        String[][] expectedRows = {
                {"2020-09-10T10:15:20.000000Z", clazz(), symbol(), "Morning", "N", "2", "10.5", "true", "test", "10:15:20", "2020-09-10T10:15:20.000000Z"}
        };
        assertSqlQueryRows("Wrong result in subquery", expectedRows, query);
    }

    @Test
    public void testAggregateWithPeriod() throws Exception {
        String sql = "select datetime, count(*), class from atsd_session_summary " +
                "where class in ('" + clazz() + "', '" + clazzTwo + "') and symbol in ('" + symbol() + "', '" + symbolTwo() + "')  " +
                " group by class, period(1 DAY) " +
                " WITH TIMEZONE = 'UTC'";
        String[][] expectedRows = {
                {"2020-09-10T00:00:00.000000Z", "2", clazz()},
                {"2020-09-10T00:00:00.000000Z", "1", clazzTwo},
        };
        assertSqlQueryRows("Wrong result in query with daily period", expectedRows, sql);

        sql = "select datetime, count(*), class from atsd_session_summary " +
                "where class in ('" + clazz() + "', '" + clazzTwo + "') and symbol in ('" + symbol() + "', '" + symbolTwo() + "')  " +
                " group by class, period(1 HOUR) " +
                " WITH TIMEZONE = 'UTC'";
        String[][] expectedRowsHourly = {
                {"2020-09-10T10:00:00.000000Z", "1", clazz()},
                {"2020-09-10T12:00:00.000000Z", "1", clazz()},
                {"2020-09-10T10:00:00.000000Z", "1", clazzTwo},
        };
        assertSqlQueryRows("Wrong result in query with hourly period", expectedRowsHourly, sql);
    }

    @Test
    public void testColumns() throws Exception {
        String query = "select class, symbol, iopen from atsd_session_summary where class='" + clazz() + "' and symbol='" + symbol() + "'";
        String[][] expectedRows = {
                {clazz(), symbol(), null},
                {clazz(), symbol(), "1298.93"},
        };
        assertSqlQueryRows(expectedRows, query);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private static class TestConfig extends SqlTestConfig<TestConfig> {

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