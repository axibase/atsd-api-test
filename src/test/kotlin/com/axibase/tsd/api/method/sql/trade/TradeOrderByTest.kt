package com.axibase.tsd.api.method.sql.trade

import com.axibase.tsd.api.model.financial.Trade
import com.axibase.tsd.api.util.TestUtil
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal

class TradeOrderByTest : SqlTradeTest() {

    @BeforeClass
    @Throws(Exception::class)
    fun prepareData() {
        val trades = listOf<Trade>(
                fromISOString("2020-03-22T10:00:01Z").setNumber(800).setPrice(BigDecimal("1000")).setSide(Trade.Side.BUY).setSession(Trade.Session.N),
                fromISOString("2020-03-22T10:00:05Z").setNumber(700).setPrice(BigDecimal("900")).setSide(Trade.Side.SELL).setSession(Trade.Session.O),
                fromISOString("2020-03-22T11:00:07Z").setNumber(1000).setPrice(BigDecimal("800")).setSide(Trade.Side.BUY).setSession(Trade.Session.N),
                fromISOString("2020-03-22T11:00:09Z").setNumber(900).setPrice(BigDecimal("700")).setSide(Trade.Side.SELL).setSession(Trade.Session.O),
                fromISOString("2020-03-22T10:00:06Z").setNumber(850).setPrice(BigDecimal("850")).setSide(Trade.Side.SELL).setSession(Trade.Session.O).setSymbol(symbolTwo()),
                fromISOString("2020-03-22T11:00:08Z").setNumber(950).setPrice(BigDecimal("950")).setSide(Trade.Side.BUY).setSession(Trade.Session.N).setSymbol(symbolTwo()),
        )
        insert(trades)
    }

    @Test(dataProvider = "testData")
    fun testOrderBy(config: TestConfig) {
        assertSqlQueryRows(config.sql, config.expected, config.sql)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        val data = arrayOf(
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by time").addExpectedRows("800", "700", "1000", "900"),
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by time desc").addExpectedRows("900", "1000", "700", "800"),
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by trade_num").addExpectedRows("700", "800", "900", "1000"),
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by trade_num desc").addExpectedRows("1000", "900", "800", "700"),
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by price").addExpectedRows("900", "1000", "700", "800"),
                test("select trade_num from atsd_trade where ${instrumentCondition()} order by price desc").addExpectedRows("800", "700", "1000", "900"),

                test("select trade_num from atsd_trade where ${classCondition()} order by time").addExpectedRows("800", "700", "850", "1000", "950", "900"),
                test("select trade_num from atsd_trade where ${classCondition()} order by time desc").addExpectedRows("900", "950", "1000", "850", "700", "800"),
                test("select trade_num from atsd_trade where ${classCondition()} order by trade_num").addExpectedRows("700", "800", "850", "900", "950", "1000"),
                test("select trade_num from atsd_trade where ${classCondition()} order by trade_num desc").addExpectedRows("1000", "950", "900", "850", "800", "700"),
                test("select trade_num from atsd_trade where ${classCondition()} order by price").addExpectedRows("900", "1000", "850", "700", "950", "800"),
                test("select trade_num from atsd_trade where ${classCondition()} order by price desc").addExpectedRows("800", "950", "700", "850", "1000", "900"),

                test("select datetime, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, period(5 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "800.0"),
                test("select datetime, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, period(5 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "800.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1000.0"),

                test("select datetime, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, period(5 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "700.0"),
                test("select datetime, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, period(5 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "700.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1000.0"),

                test("select datetime, count(*) from atsd_trade where ${instrumentCondition()} group by exchange, class, period(5 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "1")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "2"),
                test("select datetime, count(*) from atsd_trade where ${instrumentCondition()} group by exchange, class, period(5 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:05.000000Z", "2")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "1")
                        .addExpectedRow("2020-03-22T10:00:00.000000Z", "1"),

                test("select datetime, open() from atsd_trade where ${classCondition()} group by exchange, class, symbol, period(1 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T10:00:06.000000Z", "850.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "800.0")
                        .addExpectedRow("2020-03-22T11:00:08.000000Z", "950.0")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "700.0"),
                test("select datetime, open() from atsd_trade where ${classCondition()} group by exchange, class, symbol, period(1 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "700.0")
                        .addExpectedRow("2020-03-22T11:00:08.000000Z", "950.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "800.0")
                        .addExpectedRow("2020-03-22T10:00:06.000000Z", "850.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "900.0")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "1000.0"),
                test("select datetime, side, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, side, period(1 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "B", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "S", "900.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "B", "800.0")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "S", "700.0"),
                test("select datetime, side, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, side, period(1 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "S", "700.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "B", "800.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "S", "900.0")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "B", "1000.0"),
                test("select datetime, side, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, side, period(1 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "B", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "S", "900.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "B", "800.0")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "S", "700.0"),
                test("select datetime, side, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, side, period(1 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "S", "700.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "B", "800.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "S", "900.0")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "B", "1000.0"),

                test("select datetime, session, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, session, period(1 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "N", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "O", "900.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "N", "800.0")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "O", "700.0"),
                test("select datetime, session, open() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, session, period(1 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "O", "700.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "N", "800.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "O", "900.0")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "N", "1000.0"),
                test("select datetime, session, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, session, period(1 second) order by time")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "N", "1000.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "O", "900.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "N", "800.0")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "O", "700.0"),
                test("select datetime, session, close() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol, session, period(1 second) order by time desc")
                        .addExpectedRow("2020-03-22T11:00:09.000000Z", "O", "700.0")
                        .addExpectedRow("2020-03-22T11:00:07.000000Z", "N", "800.0")
                        .addExpectedRow("2020-03-22T10:00:05.000000Z", "O", "900.0")
                        .addExpectedRow("2020-03-22T10:00:01.000000Z", "N", "1000.0"),
        )
        return TestUtil.convertTo2DimArray(data);
    }

    private fun test(sql: String): TestConfig {
        return TestConfig(sql)
    }

    class TestConfig(val sql: String) {
        val expected = mutableListOf<List<String>>();

        fun addExpectedRow(vararg row: String): TestConfig {
            expected.add(row.asList())
            return this;
        }

        fun addExpectedRows(vararg rows: String): TestConfig {
            for (row in rows) {
                expected.add(listOf(row))
            }
            return this;
        }
    }
}

