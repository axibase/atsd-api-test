package com.axibase.tsd.api.method.trade.export

import com.axibase.tsd.api.model.trade.ohlcv.RawTradeRequest
import com.axibase.tsd.api.method.trade.TradeExportMethod
import com.axibase.tsd.api.method.trade.TradeExportMethod.Companion.rawCsv
import com.axibase.tsd.api.method.trade.TradeExportMethod.Companion.rawResponse
import com.axibase.tsd.api.model.financial.Trade
import com.axibase.tsd.api.util.Mocks
import com.axibase.tsd.api.util.TradeSender
import com.axibase.tsd.util.CSVMatcher.eqCsv
import org.apache.http.HttpStatus
import org.junit.Assert.assertThat
import org.testng.annotations.BeforeClass
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals


private val symbol = Mocks.tradeSymbol()
private val clazz = Mocks.tradeClass()
private val exchange = Mocks.tradeExchange()

class TradeExportRawTest {
    private val trades = listOf(
        TestTrade("2020-12-18T07:41:00Z", 3323404531, 2, 2, exchange),
        TestTrade("2020-12-18T10:41:00Z", 3423404531, 5, exchange = exchange),
        TestTrade("2020-12-18T10:41:02.500Z", 3423404532, 6, exchange = exchange),
        TestTrade("2020-12-18T10:41:02.500500Z", 3423404533, 6, 5, exchange),
        TestTrade("2020-12-18T10:41:02.500500Z", 4423404533, 7, 2, Mocks.tradeExchange())
    ).map { it.toTrade() }

    @BeforeClass
    fun setup() {
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
    }

    @Test
    fun `should require parameters`() {
        val resp = rawResponse()
        assertEquals(HttpStatus.SC_BAD_REQUEST, resp.status)
    }

    @Test
    fun `should return bad request for multiple exchange case`() {
        val req = RawTradeRequest(symbol, clazz, "2020-12-18T10:41:00Z", "2020-12-18T10:42:00Z")
        val resp = rawResponse(req)
        assertEquals(HttpStatus.SC_BAD_REQUEST, resp.status)
        val errorMessage = resp.readEntity(TradeExportMethod.ErrorMessage::class.java)
        assertEquals(
            "Multiple instruments found for the key: Instrument(symbol=$symbol, clas=$clazz, exchange=null)",
            errorMessage.error
        )
    }

    @Test
    fun `should return not found status for non-existing instrument`() {
        val req = RawTradeRequest(
            "non", "existing",
            "2020-12-18T10:41:00Z", "2020-12-18T10:42:00Z"
        )
        assertEquals(HttpStatus.SC_NOT_FOUND, rawResponse(req).status)
    }

    @Test
    fun `should return success code for existing instrument`() {
        val req = RawTradeRequest(
            symbol, clazz, "2020-12-18T10:41:00Z", "2020-12-18T10:42:00Z",
            exchange = exchange
        )
        val resp = rawResponse(req)
        assertEquals(HttpStatus.SC_OK, resp.status)
    }

    @Test
    fun `should return success code with specified exchange`() {
        val req = RawTradeRequest(
            symbol, clazz, "2020-12-18T10:41:00Z", "2020-12-18T10:42:00Z",
            exchange = exchange
        )
        val resp = rawResponse(req)
        assertEquals(HttpStatus.SC_OK, resp.status)
    }

    @Test
    fun `should filter trades via datetime filter`() {
        val req = RawTradeRequest(
            symbol, clazz, "2020-12-18T10:41:00Z", "2020-12-18T10:41:00.001Z",
            exchange = exchange
        )
        val csv = rawCsv(req)
        assertEquals(
            """
            datetime,trade_num,side,quantity,price,order_num,session
            2020-12-18T10:41:00.000000Z,3423404531,,1,5,,

        """.csv(), csv
        )
    }

    @Test
    fun `should apply timezone for non ISO datetime with timezone`() {
        val req = RawTradeRequest(
            symbol, clazz, "2020-12-18 10:41:00", "2020-12-18 10:41:00.001",
            timeZone = "Europe/Moscow",
            exchange = exchange
        )
        val csv = rawCsv(req).csv()
        val expectedCsv = """
            datetime,trade_num,side,quantity,price,order_num,session
            2020-12-18T07:41:00.000000Z,3323404531,,2,2,,
        """.csv()
        assertThat(csv, eqCsv(expectedCsv))
    }

    @Test
    fun `should select trades with microsecond gap`() {
        val req = RawTradeRequest(
            symbol, clazz, "2020-12-18T07:41:00.000000Z", "2020-12-18T07:41:00.000001Z",
            timeZone = "Europe/Moscow",
            exchange = exchange
        )
        val csv = rawCsv(req).csv()
        val expectedCsv = """
            datetime,trade_num,side,quantity,price,order_num,session
            2020-12-18T07:41:00.000000Z,3323404531,,2,2,,
        """.csv()
        assertThat(csv, eqCsv(expectedCsv))
    }


    private data class TestTrade(
        val isoTime: String, val tradeNum: Long, val price: Number, val quantity: Long = 1,
        val exchange: String? = null
    ) {
        fun toTrade(): Trade {
            return Trade(exchange, clazz, symbol, tradeNum, isoTime, BigDecimal(price.toString()), quantity)
        }
    }

}

fun String.csv(): String {
    return this.trimIndent()
}
