package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TestUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.axibase.tsd.api.util.Util.getUnixTime;

public class TradeAggregationMultiInstrumentTest extends SqlTradeTest {
    private static final String QUERY = "select symbol, {fields} from atsd_trade " +
            "where {classCondition} " +
            "group by exchange, class, symbol {period} " +
            "WITH TIMEZONE = 'UTC' " +
            "order by symbol " +
            "OPTION(PARALLEL_SCAN_THRESHOLD {threshold})";

    @BeforeClass
    public void prepareData() throws Exception {
        List<Trade> trades = new ArrayList<>();
        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z"), new BigDecimal("1"), 1));
        trades.add(trade(getUnixTime("2020-03-22T12:00:01Z"), new BigDecimal("2"), 2));
        trades.add(trade(getUnixTime("2020-03-22T12:06:01Z"), new BigDecimal("3"), 3));

        trades.add(trade(getUnixTime("2020-03-22T10:08:01Z"), new BigDecimal("10"), 10).setSymbol(symbolTwo()));
        trades.add(trade(getUnixTime("2020-03-22T14:00:01Z"), new BigDecimal("20"), 20).setSymbol(symbolTwo()));
        trades.add(trade(getUnixTime("2020-03-22T14:30:01Z"), new BigDecimal("30"), 30).setSymbol(symbolTwo()));

        trades.add(trade(getUnixTime("2020-03-22T10:00:01Z"), new BigDecimal("100"), 100).setSymbol(symbolThree()));
        trades.add(trade(getUnixTime("2020-03-22T12:00:01Z"), new BigDecimal("200"), 200).setSymbol(symbolThree()));
        trades.add(trade(getUnixTime("2020-03-22T12:06:01Z"), new BigDecimal("300"), 300).setSymbol(symbolThree()));

        insert(trades);
    }

    @Test(dataProvider = "testData")
    public void test(TestConfig testConfig) {
        String sql = testConfig.composeQuery(QUERY);
        assertSqlQueryRows(testConfig.getDescription(), testConfig.getExpected(), sql);
    }

    @DataProvider
    public Object[][] testData() {
        Collection<TestConfig> parallelScans = testDataHelper();
        Collection<TestConfig> singleScans = testDataHelper();
        Collection<TestConfig> result = new ArrayList<>(parallelScans);
        singleScans.forEach(c -> c.threshold(2));
        result.addAll(singleScans);
        return TestUtil.convertTo2DimArray(result);
    }

    private Collection<TestConfig> testDataHelper() {
        return Arrays.asList(
                test("Count and sum(quantity) without period")
                        .fields("count(*), sum(quantity)")
                        .addExpected(symbol(), "3", "6")
                        .addExpected(symbolTwo(), "3", "60")
                        .addExpected(symbolThree(), "3", "600"),
                test("Close price without period")
                        .fields("close()")
                        .addExpected(symbol(), "3")
                        .addExpected(symbolTwo(), "30")
                        .addExpected(symbolThree(), "300"),
                test("Count and sum(quantity) period 1 hour")
                        .period(1, "hour")
                        .fields("datetime, count(*), sum(quantity)")
                        .addExpected(symbol(), "2020-03-22T10:00:00.000000Z", "1", "1")
                        .addExpected(symbol(), "2020-03-22T12:00:00.000000Z", "2", "5")
                        .addExpected(symbolTwo(), "2020-03-22T10:00:00.000000Z", "1", "10")
                        .addExpected(symbolTwo(), "2020-03-22T14:00:00.000000Z", "2", "50")
                        .addExpected(symbolThree(), "2020-03-22T10:00:00.000000Z", "1", "100")
                        .addExpected(symbolThree(), "2020-03-22T12:00:00.000000Z", "2", "500"),
                test("Close price period 1 hour")
                        .period(1, "hour")
                        .fields("datetime, close()")
                        .addExpected(symbol(), "2020-03-22T10:00:00.000000Z", "1")
                        .addExpected(symbol(), "2020-03-22T12:00:00.000000Z", "3")
                        .addExpected(symbolTwo(), "2020-03-22T10:00:00.000000Z", "10")
                        .addExpected(symbolTwo(), "2020-03-22T14:00:00.000000Z", "30")
                        .addExpected(symbolThree(), "2020-03-22T10:00:00.000000Z", "100")
                        .addExpected(symbolThree(), "2020-03-22T12:00:00.000000Z", "300")
        );
    }

    private TestConfig test(String description) {
        return new TestConfig(description);
    }

    private class TestConfig extends TradeTestConfig<TestConfig> {
        private int threshold;

        public TestConfig(String description) {
            super(description);
            setVariable("period", "");
            threshold(256);
            setVariable("classCondition", classCondition());
        }

        private TestConfig period(int count, String unit) {
            setVariable("period", String.format(", period(%d %s)", count, unit));
            return this;
        }

        private TestConfig threshold(int threshold) {
            this.threshold = threshold;
            setVariable("threshold", String.valueOf(threshold));
            return this;
        }

        @Override
        public String getDescription() {
            return super.getDescription() + " threshold: " + threshold;
        }
    }
}