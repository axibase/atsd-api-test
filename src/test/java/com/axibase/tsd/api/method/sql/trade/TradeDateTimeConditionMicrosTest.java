package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TradeDateTimeConditionMicrosTest extends SqlTradeTest {
    String PLAIN_QUERY = "select trade_num from atsd_trade where {instrument} and {condition} WITH TIMEZONE='{timeZone}'";
    String AGGREGATION_QUERY = "select min(datetime), max(datetime), sum(quantity) from atsd_trade " +
            "where {instrument} and {condition} " +
            "group by exchange, class, symbol WITH TIMEZONE='{timeZone}'";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-05-19T10:21:49.123456Z").setNumber(1));
        trades.add(fromISOString("2020-05-19T10:21:49.123654Z").setNumber(2));
        trades.add(fromISOString("2020-05-19T10:21:49.123999Z").setNumber(3));
        trades.add(fromISOString("2020-05-19T10:25:00.123456Z").setNumber(4));
        trades.add(fromISOString("2020-05-19T10:25:49.123999Z").setNumber(5));
        insert(trades);
    }

    @Test(dataProvider = "testDataPlainQuery")
    public void testCondition(TestConfig config) {
        String sql = config.composeQuery(PLAIN_QUERY);
        assertSqlQueryRows(config.getDescription(), config.getExpected(), sql);
    }

    @Test(dataProvider = "testDataAggregation")
    public void testAggregation(TestConfig config) {
        String sql = config.composeQuery(AGGREGATION_QUERY);
        assertSqlQueryRows(config.getDescription(), config.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testDataPlainQuery() {
        TestConfig[] data = new TestConfig[]{
                test("datetime > '2020-05-19T10:21:49.123999Z'").addExpected("4").addExpected("5"),
                test("datetime > '2020-05-19T10:21:49.123999+00:00'").addExpected("4").addExpected("5"),
                test("datetime >= '2020-05-19T10:21:49.123999Z'").addExpected("3").addExpected("4").addExpected("5"),
                test("datetime < '2020-05-19T10:21:49.123999Z'").addExpected("1").addExpected("2"),
                test("datetime <= '2020-05-19T10:21:49.123999Z'").addExpected("1").addExpected("2").addExpected("3"),
                test("datetime > '2020-05-19T10:21:49.123654Z' and datetime < '2020-05-19T10:25:00.123456Z'").addExpected("3"),
                test("datetime >= '2020-05-19T10:21:49.123654Z' and datetime <= '2020-05-19T10:25:00.123456Z'").addExpected("2").addExpected("3").addExpected("4"),
                test("datetime between '2020-05-19T10:21:49.123654Z' EXCL and '2020-05-19T10:25:00.123456Z' EXCL").addExpected("3"),
                test("datetime between '2020-05-19T10:21:49.123654Z' INCL and '2020-05-19T10:25:00.123456Z' EXCL").addExpected("2").addExpected("3"),
                test("datetime between '2020-05-19T10:21:49.123654Z' EXCL and '2020-05-19T10:25:00.123456Z' INCL").addExpected("3").addExpected("4"),
                test("datetime between '2020-05-19T10:21:49.123654Z' INCL and '2020-05-19T10:25:00.123456Z' INCL").addExpected("2").addExpected("3").addExpected("4"),

                test("datetime > '2020-05-19 10:21:49.123999'").addExpected("4").addExpected("5"),
                test("datetime >= '2020-05-19 10:21:49.123999'").addExpected("3").addExpected("4").addExpected("5"),
                test("datetime < '2020-05-19 10:21:49.123999'").addExpected("1").addExpected("2"),
                test("datetime <= '2020-05-19 10:21:49.123999'").addExpected("1").addExpected("2").addExpected("3"),
                test("datetime > '2020-05-19 10:21:49.123654' and datetime < '2020-05-19 10:25:00.123456'").addExpected("3"),
                test("datetime >= '2020-05-19 10:21:49.123654' and datetime <= '2020-05-19 10:25:00.123456'").addExpected("2").addExpected("3").addExpected("4"),
                test("datetime between '2020-05-19 10:21:49.123654' EXCL and '2020-05-19 10:25:00.123456' EXCL").addExpected("3"),
                test("datetime between '2020-05-19 10:21:49.123654' INCL and '2020-05-19 10:25:00.123456' EXCL").addExpected("2").addExpected("3"),
                test("datetime between '2020-05-19 10:21:49.123654' EXCL and '2020-05-19 10:25:00.123456' INCL").addExpected("3").addExpected("4"),
                test("datetime between '2020-05-19 10:21:49.123654' INCL and '2020-05-19 10:25:00.123456' INCL").addExpected("2").addExpected("3").addExpected("4"),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    @DataProvider
    public Object[][] testDataAggregation() {
        TestConfig[] data = new TestConfig[]{
                test("datetime > '2020-05-19T10:21:49.123999Z'").addExpected("2020-05-19T10:25:00.123456Z", "2020-05-19T10:25:49.123999Z", "2"),
                test("datetime >= '2020-05-19T10:21:49.123999Z'").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:25:49.123999Z", "3"),
                test("datetime < '2020-05-19T10:21:49.123999Z'").addExpected("2020-05-19T10:21:49.123456Z", "2020-05-19T10:21:49.123654Z", "2"),
                test("datetime <= '2020-05-19T10:21:49.123999Z'").addExpected("2020-05-19T10:21:49.123456Z", "2020-05-19T10:21:49.123999Z", "3"),
                test("datetime > '2020-05-19T10:21:49.123654Z' and datetime < '2020-05-19T10:25:00.123456Z'").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:21:49.123999Z", "1"),
                test("datetime >= '2020-05-19T10:21:49.123654Z' and datetime <= '2020-05-19T10:25:00.123456Z'").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:25:00.123456Z", "3"),
                test("datetime between '2020-05-19T10:21:49.123654Z' EXCL and '2020-05-19T10:25:00.123456Z' EXCL").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:21:49.123999Z", "1"),
                test("datetime between '2020-05-19T10:21:49.123654Z' INCL and '2020-05-19T10:25:00.123456Z' EXCL").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:21:49.123999Z", "2"),
                test("datetime between '2020-05-19T10:21:49.123654Z' EXCL and '2020-05-19T10:25:00.123456Z' INCL").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:25:00.123456Z", "2"),
                test("datetime between '2020-05-19T10:21:49.123654Z' INCL and '2020-05-19T10:25:00.123456Z' INCL").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:25:00.123456Z", "3"),

                test("datetime > '2020-05-19 10:21:49.123999'").addExpected("2020-05-19T10:25:00.123456Z", "2020-05-19T10:25:49.123999Z", "2"),
                test("datetime >= '2020-05-19 10:21:49.123999'").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:25:49.123999Z", "3"),
                test("datetime < '2020-05-19 10:21:49.123999'").addExpected("2020-05-19T10:21:49.123456Z", "2020-05-19T10:21:49.123654Z", "2"),
                test("datetime <= '2020-05-19 10:21:49.123999'").addExpected("2020-05-19T10:21:49.123456Z", "2020-05-19T10:21:49.123999Z", "3"),
                test("datetime > '2020-05-19 10:21:49.123654' and datetime < '2020-05-19 10:25:00.123456'").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:21:49.123999Z", "1"),
                test("datetime >= '2020-05-19 10:21:49.123654' and datetime <= '2020-05-19 10:25:00.123456'").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:25:00.123456Z", "3"),
                test("datetime between '2020-05-19 10:21:49.123654' EXCL and '2020-05-19 10:25:00.123456' EXCL").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:21:49.123999Z", "1"),
                test("datetime between '2020-05-19 10:21:49.123654' INCL and '2020-05-19 10:25:00.123456' EXCL").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:21:49.123999Z", "2"),
                test("datetime between '2020-05-19 10:21:49.123654' EXCL and '2020-05-19 10:25:00.123456' INCL").addExpected("2020-05-19T10:21:49.123999Z", "2020-05-19T10:25:00.123456Z", "2"),
                test("datetime between '2020-05-19 10:21:49.123654' INCL and '2020-05-19 10:25:00.123456' INCL").addExpected("2020-05-19T10:21:49.123654Z", "2020-05-19T10:25:00.123456Z", "3"),
        };
        return TestUtil.convertTo2DimArray(data);
    }


    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("condition", description);
            timeZone("UTC");
        }

        private TestConfig timeZone(String timeZone) {
            setVariable("timeZone", timeZone);
            return this;
        }
    }
}