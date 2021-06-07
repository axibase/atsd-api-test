package com.axibase.tsd.api.method.trade

import com.axibase.tsd.api.method.BaseMethod
import com.axibase.tsd.api.model.trade.ohlcv.*
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

        @JvmStatic
        fun barResponse(type: BarsRequestType, barsRequest: BarsRequest): Response =
            executeTradeExportRequest(type.path, barsRequest, { target, request ->
                val statRequest = request.statisticsRequest
                barsRequest.addQueryParameter(
                    fillRequestsParams(target, statRequest.baseTradeRequest)
                        .queryParam("statistics", statRequest.statistics?.toCommaSeparatedList()))
            })

        @JvmStatic
        fun barCsv(type: BarsRequestType, barsRequest: BarsRequest) = barResponse(type, barsRequest).toCsv()

        private fun <T> executeTradeExportRequest(
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
