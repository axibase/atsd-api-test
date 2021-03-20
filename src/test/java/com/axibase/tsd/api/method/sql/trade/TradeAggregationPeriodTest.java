package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class TradeAggregationPeriodTest extends SqlTradeTest {
    public static final String QUERY = "select" +
            " date_format(time, 'yyyy-MM-dd HH:mm:ss') AS dt," +
            " date_format(time, 'iso'), " +
            " {aggregation} from atsd_trade " +
            " where {instrument}  and time between '{startDate}' and '{endDate}' EXCL " +
            " group by exchange, class, symbol, period(1 day) " +
            " WITH TIMEZONE = '{timeZone}' " +
            " order by time";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = fromISO(
                "2020-04-03T01:00:00Z",
                "2020-04-04T01:00:00Z",
                "2020-04-06T01:00:00Z",
                "2020-04-07T01:00:00Z",

                "2021-03-11T14:00:00Z",
                "2021-03-12T14:00:00Z",
                "2021-03-16T14:00:00Z",
                "2021-03-17T14:00:00Z",

                "2020-10-02T01:00:00Z",
                "2020-10-03T01:00:00Z",
                "2020-10-05T01:00:00Z",
                "2020-10-06T01:00:00Z",

                "2020-10-29T14:00:00Z",
                "2020-10-30T14:00:00Z",
                "2020-11-01T14:00:00Z",
                "2020-11-02T14:00:00Z"
        );
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
                test("West of GMT (USA) summer time, open()")
                        .aggregation("open()")
                        .startDate("2021-03-11")
                        .endDate("2021-03-17")
                        .timeZone("US/Eastern")
                        .addExpected("2021-03-11 00:00:00", "2021-03-11T00:00:00.000-05:00", "1")
                        .addExpected("2021-03-12 00:00:00", "2021-03-12T00:00:00.000-05:00", "1")
                        .addExpected("2021-03-16 00:00:00", "2021-03-16T00:00:00.000-04:00", "1"),

                test("West of GMT (USA) summer time, close()")
                        .aggregation("close()")
                        .startDate("2021-03-11")
                        .endDate("2021-03-17")
                        .timeZone("US/Eastern")
                        .addExpected("2021-03-11 00:00:00", "2021-03-11T00:00:00.000-05:00", "1")
                        .addExpected("2021-03-12 00:00:00", "2021-03-12T00:00:00.000-05:00", "1")
                        .addExpected("2021-03-16 00:00:00", "2021-03-16T00:00:00.000-04:00", "1"),

                test("West of GMT (USA) winter time, open()")
                        .aggregation("open()")
                        .startDate("2020-10-29")
                        .endDate("2020-11-03")
                        .timeZone("US/Eastern")
                        .addExpected("2020-10-29 00:00:00", "2020-10-29T00:00:00.000-04:00", "1")
                        .addExpected("2020-10-30 00:00:00", "2020-10-30T00:00:00.000-04:00", "1")
                        .addExpected("2020-11-01 00:00:00", "2020-11-01T00:00:00.000-04:00", "1")
                        .addExpected("2020-11-02 00:00:00", "2020-11-02T00:00:00.000-05:00", "1"),

                test("West of GMT (USA) winter time, close()")
                        .aggregation("close()")
                        .startDate("2020-10-29")
                        .endDate("2020-11-03")
                        .timeZone("US/Eastern")
                        .addExpected("2020-10-29 00:00:00", "2020-10-29T00:00:00.000-04:00", "1")
                        .addExpected("2020-10-30 00:00:00", "2020-10-30T00:00:00.000-04:00", "1")
                        .addExpected("2020-11-01 00:00:00", "2020-11-01T00:00:00.000-04:00", "1")
                        .addExpected("2020-11-02 00:00:00", "2020-11-02T00:00:00.000-05:00", "1"),

                test("East of GMT (Australia) summer time, open()")
                        .aggregation("open()")
                        .startDate("2020-04-03")
                        .endDate("2020-04-08")
                        .timeZone("Australia/Sydney")
                        .addExpected("2020-04-03 00:00:00", "2020-04-03T00:00:00.000+11:00", "1")
                        .addExpected("2020-04-04 00:00:00", "2020-04-04T00:00:00.000+11:00", "1")
                        .addExpected("2020-04-06 00:00:00", "2020-04-06T00:00:00.000+10:00", "1")
                        .addExpected("2020-04-07 00:00:00", "2020-04-07T00:00:00.000+10:00", "1"),

                test("East of GMT (Australia) summer time, close()")
                        .aggregation("close()")
                        .startDate("2020-04-03")
                        .endDate("2020-04-08")
                        .timeZone("Australia/Sydney")
                        .addExpected("2020-04-03 00:00:00", "2020-04-03T00:00:00.000+11:00", "1")
                        .addExpected("2020-04-04 00:00:00", "2020-04-04T00:00:00.000+11:00", "1")
                        .addExpected("2020-04-06 00:00:00", "2020-04-06T00:00:00.000+10:00", "1")
                        .addExpected("2020-04-07 00:00:00", "2020-04-07T00:00:00.000+10:00", "1"),

                test("East of GMT (Australia) winter time, open()")
                        .aggregation("open()")
                        .startDate("2020-10-02")
                        .endDate("2020-10-07")
                        .timeZone("Australia/Sydney")
                        .addExpected("2020-10-02 00:00:00", "2020-10-02T00:00:00.000+10:00", "1")
                        .addExpected("2020-10-03 00:00:00", "2020-10-03T00:00:00.000+10:00", "1")
                        .addExpected("2020-10-05 00:00:00", "2020-10-05T00:00:00.000+11:00", "1")
                        .addExpected("2020-10-06 00:00:00", "2020-10-06T00:00:00.000+11:00", "1"),

                test("East of GMT (Australia) winter time, close()")
                        .aggregation("close()")
                        .startDate("2020-10-02")
                        .endDate("2020-10-07")
                        .timeZone("Australia/Sydney")
                        .addExpected("2020-10-02 00:00:00", "2020-10-02T00:00:00.000+10:00", "1")
                        .addExpected("2020-10-03 00:00:00", "2020-10-03T00:00:00.000+10:00", "1")
                        .addExpected("2020-10-05 00:00:00", "2020-10-05T00:00:00.000+11:00", "1")
                        .addExpected("2020-10-06 00:00:00", "2020-10-06T00:00:00.000+11:00", "1"),
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

        public TestConfig aggregation(String aggregation) {
            setVariable("aggregation", aggregation);
            return this;
        }

        public TestConfig startDate(String startDate) {
            setVariable("startDate", startDate);
            return this;
        }

        public TestConfig endDate(String endDate) {
            setVariable("endDate", endDate);
            return this;
        }

        public TestConfig timeZone(String timeZone) {
            setVariable("timeZone", timeZone);
            return this;
        }
    }
}