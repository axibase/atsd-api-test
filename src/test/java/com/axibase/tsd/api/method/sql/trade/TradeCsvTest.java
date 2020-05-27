package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.method.sql.OutputFormat;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.TradeSender;
import com.axibase.tsd.api.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

public class TradeCsvTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        long timestamp = Util.getUnixTime("2020-05-27T10:05:06Z");
        Trade one = trade(timestamp, BigDecimal.valueOf(2200000), 1000000);
        Trade two = trade(timestamp, new BigDecimal("15000.45"), 12345678);
        TradeSender.send(one, two).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @Test(dataProvider = "testData")
    public void test(String description, String sql, String expected) throws Exception {
        final Response response = executeSqlRequest(webTarget -> webTarget
                .queryParam("q", sql)
                .queryParam("outputFormat", OutputFormat.CSV)
                .request()
                .get());
        response.bufferEntity();
        assertSame(description, Response.Status.Family.SUCCESSFUL, Util.responseFamily(response));
        assertEquals(description, expected, StringUtils.replace(responseAsString(response), "\r", ""));
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][]{
                {
                        "Test single value output in scientific format",
                        "select high() from atsd_trade where " + instrumentCondition() + " group by exchange, class, symbol",
                        "\"high()\"\n" +
                                "2200000\n"
                }, {
                "Test multiple values output in scientific format",
                "select high(), high(), high() from atsd_trade where " + instrumentCondition() + " group by exchange, class, symbol",
                "\"high()\",\"high()\",\"high()\"\n" +
                        "2200000,2200000,2200000\n"
        },
                {
                        "Test mixed values type output",
                        "select price, datetime,  'test', price, null, price, quantity, price from atsd_trade where " + instrumentCondition(),
                        "\"price\",\"datetime\",\"'test'\",\"price\",\"null\",\"price\",\"quantity\",\"price\"\n" +
                                "2200000,\"2020-05-27T10:05:06.000000Z\",\"test\",2200000,,2200000,1000000,2200000\n" +
                                "15000.45,\"2020-05-27T10:05:06.000000Z\",\"test\",15000.45,,15000.45,12345678,15000.45\n"
                },
                {
                        "Test null and BigDecimal",
                        "select null, price from atsd_trade where " + instrumentCondition(),
                        "\"null\",\"price\"\n" +
                                ",2200000\n" +
                                ",15000.45\n"
                },
                {
                        "Test BigDecimal and null",
                        "select price, null from atsd_trade where " + instrumentCondition(),
                        "\"price\",\"null\"\n" +
                                "2200000,\n" +
                                "15000.45,\n"
                },
                {
                        "Test values without nulls and BigDecimals",
                        "select  datetime,  'test', quantity from atsd_trade where " + instrumentCondition(),
                        "\"datetime\",\"'test'\",\"quantity\"\n" +
                                "\"2020-05-27T10:05:06.000000Z\",\"test\",1000000\n" +
                                "\"2020-05-27T10:05:06.000000Z\",\"test\",12345678\n"

                }

        };
    }
}