package com.axibase.tsd.api.method.trade.export

import com.axibase.tsd.api.method.entity.EntityMethod
import com.axibase.tsd.api.method.trade.OhlcvStatistic
import com.axibase.tsd.api.method.trade.OhlcvStatistic.*
import com.axibase.tsd.api.method.trade.OhlcvTradeRequest
import com.axibase.tsd.api.method.trade.TradeExportMethod.Companion.ohlcvCsv
import com.axibase.tsd.api.model.Period
import com.axibase.tsd.api.model.financial.Trade
import com.axibase.tsd.api.util.Mocks
import com.axibase.tsd.api.util.TestUtil
import com.axibase.tsd.api.util.TradeSender
import com.axibase.tsd.api.util.Util
import com.axibase.tsd.util.CSVMatcher.eqCsv
import org.junit.Assert.assertThat
import org.junit.internal.matchers.StringContains.containsString
import org.testng.Assert
import org.testng.Assert.assertEquals
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.streams.toList
import kotlin.test.fail


private val exchange = Mocks.tradeExchange()
private val clazz = Mocks.tradeClass()
private val symbol = Mocks.tradeSymbol()

class TradeExportOhlcvTest {


    @BeforeClass
    fun insertTrades() {
        val trades = TradeExportOhlcvTest::class.java.classLoader
            .getResourceAsStream("csv/trades.csv")
            .bufferedReader().lines()
            .map {
                val values = it.split(",").toTypedArray()
                trade(Mocks.tradeNum(), values[0], Trade.Side.valueOf(values[1]), values[2])
            }.toList()
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
        EntityMethod.updateEntity("${symbol}_[$clazz]", mapOf("tags" to mapOf("lot" to "10")))
    }

    @DataProvider
    fun successCases(): Array<Array<Any>> {
        val date1 = "2020-11-25T14:00:00Z"
        val date2 = "2020-11-25T14:01:00Z"
        val date3 = "2020-11-25T14:02:00Z"
        val endDate = "2020-12-31T00:00:00Z"
        val period = Period(1, com.axibase.tsd.api.model.TimeUnit.MINUTE)
        val tz = "Europe/Moscow"

        /* OHLCV for the minute 1. */
        val open1 = BigDecimal.valueOf(23)
        val high1 = BigDecimal.valueOf(999)
        val low1 = BigDecimal("0.001")
        val close1 = BigDecimal.valueOf(71)
        val volume1 = 41
        val line1 = ResponseLine(Util.getUnixTime(date1), open1, high1, low1, close1, volume1)

        /* OHLCV for the minute 2. */
        val open2 = BigDecimal.valueOf(70)
        val high2 = BigDecimal.valueOf(9999)
        val low2 = BigDecimal("0.01")
        val close2 = BigDecimal.valueOf(1)
        val volume2 = 70
        val line2 = ResponseLine(Util.getUnixTime(date2), open2, high2, low2, close2, volume2)

        /* OHLCV for both minutes. */
        val volume = volume1 + volume2
        val line = ResponseLine(Util.getUnixTime(date1), open1, high2, low1, close2, volume)
        val testCases = arrayOf(
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, date3, tz, exchange = exchange, period = period),
                listOf(line1, line2)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, endDate, tz, exchange = exchange, period = period),
                listOf(line1, line2)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, endDate, exchange = "", timeZone = "", period = period),
                listOf(line1, line2)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, date2, exchange = exchange, period = period, timeZone = tz),
                listOf(line1)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date2, date3, period = period, timeZone = tz, exchange = exchange),
                listOf(line2)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, date3, tz, exchange = exchange),
                listOf(line)
            ),
            SuccessCase(
                OhlcvTradeRequest(symbol, clazz, date1, endDate), listOf(line)
            )
        )
        return TestUtil.convertTo2DimArray(testCases)
    }

    @DataProvider
    fun errorCases(): Array<Array<Any>> {
        val period = Period(1, com.axibase.tsd.api.model.TimeUnit.MINUTE)
        val tz = "Europe/Moscow"
        val date1 = "2020-11-25T14:00:00Z"
        val date3 = "2020-11-25T14:02:00Z"
        val cases = arrayOf(
            ErrorCase(
                OhlcvTradeRequest(null, clazz, date1, date3, exchange = exchange, period = period, timeZone = tz)
            ),
            ErrorCase(
                OhlcvTradeRequest(
                    symbol,
                    null,
                    date1,
                    date3,
                    period = period,
                    timeZone = tz,
                    exchange = exchange
                ), " 'class' "
            ),
            ErrorCase(
                OhlcvTradeRequest(symbol, clazz, null, date3, exchange = exchange, period = period, timeZone = tz),
                " 'startDate' "
            )
        )
        return TestUtil.convertTo2DimArray(cases)
    }


    @Test(dataProvider = "successCases")
    fun testSuccessCase(testCase: SuccessCase) {
        val csv = ohlcvCsv(testCase.request)
        val actualLines = csv.trim().split("(\\r)?\\n".toRegex());
        val expectedLines = testCase.responseLines;
        assertEquals(actualLines.size, expectedLines.size + 1, "Unexpected lines count in response.")
        val header = "datetime,open,high,low,close,volume"
        assertEquals(actualLines[0], header, "Unexpected header line in response.")
        for (i in expectedLines.indices) {
            checkLine(actualLines[i + 1], expectedLines[i])
        }
    }


    @Test(dataProvider = "errorCases")
    fun testErrorCase(case: ErrorCase) {
        try {
            ohlcvCsv(case.req)
            fail("Exception should be thrown for request: ${case.req}")
        } catch (e: IllegalStateException) {
            val explanation = String.format(
                "Actual error message '%s' does not contains expected sub-string '%s'.",
                e.message, case.errorSubstring
            )
            Assert.assertNotNull(e.message)
            Assert.assertTrue(e.message!!.contains(case.errorSubstring), explanation)
        }
    }

    @Test
    fun `should return 400 for instrument where lot size is not defined`() {
        val trade = Trade(
            Mocks.tradeExchange(), Mocks.tradeClass(), Mocks.tradeSymbol(), Mocks.tradeNum(),
            "2020-11-25T14:00:00Z", "1".toBigDecimal(), 1
        )
        TradeSender.send(trade).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
        val req = OhlcvTradeRequest(
            trade.symbol, trade.clazz, "2020-11-25T14:00:00Z",
            "2020-11-25T15:00:00Z", statistics = listOf(AMOUNT)
        )
        try {
            ohlcvCsv(req)
            fail("Should return bad request status")
        } catch (e: IllegalStateException) {
            assertThat(e.message, containsString("Lot tag is not defined for entity"))
        }
    }

    @DataProvider
    fun statisticCases(): Array<Array<Any>> = TestUtil.convertTo2DimArray(
        listOf(
            StatisticCase(
                listOf(AMOUNT), """
            datetime,amount
            2020-11-25T14:00:00.000Z,168300.110
        """.csv()
            ),
            StatisticCase(
                listOf(COUNT, HIGH, LOW), """
            datetime,count,high,low
            2020-11-25T14:00:00.000Z,111,9999,0.001
        """.csv()
            ),
            StatisticCase(
                emptyList(),
                """
            datetime,open,high,low,close,volume
            2020-11-25T14:00:00.000Z,23,9999,0.001,1,111
        """.csv(),
            ),
            StatisticCase(
                OhlcvStatistic.values().toList(),
                """
            datetime,open,high,low,close,volume,count,vwap,amount
            2020-11-25T14:00:00.000Z,23,9999,0.001,1,111,111,151.6217207207207207,168300.110
        """.csv(),
            ),
            StatisticCase(
                listOf(COUNT, COUNT, HIGH, LOW), """
            datetime,count,high,low
            2020-11-25T14:00:00.000Z,111,9999,0.001
        """.csv()
            ),
            StatisticCase(
                listOf(COUNT, COUNT, HIGH, LOW), """
            datetime,count,high,low
            2020-11-25T14:00:00.000Z,17,999,23
            2020-11-25T14:00:40.000Z,69,9999,0.001
            2020-11-25T14:01:20.000Z,25,25,1
        """.csv(),
                period = Period(40, com.axibase.tsd.api.model.TimeUnit.SECOND)
            ),
        )
    )


    @Test(dataProvider = "statisticCases")
    fun `should return right values for custom statistics`(case: StatisticCase) {
        val req = case.req()
        val csv = ohlcvCsv(req).csv()
        assertThat("Should return same csv for request: $req", csv, eqCsv(case.csv))
    }


    private fun checkLine(actualLine: String, expectedLine: ResponseLine) {
        val actualFields = actualLine.split(",").toTypedArray()
        assertEquals(actualFields.size, 6, "Unexpected count of fields in line: $actualLine")
        assertEquals(Util.getUnixTime(actualFields[0]), expectedLine.dateMillis, "Unexpected timestamp.")
        assertEquals(BigDecimal(actualFields[1]), expectedLine.open, "Unexpected OPEN value.")
        assertEquals(BigDecimal(actualFields[2]), expectedLine.high, "Unexpected HIGH value.")
        assertEquals(BigDecimal(actualFields[3]), expectedLine.low, "Unexpected LOW value.")
        assertEquals(BigDecimal(actualFields[4]), expectedLine.close, "Unexpected CLOSE value.")
        assertEquals(actualFields[5].toInt(), expectedLine.volume, "Unexpected VOLUME value.")
    }

    private fun trade(tradeNumber: Long, date: String, side: Trade.Side, price: String): Trade {
        val trade = Trade(exchange, clazz, symbol, tradeNumber, Util.getUnixTime(date), BigDecimal(price), 1)
        trade.side = side
        return trade
    }

    data class ResponseLine(
        val dateMillis: Long = 0,
        val open: BigDecimal? = null,
        val high: BigDecimal? = null,
        val low: BigDecimal? = null,
        val close: BigDecimal? = null,
        val volume: Int? = 0
    )

    data class SuccessCase(
        /* Request parameters. */
        val request: OhlcvTradeRequest,

        /* Response paramters */
        val responseLines: List<ResponseLine> // response lines in case there is no error:
    )

    data class StatisticCase(
        val statistics: List<OhlcvStatistic>,
        val csv: String,
        val period: Period? = null
    ) {
        fun req(): OhlcvTradeRequest = OhlcvTradeRequest(
            symbol, clazz, "2020-11-25T14:00:00Z", "2020-11-25T15:00:00Z",
            period = period, statistics = statistics
        )
    }

    data class ErrorCase(val req: OhlcvTradeRequest, val errorSubstring: String = "")
}

