package com.axibase.tsd.api.method.trade

import com.axibase.tsd.api.method.BaseMethod
import org.apache.http.HttpStatus
import javax.ws.rs.core.Response

private const val RAW_PATH = "/trades"

class TradeExportMethod : BaseMethod() {
    companion object {
        @JvmStatic
        fun rawResponse(rawTradeRequest: RawTradeRequest? = null): Response {
            return executeApiRequest {
                val tradesTarget = it.path(RAW_PATH)
                val target = if (rawTradeRequest != null)
                    tradesTarget.queryParam("symbol", rawTradeRequest.symbol)
                        .queryParam("class", rawTradeRequest.clazz)
                        .queryParam("startDate", rawTradeRequest.startDate)
                        .queryParam("endDate", rawTradeRequest.endDate)
                        .queryParam("exchange", rawTradeRequest.exchange)
                        .queryParam("workdayCalendar", rawTradeRequest.workdayCalendar)
                        .queryParam("timezone", rawTradeRequest.timeZone)
                else tradesTarget
                val resp = target.request().get()
                resp.bufferEntity()
                resp;
            }
        }


        @JvmStatic
        fun rawCsv(rawTradeRequest: RawTradeRequest? = null): String {
            val resp = rawResponse(rawTradeRequest);
            resp.bufferEntity()
            if (resp.status == HttpStatus.SC_OK) {
                return resp.readEntity(String::class.java);
            }
            if (Response.Status.Family.CLIENT_ERROR.equals(resp.statusInfo.family)) {
                val errorMessage = resp.readEntity(ErrorMessage::class.java)
                throw IllegalStateException(errorMessage.error)
            }
            throw IllegalStateException("Unexpected response: $resp")
        }
    }

    data class ErrorMessage(val error: String? = null)
}

data class RawTradeRequest(
    val symbol: String?, val clazz: String?, val startDate: String?, val endDate: String?,
    val timeZone: String? = null, val workdayCalendar: String? = null, val exchange: String? = null
)
