package com.axibase.tsd.api.method.sql.trade.session_summary;

import com.axibase.tsd.api.method.sql.trade.SqlTradeTest;
import com.axibase.tsd.api.method.trade.session_summary.TradeSessionSummaryMethod;
import com.axibase.tsd.api.model.financial.TradeSessionStage;
import com.axibase.tsd.api.model.financial.TradeSessionSummary;
import com.axibase.tsd.api.model.financial.TradeSessionType;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TradeSessionSummaryDateFormatFilterTest extends SqlTradeTest {
    public static final String QUERY =
            "select datetime from atsd_session_summary " +
                    "where exchange = 'MOEX' and class = '{class}' and {date_format} " +
                    "group by exchange, class, symbol, period(1 day) " +
                    "WITH TIMEZONE = 'UTC'";

    @BeforeClass
    public void prepareData() throws Exception {
        insert(trade(1000).setExchange("MOEX"));

        TradeSessionSummary sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-04-16T15:55:15Z");
        sessionSummary.addTag("rptseq", "2");
        sessionSummary.addTag("offer", "10.5");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.MORNING, TradeSessionStage.N, "2020-04-17T23:15:55Z");
        sessionSummary.addTag("action", "test");
        sessionSummary.addTag("starttime", "10:15:20");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);

        sessionSummary = new TradeSessionSummary(clazz(), symbol(), TradeSessionType.DAY, TradeSessionStage.N, "2020-04-18T23:00:00Z");
        sessionSummary.addTag("action", "test");
        sessionSummary.addTag("starttime", "10:15:20");
        TradeSessionSummaryMethod.importStatistics(sessionSummary);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Day pattern less than date_format function")
                        .dateFormat("date_format(time, 'dd') < '17'")
                        .addExpected("2020-04-16T00:00:00.000000Z"),
                test("Day pattern less or equal date_format function")
                        .dateFormat("date_format(time, 'dd') <= '16'")
                        .addExpected("2020-04-16T00:00:00.000000Z"),
                test("Day pattern  equal date_format function")
                        .dateFormat("date_format(time, 'dd') = '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z"),
                test("Day pattern  not equal date_format function")
                        .dateFormat("date_format(time, 'dd') != '18'")
                        .addExpected("2020-04-16T00:00:00.000000Z")
                        .addExpected("2020-04-17T00:00:00.000000Z"),
                test("Day pattern greater than date_format function")
                        .dateFormat("date_format(time, 'dd') > '17'")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
                test("Day pattern greater or equal date_format function")
                        .dateFormat("date_format(time, 'dd') >= '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z")
                        .addExpected("2020-04-18T00:00:00.000000Z"),

                test("Day pattern less than extract function")
                        .dateFormat("extract(day from time) < '17'")
                        .addExpected("2020-04-16T00:00:00.000000Z"),
                test("Day pattern less or equal extract function")
                        .dateFormat("extract(day from time) <= '16'")
                        .addExpected("2020-04-16T00:00:00.000000Z"),
                test("Day pattern  equal extract function")
                        .dateFormat("extract(day from time) = '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z"),
                test("Day pattern  not equal extract function")
                        .dateFormat("extract(day from time) != '18'")
                        .addExpected("2020-04-16T00:00:00.000000Z")
                        .addExpected("2020-04-17T00:00:00.000000Z"),
                test("Day pattern greater than extract function")
                        .dateFormat("extract(day from time) > '17'")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
                test("Day pattern greater or equal extract function")
                        .dateFormat("extract(day from time) >= '17'")
                        .addExpected("2020-04-17T00:00:00.000000Z")
                        .addExpected("2020-04-18T00:00:00.000000Z"),

                test("HH:mm pattern not equal")
                        .dateFormat("date_format(time, 'HH:mm') != '23:15'")
                        .addExpected("2020-04-16T00:00:00.000000Z")
                        .addExpected("2020-04-18T00:00:00.000000Z"),

                test("Minute pattern less than")
                        .dateFormat("date_format(time, 'mm') < '15'")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
                test("Minute pattern less or equal")
                        .dateFormat("date_format(time, 'mm') <= '15'")
                        .addExpected("2020-04-17T00:00:00.000000Z")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
                test("Minute pattern  equal")
                        .dateFormat("date_format(time, 'mm') = '15'")
                        .addExpected("2020-04-17T00:00:00.000000Z"),
                test("Minute pattern not equal")
                        .dateFormat("date_format(time, 'mm') != '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
                test("Minute pattern greater than")
                        .dateFormat("date_format(time, 'mm') > '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z"),
                test("Minute pattern greater or equal")
                        .dateFormat("date_format(time, 'mm') >= '15'")
                        .addExpected("2020-04-16T00:00:00.000000Z")
                        .addExpected("2020-04-17T00:00:00.000000Z"),

                test("Day of week pattern")
                        .dateFormat("date_format(time, 'u') = '6'")
                        .addExpected("2020-04-18T00:00:00.000000Z"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("class", clazz());
        }


        public TestConfig dateFormat(String dateFormat) {
            setVariable("date_format", dateFormat);
            return this;
        }

    }

}