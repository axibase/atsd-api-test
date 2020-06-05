package com.axibase.tsd.api.method.sql.trade

import com.axibase.tsd.api.util.TradeSender
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.concurrent.TimeUnit

class TradeAggregationWithLimitTest : SqlTradeTest() {

    @BeforeClass
    @Throws(Exception::class)
    fun prepareData() {
        val trades = fromISO(
                "2020-03-22T10:00:01Z",
                "2020-03-22T10:00:09Z",
                "2020-03-22T10:00:49Z",
                "2020-03-22T10:00:55Z",
                "2020-03-22T11:01:05Z",
                "2020-03-22T11:01:14Z",
                "2020-03-22T11:01:29Z",
                "2020-03-22T11:01:49Z",
                "2020-03-22T11:01:50Z")
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
    }

    @Test(dataProvider = "testData")
    fun test(config: Config) {
        val sql = "select count(*) from atsd_trade where ${instrumentCondition()} " +
                "group by exchange, class, symbol, period(${config.period}) limit 1"
        val expected = arrayOf(arrayOf(config.expected.toString()))
        assertSqlQueryRows(config.description, expected, sql)
    }

    @DataProvider
    fun testData(): Array<Array<Config>> {
        return arrayOf(
                arrayOf(Config("Test period in seconds", "10 second", 2)),
                arrayOf(Config("Test period in hours", "1 hour", 4))
        )
    }

    data class Config(val description: String, val period: String, val expected: Int)
}