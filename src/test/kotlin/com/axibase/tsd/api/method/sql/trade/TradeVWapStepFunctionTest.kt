package com.axibase.tsd.api.method.sql.trade

import com.axibase.tsd.api.Checker
import com.axibase.tsd.api.method.checks.EntityCheck
import com.axibase.tsd.api.method.entity.EntityMethod
import com.axibase.tsd.api.model.entity.Entity
import com.axibase.tsd.api.model.financial.Trade
import com.axibase.tsd.api.util.TestUtil
import com.axibase.tsd.api.util.Util
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.math.BigDecimal

class TradeVWapStepFunctionTest : SqlTradeTest() {

    @BeforeClass
    @Throws(Exception::class)
    fun prepareData() {
        val entity = Entity()
        entity.name = entity()
        val entityTwo = Entity()
        entityTwo.name = entityTwo()
        val entityThree = Entity()
        entityThree.name = entityThree()
        val trades: MutableList<Trade> = ArrayList()
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setPrice(BigDecimal("126.99")).setQuantity(22330))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:09:00Z"), BigDecimal("127.36"), 22330))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:49:00Z"), BigDecimal("127.02"), 22339))
        trades.add(fromISOString("2020-03-22T10:55:00.654321Z").setPrice(BigDecimal("127.28")).setQuantity(22330))
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setPrice(BigDecimal("126.99")).setQuantity(22330).setSymbol(symbolTwo()))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:09:00Z"), BigDecimal("127.36"), 22330).setSymbol(symbolTwo()))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:49:00Z"), BigDecimal("127.02"), 22339).setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T10:55:00.654321Z").setPrice(BigDecimal("127.28")).setQuantity(22330).setSymbol(symbolTwo()))
        trades.add(fromISOString("2020-03-22T10:01:00.123456Z").setPrice(BigDecimal("126.99")).setQuantity(22330).setSymbol(symbolThree()))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:09:00Z"), BigDecimal("127.36"), 22330).setSymbol(symbolThree()))
        trades.add(trade(Util.getUnixTime("2020-03-22T10:49:00Z"), BigDecimal("127.02"), 22339).setSymbol(symbolThree()))
        trades.add(fromISOString("2020-03-22T10:55:00.654321Z").setPrice(BigDecimal("127.28")).setQuantity(22330).setSymbol(symbolThree()))
        insert(trades)
        Checker.check(EntityCheck(entity))
        Checker.check(EntityCheck(entityTwo))
        Checker.check(EntityCheck(entityThree))
        entity.tags = TestUtil.createTags("step", "0.5", "scale", "3")
        entityTwo.tags = TestUtil.createTags("scale", "3")
        EntityMethod.updateEntity(entity)
        EntityMethod.updateEntity(entityTwo)
    }

    @Test
    fun test() {
        val sql = "select vwap(), vwap_step(), ROUND(vwap()/entity.tags.\"step\", 0)*entity.tags.\"step\" AS vwap_step from atsd_trade " +
                "where ${instrumentCondition()} " +
                "group by exchange, class, symbol"
        val expected = arrayOf(arrayOf("127.16248564296029", "127.0", "127.0"))
        assertSqlQueryRows(expected, sql)
    }

    @Test
    fun testScale() {
        val sql = "select vwap(), vwap_step() from atsd_trade " +
                "where ${instrumentTwoCondition()} " +
                "group by exchange, class, symbol"
        val expected = arrayOf(arrayOf("127.16248564296029", "127.162"))
        assertSqlQueryRows(expected, sql)
    }

    @Test
    fun testNoTags() {
        val sql = "select vwap(), vwap_step() from atsd_trade " +
                "where ${instrumentThreeCondition()} " +
                "group by exchange, class, symbol"
        val expected = arrayOf(arrayOf("127.16248564296029", "127.16248564296029"))
        assertSqlQueryRows(expected, sql)
    }
}