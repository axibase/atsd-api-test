package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TradeDailyTimeRangeMicrosTest extends SqlTradeTest {
    private static final String QUERY_TEMPLATE =
            "SELECT datetime FROM atsd_trade " +
                    "WHERE {instrument} AND {timeRange} " +
                    "WITH TIMEZONE = 'UTC' ";

    @BeforeClass
    public void prepareData() throws Exception {
        String[] timestamps = {
                "2020-05-19T10:00:00.000000Z",
                "2020-05-19T10:00:00.000001Z",
                "2020-05-19T10:59:59.999999Z",
                "2020-05-19T11:00:00.000000Z",
                "2020-05-19T11:00:00.000001Z",

        };
        insert(fromISO(timestamps));
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) throws Exception {
        String sql = testConfig.composeQuery(QUERY_TEMPLATE);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        TestConfig[] data = {
                test("HH:mm end time exclusive")
                        .timeRange("date_format(time, 'HH:mm') BETWEEN '10:00' AND '11:00' EXCL")
                        .addExpected("2020-05-19T10:00:00.000000Z")
                        .addExpected("2020-05-19T10:00:00.000001Z")
                        .addExpected("2020-05-19T10:59:59.999999Z")
                ,
                test("HH:mm end time inclusive")
                        .timeRange("date_format(time, 'HH:mm') BETWEEN '10:00' AND '11:00'")
                        .addExpected("2020-05-19T10:00:00.000000Z")
                        .addExpected("2020-05-19T10:00:00.000001Z")
                        .addExpected("2020-05-19T10:59:59.999999Z")
                        .addExpected("2020-05-19T11:00:00.000000Z")
                        .addExpected("2020-05-19T11:00:00.000001Z")
                ,
                test("HH:mm:ss.SSS end time exclusive")
                        .timeRange("date_format(time, 'HH:mm:ss.SSS') BETWEEN '10:00:00.000' AND '11:00:00.000' EXCL")
                        .addExpected("2020-05-19T10:00:00.000000Z")
                        .addExpected("2020-05-19T10:00:00.000001Z")
                        .addExpected("2020-05-19T10:59:59.999999Z")
        };
        return TestUtil.convertTo2DimArray(data);
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {

        public TestConfig(String description) {
            super(description);
        }

        public TestConfig timeRange(String timeRange) {
            setVariable("timeRange", timeRange);
            return this;
        }

    }
}