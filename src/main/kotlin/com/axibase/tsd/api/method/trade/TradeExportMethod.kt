package com.axibase.tsd.api.method.trade

import com.axibase.tsd.api.method.BaseMethod
import com.axibase.tsd.api.model.Period
import org.apache.http.HttpStatus
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

private const val RAW_PATH = "trades"
private const val OHLCV_PATH = "ohlcv"

class TradeExportMethod : BaseMethod() {
    companion object {
        @JvmStatic
        fun rawResponse(rawTradeRequest: RawTradeRequest? = null): Response =
            executeTradeExportRequest(RAW_PATH, rawTradeRequest)
            { target, request ->
                fillRequestsParams(target, request)
            }


        @JvmStatic
        fun rawCsv(rawTradeRequest: RawTradeRequest? = null): String = rawResponse(rawTradeRequest).toCsv()

        @JvmStatic
        fun ohlcvResponse(ohlcvTradeRequest: OhlcvTradeRequest? = null): Response =
            executeTradeExportRequest(OHLCV_PATH, ohlcvTradeRequest)
            { target, request ->
                fillRequestsParams(target, request)
                    .queryParam("period", request.period)
                    .queryParam("statistics", request.statistics?.toCommaSeparatedList())
            }

        @JvmStatic
        fun ohlcvCsv(ohlcvTradeRequest: OhlcvTradeRequest? = null) = ohlcvResponse(ohlcvTradeRequest).toCsv()


        private fun <T : TradeRequest> executeTradeExportRequest(
            path: String, tradeRequest: T?,
            reqFiller: (WebTarget, T) -> WebTarget
        ): Response {
            return executeApiRequest {
                val ohlcvTarget = it.path(path)
                val target = if (tradeRequest != null)
                    reqFiller(ohlcvTarget, tradeRequest)
                else ohlcvTarget
                val resp = target.request().get()
                resp.bufferEntity()
                resp
            }
        }

        private fun fillRequestsParams(target: WebTarget, tradeRequest: TradeRequest): WebTarget {
            return target.queryParam("symbol", tradeRequest.symbol)
                .queryParam("class", tradeRequest.clazz)
                .queryParam("startDate", tradeRequest.startDate)
                .queryParam("endDate", tradeRequest.endDate)
                .queryParam("exchange", tradeRequest.exchange)
                .queryParam("workdayCalendar", tradeRequest.workdayCalendar)
                .queryParam("timezone", tradeRequest.timeZone)
        }
    }

    data class ErrorMessage(val error: String? = null)
}


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

private fun List<OhlcvStatistic>.toCommaSeparatedList(): String = this.joinToString(",") { it.toString() }

private fun Response.toCsv(): String {
    if (this.status == HttpStatus.SC_OK) {
        return this.readEntity(String::class.java);
    }
    if (Response.Status.Family.CLIENT_ERROR.equals(this.statusInfo.family)) {
        val errorMessage = this.readEntity(TradeExportMethod.ErrorMessage::class.java)
        throw IllegalStateException(errorMessage.error)
    }
    throw IllegalStateException("Unexpected response: $this")
}
