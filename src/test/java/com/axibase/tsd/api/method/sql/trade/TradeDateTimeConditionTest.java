package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeDateTimeConditionTest extends SqlTradeTest {
    public static final String TEMPLATE = "select trade_num from atsd_trade where {instrument} " +
            " and time between {from} and {to} {toExcl} " +
            "with timezone='UTC'";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-05-19T10:21:49.123456Z").setNumber(1));
        trades.add(fromISOString("2020-05-19T10:21:49.123654Z").setNumber(2));
        trades.add(fromISOString("2020-05-19T10:21:49.123999Z").setNumber(3));
        trades.add(fromISOString("2020-10-10T10:20:30Z").setNumber(4).setSymbol(symbolTwo()));
        trades.add(fromISOString("2020-10-11T15:22:33Z").setNumber(5).setSymbol(symbolTwo()));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void testWhereClauseCondition(SqlTestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery(TEMPLATE);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @Test(dataProvider = "testDataArithmetic")
    public void testCalendarArithmetic(SqlTestConfig testConfig) throws Exception {
        String query = "select trade_num from atsd_trade where {instrument} " +
                " and {condition} with timezone='UTC'";
        String sql = testConfig.composeQuery(query);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        SqlTestConfig[] data = {
                test("Single value")
                        .from("'2020-05-19T10:21:49.123457Z'")
                        .to("'2020-05-19T10:21:49.123990Z'")
                        .addExpected("2"),
                test("Single value to exclusive")
                        .from("'2020-05-19T10:21:49.123457Z'")
                        .to("'2020-05-19T10:21:49.123999Z'")
                        .toExcl("excl")
                        .addExpected("2"),
                test("From inclusive to exclusive")
                        .from("'2020-05-19T10:21:49.123456Z'")
                        .to("'2020-05-19T10:21:49.123999Z'")
                        .toExcl("excl")
                        .addExpected("1")
                        .addExpected("2"),
                test("From and to inclusive")
                        .from("'2020-05-19T10:21:49.123456Z'")
                        .to("'2020-05-19T10:21:49.123999Z'")
                        .addExpected("1")
                        .addExpected("2")
                        .addExpected("3"),
                test("Between with constants low boundary")
                        .instrument(instrumentTwoCondition())
                        .from("'2020-10-10T12:00:00Z' - 1*hour - 45*minute")
                        .to("'2020-10-11T15:00:00Z'")
                        .addExpected("4"),
                test("Between with constants  both boundaries")
                        .instrument(instrumentTwoCondition())
                        .from("'2020-10-10T12:00:00Z' - 1*hour - 45*minute")
                        .to("'2020-10-11T15:00:00Z' + 22 * minute + 34 * second")
                        .addExpected("4")
                        .addExpected("5"),
                test("Between with workday function low boundary")
                        .instrument(instrumentTwoCondition())
                        .from("workday('2020-10-11', -1) + 1*day + 11*hour")
                        .to("'2020-10-11T15:00:00Z' + 22 * minute + 34 * second")
                        .addExpected("5"),
                test("Between with workday function both boundaries")
                        .instrument(instrumentTwoCondition())
                        .from("workday('2020-10-11', -1) + 1*day + 10*hour")
                        .to("workday('2020-10-14', -1) - 2*day")
                        .addExpected("4"),

        };
        return TestUtil.convertTo2DimArray(data);
    }

    @DataProvider
    public Object[][] testDataArithmetic() throws Exception {
        SqlTestConfig[] data = {
                test("Greater then with time constant both values")
                        .instrument(instrumentTwoCondition())
                        .condition("time > '2020-10-10T12:00:00Z' - 1*hour - 45*minute")
                        .addExpected("4")
                        .addExpected("5"),
                test("Greater then with time constant single value")
                        .instrument(instrumentTwoCondition())
                        .condition("time > '2020-10-10T12:00:00Z' - 1*hour")
                        .addExpected("5"),
                test("Less than workday function")
                        .instrument(instrumentTwoCondition())
                        .condition("time < workday('2020-10-14', -1) - 2*day")
                        .addExpected("4"),
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
            condition("");
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

        private TestConfig condition(String condition) {
            setVariable("condition", condition);
            return this;
        }
    }
}