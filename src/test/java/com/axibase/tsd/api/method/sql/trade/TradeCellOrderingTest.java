package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TradeCellOrderingTest extends SqlTradeTest {
    private static final String QUERY = "select trade_num from atsd_trade where {instrument} {orderBy} {limit}";

    private final long numOne = Long.MAX_VALUE;
    private final long numTwo = Long.MAX_VALUE - 1;
    private final long numThree = Long.MAX_VALUE - 2;
    private final long numFour = Long.MAX_VALUE - 3;

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(fromISOString("2020-06-19T10:05:01.123456Z").setNumber(numOne));
        trades.add(fromISOString("2020-06-19T10:05:01.123456Z").setNumber(numTwo));
        trades.add(fromISOString("2020-06-19T10:55:01.123456Z").setNumber(numThree));
        trades.add(fromISOString("2020-06-19T10:55:01.123456Z").setNumber(numFour));
        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("Test order by time asc")
                        .orderBy("datetime asc, trade_num asc")
                        .addExpected(numTwo, numOne, numFour, numThree),
                test("Test order by time asc with limit 1")
                        .orderBy("datetime asc, trade_num asc")
                        .limit(1)
                        .addExpected(numTwo),
                test("Test order by time asc with limit 3")
                        .orderBy("datetime asc, trade_num asc")
                        .limit(3)
                        .addExpected(numTwo, numOne, numFour),

                test("Test order by time desc")
                        .orderBy("datetime desc, trade_num desc")
                        .addExpected(numThree, numFour, numOne, numTwo),
                test("Test order by time desc with limit 1")
                        .orderBy("datetime desc, trade_num desc")
                        .limit(1)
                        .addExpected(numThree),
                test("Test order by time desc with limit 3")
                        .orderBy("datetime desc, trade_num desc")
                        .limit(3)
                        .addExpected(numThree, numFour, numOne),

                test("Test order by trade_num asc")
                        .orderBy("trade_num asc")
                        .addExpected(numFour, numThree, numTwo, numOne),
                test("Test order by trade_num asc with limit 1")
                        .orderBy("trade_num asc")
                        .limit(1)
                        .addExpected(numFour),
                test("Test order by trade_num asc with limit 3")
                        .orderBy("trade_num asc")
                        .limit(3)
                        .addExpected(numFour, numThree, numTwo),

                test("Test order by trade_num desc")
                        .orderBy("trade_num desc")
                        .addExpected(numOne, numTwo, numThree, numFour),
                test("Test order by trade_num desc with limit 1")
                        .orderBy("trade_num desc")
                        .limit(1)
                        .addExpected(numOne),
                test("Test order by trade_num desc with limit 3")
                        .orderBy("trade_num desc")
                        .limit(3)
                        .addExpected(numOne, numTwo, numThree),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("orderBy", "");
            setVariable("limit", "");
        }

        private TestConfig limit(int limit) {
            setVariable("limit", "limit " + limit);
            return this;
        }

        private TestConfig orderBy(String orderBy) {
            setVariable("orderBy", "order by " + orderBy);
            return this;
        }

        public TestConfig addExpected(long... nums) {
            Arrays.stream(nums).forEach(num -> addExpected(String.valueOf(num)));
            return this;
        }
    }
}