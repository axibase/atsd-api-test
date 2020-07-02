package com.axibase.tsd.api.method.sql.trade

import com.axibase.tsd.api.method.BaseMethod
import com.axibase.tsd.api.method.sql.OutputFormat
import com.axibase.tsd.api.util.TradeSender
import com.axibase.tsd.api.util.Util
import org.apache.commons.lang3.StringUtils
import org.testng.AssertJUnit.assertEquals
import org.testng.AssertJUnit.assertSame
import org.testng.annotations.BeforeClass
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.ws.rs.core.Response

class TradeCsvTest : SqlTradeTest() {

    @BeforeClass
    fun prepareData() {
        val timestamp = Util.getUnixTime("2020-05-27T10:05:06Z")
        val one = trade(timestamp, BigDecimal.valueOf(2200000), 1000000)
        val two = trade(timestamp, BigDecimal("15000.45"), 12345678)
        TradeSender.send(one, two).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES)
    }

    @Test(dataProvider = "testData")
    fun test(description: String, sql: String, expected: String) {
        val response = executeSqlRequest({ webTarget ->
            webTarget
                    .queryParam("q", sql)
                    .queryParam("outputFormat", OutputFormat.CSV)
                    .request()
                    .get()
        })
        response.bufferEntity()
        assertSame(description, Response.Status.Family.SUCCESSFUL, Util.responseFamily(response))
        assertEquals(description, expected.trimIndent(), StringUtils.replace(BaseMethod.responseAsString(response), "\r", ""))
    }

    @DataProvider
    fun testData(): Array<Array<String>> {
        return arrayOf(
                arrayOf(
                        "Test single value output in scientific format",
                        "select high() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol",

                        """
                        "high()"
                        2200000

                        """
                ),

                arrayOf(
                        "Test multiple values output in scientific format",
                        "select high(), high(), high() from atsd_trade where ${instrumentCondition()} group by exchange, class, symbol",

                        """
                        "high()","high()","high()"
                        2200000,2200000,2200000

                        """
                ),

                arrayOf(
                        "Test mixed values type output",
                        "select price, datetime,  'test', price, null, price, quantity, price from atsd_trade where ${instrumentCondition()}",

                        """
                        "price","datetime","'test'","price","null","price","quantity","price"
                        2200000,"2020-05-27T10:05:06.000000Z","test",2200000,,2200000,1000000,2200000
                        15000.45,"2020-05-27T10:05:06.000000Z","test",15000.45,,15000.45,12345678,15000.45

                        """
                ),

                arrayOf(
                        "Test null and BigDecimal",
                        "select null, price from atsd_trade where ${instrumentCondition()}",

                        """
                        "null","price"
                        ,2200000
                        ,15000.45

                        """
                ),

                arrayOf(
                        "Test BigDecimal and null",
                        "select price, null from atsd_trade where ${instrumentCondition()}",

                        """
                        "price","null"
                        2200000,
                        15000.45,

                        """
                ),
                arrayOf("Test values without nulls and BigDecimals",
                        "select  datetime,  'test', quantity from atsd_trade where ${instrumentCondition()}",

                        """
                        "datetime","'test'","quantity"
                        "2020-05-27T10:05:06.000000Z","test",1000000
                        "2020-05-27T10:05:06.000000Z","test",12345678

                        """
                ),
                arrayOf("Test several BigDecimals at the beginning of the row",
                        "select  time,  price, price, quantity, 'test' from atsd_trade where ${instrumentCondition()}",

                        """
                        "time","price","price","quantity","'test'"
                        1590573906000.000,2200000,2200000,1000000,"test"
                        1590573906000.000,15000.45,15000.45,12345678,"test"

                        """
                ),
                arrayOf("Test several BigDecimals in the middle of the row",
                        "select  'test', time,  price, price, quantity, 'test' from atsd_trade where ${instrumentCondition()}",

                        """
                        "'test'","time","price","price","quantity","'test'"
                        "test",1590573906000.000,2200000,2200000,1000000,"test"
                        "test",1590573906000.000,15000.45,15000.45,12345678,"test"

                        """
                ),
                arrayOf("Test several BigDecimals at the end of the row",
                        "select  'test', time,  price, price from atsd_trade where ${instrumentCondition()}",

                        """
                        "'test'","time","price","price"
                        "test",1590573906000.000,2200000,2200000
                        "test",1590573906000.000,15000.45,15000.45

                        """
                )
        )
    }

}