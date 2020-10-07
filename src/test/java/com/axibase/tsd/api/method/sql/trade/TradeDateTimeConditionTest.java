package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeDateTimeConditionTest extends SqlTradeTest {
    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-05-19T10:21:49.123456Z").setNumber(1));
        trades.add(fromISOString("2020-05-19T10:21:49.123654Z").setNumber(2));
        trades.add(fromISOString("2020-05-19T10:21:49.123999Z").setNumber(3));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void testDateFormat(SqlTestConfig testConfig) throws Exception {
        String template = "select trade_num from atsd_trade where {instrument} " +
                " and time between '{from}' and '{to}' {toExcl} " +
                "with timezone='UTC'";
        String sql = testConfig.composeQuery(template);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        SqlTestConfig[] data = {
                test("Single value").from("2020-05-19T10:21:49.123457Z").to("2020-05-19T10:21:49.123990Z").addExpected("2"),
                test("Single value to exclusive")
                        .from("2020-05-19T10:21:49.123457Z")
                        .to("2020-05-19T10:21:49.123999Z")
                        .toExcl("excl")
                        .addExpected("2"),
                test("From inclusive to exclusive")
                        .from("2020-05-19T10:21:49.123456Z")
                        .to("2020-05-19T10:21:49.123999Z")
                        .toExcl("excl")
                        .addExpected("1")
                        .addExpected("2"),
                test("From and to inclusive")
                        .from("2020-05-19T10:21:49.123456Z")
                        .to("2020-05-19T10:21:49.123999Z")
                        .addExpected("1")
                        .addExpected("2")
                        .addExpected("3"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            toExcl("");
        }

        private TestConfig from(String from) {
            setVariable("from", from);
            return this;
        }

        private TestConfig to(String to) {
            setVariable("to", to);
            return this;
        }

        private TestConfig toExcl(String toExcl) {
            setVariable("toExcl", toExcl);
            return this;
        }
    }
}