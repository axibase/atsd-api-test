package com.axibase.tsd.api.model.trade.ohlcv

import com.axibase.tsd.api.model.Period
import java.math.BigDecimal
import javax.ws.rs.client.WebTarget

interface TradeRequest {
    val symbol: String?
    val clazz: String?
    val startDate: String?
    val endDate: String?
    val timeZone: String?
    val workdayCalendar: String?
    val exchange: String?
}

data class RawTradeRequest(
    override val symbol: String?, override val clazz: String?,
    override val startDate: String?,
    override val endDate: String?,
    override val timeZone: String? = null,
    override val workdayCalendar: String? = null,
    override val exchange: String? = null
) : TradeRequest

enum class OhlcvStatistic {
    OPEN,
    HIGH,
    LOW,
    CLOSE,
    VOLUME,
    COUNT,
    VWAP,
    AMOUNT
}

data class OhlcvTradeRequest(
    override val symbol: String?, override val clazz: String?,
    override val startDate: String?,
    override val endDate: String?,
    override val timeZone: String? = null,
    override val workdayCalendar: String? = null,
    override val exchange: String? = null,
    val period: Period? = null,
    val statistics: List<OhlcvStatistic>? = null
) : TradeRequest

class StatisticsRequest(
    val baseTradeRequest: TradeRequest,
    val statistics: List<OhlcvStatistic>? = null
)

interface BarsRequest {
    val statisticsRequest: StatisticsRequest
    fun addQueryParameter(target: WebTarget): WebTarget
}

class BarsCountRequest(
    override val statisticsRequest: StatisticsRequest,
    val dailyBarsCount: Int
) : BarsRequest {
    override fun addQueryParameter(target: WebTarget): WebTarget =
        target.queryParam("dailyBarsCount", dailyBarsCount)
}

class BarsSizeRequest(
    override val statisticsRequest: StatisticsRequest,
    val barsSize: BigDecimal
): BarsRequest {
    override fun addQueryParameter(target: WebTarget): WebTarget =
        target.queryParam("barSize", barsSize)
}

class BarsTimeRequest(
    override val statisticsRequest: StatisticsRequest,
    val period: String
) : BarsRequest {
    override fun addQueryParameter(target: WebTarget): WebTarget =
        target.queryParam("period", period)
}

enum class BarsRequestType(val path: String) {
    COUNT("bars/count"),
    VOLUME("bars/volume"),
    AMOUNT("bars/amount"),
    TIME("bars/time")
}

data class ResponseLine(
    val dateMillis: Long = 0,
    val open: BigDecimal? = null,
    val high: BigDecimal? = null,
    val low: BigDecimal? = null,
    val close: BigDecimal? = null,
    val volume: Int? = 0
)
