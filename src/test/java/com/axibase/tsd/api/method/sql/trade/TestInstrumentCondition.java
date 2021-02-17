package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestInstrumentCondition extends SqlTradeTest {
    public static final String SQL = "select class, symbol from atsd_trade where {where} order by class, symbol";
    private final String TEST_PREFIX = clazz();
    private final String CLASS_1 = TEST_PREFIX + "CLASS1";
    private final String CLASS_2 = TEST_PREFIX + "CLASS11";
    private final String CLASS_3 = TEST_PREFIX + "CLASS3";
    private final String SYMBOL_1 = TEST_PREFIX + "SYMBOL1";
    private final String SYMBOL_2 = TEST_PREFIX + "SYMBOL11";
    private final String SYMBOL_3 = TEST_PREFIX + "SYMBOL3";

    @BeforeClass
    public void prepareData() throws Exception {
        long timestamp = Util.getUnixTime("2020-05-21T10:15:14Z");
        Trade trade1 = trade(timestamp).setClazz(CLASS_1).setSymbol(SYMBOL_1);
        Trade trade2 = trade(timestamp).setClazz(CLASS_2).setSymbol(SYMBOL_2);
        Trade trade3 = trade(timestamp).setClazz(CLASS_3).setSymbol(SYMBOL_3);
        insert(trade1, trade2, trade3);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(SQL);
        assertSqlQueryRows(testConfig.getDescription() + "\n" + sql, testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("exchange = '" + exchange() + "' AND class like '" + TEST_PREFIX + "CLASS%' AND symbol = '" + SYMBOL_2 + "'")
                        .addExpected(CLASS_2, SYMBOL_2),
                test("class like '" + TEST_PREFIX + "CLASS%' AND symbol = '" + SYMBOL_2 + "'")
                        .addExpected(CLASS_2, SYMBOL_2),
                test("exchange = '" + exchange() + "' AND class like '" + TEST_PREFIX + "CLASS%' AND symbol != '" + SYMBOL_2 + "'")
                        .addExpected(CLASS_1, SYMBOL_1)
                        .addExpected(CLASS_3, SYMBOL_3),
                test("class like '" + TEST_PREFIX + "CLASS%' AND symbol != '" + SYMBOL_2 + "'")
                        .addExpected(CLASS_1, SYMBOL_1)
                        .addExpected(CLASS_3, SYMBOL_3),
                test("exchange = '" + exchange() + "' AND symbol = '" + SYMBOL_2 + "'")
                        .addExpected(CLASS_2, SYMBOL_2),
                test("exchange = '" + exchange() + "' AND symbol NOT IN ('" + SYMBOL_1 + "', '" + SYMBOL_3 + "')")
                        .addExpected(CLASS_2, SYMBOL_2),
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
            setVariable("where", description);
        }
    }

}