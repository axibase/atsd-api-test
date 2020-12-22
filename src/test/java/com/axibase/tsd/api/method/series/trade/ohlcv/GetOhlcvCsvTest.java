package com.axibase.tsd.api.method.series.trade.ohlcv;

import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import com.axibase.tsd.api.util.TradeSender;
import lombok.RequiredArgsConstructor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static com.axibase.tsd.api.util.Util.getUnixTime;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GetOhlcvCsvTest {
    private final String endpointSubPath = "ohlcv";
    private final String exchange = Mocks.tradeExchange();
    private final String clazz = Mocks.tradeClass();
    private final String symbol = Mocks.tradeSymbol();

    @BeforeClass
    public void insertTrades() throws Exception {
        List<Trade> trades = new ArrayList<>();
        try (Scanner scanner = new Scanner(GetOhlcvCsvTest.class.getResourceAsStream("trades.csv"))){
            int lineNumber = 1;
            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(",");
                trades.add(trade(lineNumber++, values[0], Trade.Side.valueOf(values[1]), values[2]));
            }
        }
        TradeSender.send(trades).waitUntilTradesInsertedAtMost(1, TimeUnit.MINUTES);
    }

    @DataProvider
    public Object[][] testCases() {
        final String date1 = "2020-11-25T14:00:00Z";
        final String date2 = "2020-11-25T14:01:00Z";
        final String date3 = "2020-11-25T14:02:00Z";
        final String period = "1 minute";
        final String tz = "Europe/Moscow";

        /* OHLCV for the minute 1. */
        final BigDecimal open1 = BigDecimal.valueOf(23);
        final BigDecimal high1 = BigDecimal.valueOf(999);
        final BigDecimal low1 = new BigDecimal("0.001");
        final BigDecimal close1 = BigDecimal.valueOf(71);
        final int volume1 = 41;
        final ResponseLine line1 = new ResponseLine(getUnixTime(date1), open1, high1, low1, close1, volume1);

        /* OHLCV for the minute 2. */
        final BigDecimal open2 = BigDecimal.valueOf(70);
        final BigDecimal high2 = BigDecimal.valueOf(9999);
        final BigDecimal low2 = new BigDecimal("0.01");
        final BigDecimal close2 = BigDecimal.valueOf(1);
        final int volume2 = 70;
        final ResponseLine line2 = new ResponseLine(getUnixTime(date2), open2, high2, low2, close2, volume2);

        /* OHLCV for both minutes. */
        final BigDecimal open = open1;
        final BigDecimal high = high2;
        final BigDecimal low = low1;
        final BigDecimal close = close2;
        final int volume = volume1 + volume2;
        final ResponseLine line = new ResponseLine(getUnixTime(date1), open, high, low, close, volume);

        TestCase[] testCases = {
                new TestCase(symbol, clazz, exchange, date1, date3, period, tz, false, "", new ResponseLine[]{line1, line2}),
                new TestCase(symbol, clazz, exchange, date1, null, period, tz, false, "", new ResponseLine[]{line1, line2}),
                new TestCase(symbol, clazz, "", date1, "", period, "", false, "", new ResponseLine[]{line1, line2}),
                new TestCase(symbol, clazz, exchange, date1, date2, period, tz, false, "", new ResponseLine[]{line1}),
                new TestCase(symbol, clazz, exchange, date2, date3, period, tz, false, "", new ResponseLine[]{line2}),
                new TestCase(symbol, clazz, exchange, date1, date3, null, tz, false, "", new ResponseLine[]{line}),
                new TestCase(symbol, clazz, null, date1, null, null, null, false, "", new ResponseLine[]{line}),
                new TestCase(null, clazz, exchange, date1, date3, period, tz, true, " 'symbol' ", null),
                new TestCase(symbol, null, exchange, date1, date3, period, tz, true, " 'class' ", null),
                new TestCase(symbol, clazz, exchange, null, date3, period, tz, true, " 'startDate' ", null),
        };
        return TestUtil.convertTo2DimArray(testCases);
    }

    @Test(dataProvider = "testCases")
    public void test(final TestCase testCase) throws Exception {
        Response response = BaseMethod.executeApiRequest(webTarget ->
                testCase.setQueryParameters(webTarget).path(endpointSubPath).request().get());
        if (testCase.isErrorResponse) {
            checkErrorResponse(response, testCase.errorSubstring);
        } else {
            checkRegularResponse(response, testCase.responseLines);
        }
    }

    private void checkErrorResponse(Response response, String expectedSubstring) throws Exception {
        assertTrue(response.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE));
        String errorMessage = BaseMethod.extractErrorMessage(response);
        String explanation = String.format("Actual error message '%s' does not contains expected sub-string '%s'.",
                errorMessage, expectedSubstring);
        assertTrue(errorMessage.contains(expectedSubstring), explanation);
    }

    private void checkRegularResponse(Response response, ResponseLine[] expectedLines) {
        assertTrue(response.getMediaType().isCompatible(MediaType.TEXT_PLAIN_TYPE));
        String csv = BaseMethod.responseAsString(response);
        String[] actualLines = csv.split("\\r?\\n");
        assertEquals(actualLines.length, expectedLines.length + 1, "Unexpected lines count in response.");
        String header = "datetime,open,high,low,close,volume";
        assertEquals(actualLines[0], header, "Unexpected header line in response.");
        for (int i = 0; i < expectedLines.length; i++) {
            checkLine(actualLines[i + 1], expectedLines[i]);
        }
    }

    private void checkLine(String actualLine, ResponseLine expectedLine) {
        String[] actualFields = actualLine.split(",");
        assertEquals(actualFields.length, 6, "Unexpected count of fields in line: " + actualLine);
        assertEquals(getUnixTime(actualFields[0]), expectedLine.dateMillis, "Unexpected timestamp.");
        assertEquals(new BigDecimal(actualFields[1]), expectedLine.open, "Unexpected OPEN value.");
        assertEquals(new BigDecimal(actualFields[2]), expectedLine.high, "Unexpected HIGH value.");
        assertEquals(new BigDecimal(actualFields[3]), expectedLine.low, "Unexpected LOW value.");
        assertEquals(new BigDecimal(actualFields[4]), expectedLine.close, "Unexpected CLOSE value.");
        assertEquals(Integer.parseInt(actualFields[5]), expectedLine.volume, "Unexpected VOLUME value.");
    }

    private Trade trade(int tradeNumber, String date, Trade.Side side, String price) {
        Trade trade = new Trade(exchange, clazz, symbol, tradeNumber, getUnixTime(date), new BigDecimal(price), 1);
        trade.setSide(side);
        return trade;
    }

    @RequiredArgsConstructor
    private static class TestCase {
        /* Request parameters. */
        private final String symbol;
        private final String clazz;
        private final String exchange;
        private final String startDate;
        private final String endDate;
        private final String period;
        private final String timezone;

        /* Response parameters. */
        private final boolean isErrorResponse;
        private final String errorSubstring;        // error message must contain this substring
        private final ResponseLine[] responseLines; // response lines in case there is no error

        WebTarget setQueryParameters(WebTarget webTarget) {
            webTarget = setParameter(webTarget, "symbol", symbol);
            webTarget = setParameter(webTarget, "class", clazz);
            webTarget = setParameter(webTarget, "exchange", exchange);
            webTarget = setParameter(webTarget, "startDate", startDate);
            webTarget = setParameter(webTarget, "endDate", endDate);
            webTarget = setParameter(webTarget, "period", period);
            webTarget = setParameter(webTarget, "timezone", timezone);
            return webTarget;
        }

        WebTarget setParameter(WebTarget webTarget, String name, String value) {
            if (value != null) {
                webTarget = webTarget.queryParam(name, value);
            }
            return webTarget;
        }
    }

    @RequiredArgsConstructor
    private static class ResponseLine {
        private final long dateMillis;
        private final BigDecimal open;
        private final BigDecimal high;
        private final BigDecimal low;
        private final BigDecimal close;
        private final int volume;
    }
}
