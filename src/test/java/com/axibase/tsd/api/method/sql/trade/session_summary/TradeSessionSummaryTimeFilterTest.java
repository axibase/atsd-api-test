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

public class TradeSessionSummaryTimeFilterTest extends SqlTradeTest {
    private static final String QUERY = "select rptseq from atsd_session_summary where {where}";
    private final String clazzTwo = Mocks.tradeClass();

    @BeforeClass
    public void prepareData() throws Exception {
        insert(
                trade(1000).setExchange("MOEX"),
                trade(1000).setClazz(clazzTwo).setSymbol(symbolTwo()).setExchange("MOEX")
        );
        List<TradeSessionSummary> list = new ArrayList<>();
        String dateOne = "2020-09-10T10:15:20Z";
        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, dateOne);
        sessionSummary.addTag("rptseq", "1");

        list.add(sessionSummary);

        String dateTwo = "2020-09-10T12:15:20Z";
        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.DAY, TradeSessionStage.C, dateTwo);
        sessionSummary.addTag("rptseq", "2");

        list.add(sessionSummary);

        String dateThree = "2020-09-10T10:45:20Z";
        sessionSummary = new TradeSessionSummary(clazzTwo, symbolTwo(), TradeSessionType.MORNING, TradeSessionStage.E, dateThree);
        sessionSummary.addTag("rptseq", "3");

        list.add(sessionSummary);

        String dateFour = "2020-09-10T11:45:20Z";
        sessionSummary = new TradeSessionSummary(clazzTwo, symbolTwo(), TradeSessionType.MORNING, TradeSessionStage.E, dateFour);
        sessionSummary.addTag("rptseq", "4");

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
        TestConfig[] data = new TestConfig[]{
                test("exchange='MOEX' and class in ('" + clazz() + "', '" + clazzTwo + "') and time > '2020-09-10T11:00:20Z'")
                        .addExpected("2").addExpected("4"),
                test("exchange='MOEX' and ((class='" + clazz() + "' and datetime between '2020-09-10T12:00:00Z' and '2020-09-10T12:30:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime between '2020-09-10T10:00:20Z' and '2020-09-10T10:50:20Z'))")
                        .addExpected("2").addExpected("3"),
                test("exchange='MOEX' and ((class='" + clazz() + "' and datetime between '2020-09-10T12:00:00Z' and '2020-09-10T12:30:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime > '2020-09-10T10:00:20Z'))")
                        .addExpected("2").addExpected("3").addExpected("4"),
                test("exchange='MOEX' and ((class='" + clazz() + "' and datetime < '2020-09-10T12:00:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime > '2020-09-10T11:00:20Z'))")
                        .addExpected("1").addExpected("4"),
                test("exchange='MOEX' and ((class='" + clazz() + "' and datetime < '2020-09-10T12:00:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime > '2020-09-10T11:00:20Z'))")
                        .addExpected("1").addExpected("4"),
                test("((exchange='MOEX' and class='" + clazz() + "' and datetime < '2020-09-10T12:00:00Z' ) " +
                        "OR (exchange='MOEX' and class = '" + clazzTwo + "' and datetime > '2020-09-10T11:00:20Z'))")
                        .addExpected("1").addExpected("4"),
                test("((exchange='MOEX' and class='" + clazz() + "' and datetime < '2020-09-10T12:00:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime > '2020-09-10T11:00:20Z'))")
                        .addExpected("1").addExpected("4"),
                test("exchange='MOEX' and datetime > '2020-09-10T11:00:00Z' and ((class='" + clazz() + "' and datetime < '2020-09-10T13:00:00Z' ) " +
                        "OR (class = '" + clazzTwo + "' and datetime > '2020-09-10T10:00:20Z'))")
                        .addExpected("2").addExpected("4"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String where) {
        return new TestConfig(where);
    }

    private static class TestConfig extends SqlTestConfig<TestConfig> {

        public TestConfig(String where) {
            super(where);
            setVariable("where", where);
        }
    }
}