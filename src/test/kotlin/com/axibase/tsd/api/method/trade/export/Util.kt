package com.axibase.tsd.api.method.trade.export

import com.axibase.tsd.api.model.financial.Trade
import com.axibase.tsd.api.model.trade.ohlcv.ResponseLine
import com.axibase.tsd.api.util.TradeSender
import com.axibase.tsd.api.util.Util
import org.testng.Assert
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.streams.toList

/**
 * @param csvFilePath - path to the resource file with trades stored as csv lines
 * @param tradeCreator - function which converts string line values to a Trade
 * @param linesToSkip  - specify to skip several header lines
 */
fun insertTrades(csvFilePath: String,
                 tradeCreator: (List<String>) -> Trade,
                 linesToSkip: Long = 0) {
    val inSteam = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(csvFilePath)
        ?: throw IllegalArgumentException("Failed to read file: $csvFilePath")
    val trades = inSteam
        .bufferedReader().lines()
        .skip(linesToSkip)
        .filter(String::isNotBlank)
        .map {
            val values = it
                .split(",")
                .stream()
                .map { v -> v.trim() }
                .collect(Collectors.toList())
            tradeCreator(values)
        }.toList()
    TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
}


fun checkLine(lineIndex: Int, actualString: String, expectedLine: ResponseLine) {
    val actualLine = parseResponseLine(actualString)
    Assert.assertEquals(
        actualLine,
        expectedLine,
        "Line index $lineIndex. Expected line $expectedLine. Actual line: $actualLine"
    )
}

fun parseResponseLine(line: String): ResponseLine {
    val fields = line.split(",").toTypedArray()
    return ResponseLine(
        dateMillis = Util.getUnixTime(fields[0]),
        open = BigDecimal(fields[1]),
        high = BigDecimal(fields[2]),
        low = BigDecimal(fields[3]),
        close = BigDecimal(fields[4]),
        volume = fields[5].toInt())
}
