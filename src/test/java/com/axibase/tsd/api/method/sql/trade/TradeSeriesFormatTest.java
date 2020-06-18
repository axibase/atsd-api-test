package com.axibase.tsd.api.method.sql.trade;

import com.axibase.tsd.api.model.financial.Trade;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Util;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TradeSeriesFormatTest extends SqlTradeTest {

    @BeforeClass
    public void prepareData() throws Exception {
        Trade trade = fromISOString("2020-05-19T10:21:49.123456Z").setPrice(new BigDecimal("1.12")).setQuantity(15);
        insert(trade);
    }

    @Test
    public void testSingleSeries() throws Exception {
        String sql = "select datetime, price from atsd_trade where " + instrumentCondition();
        Response response = querySeriesResponse(sql);
        Series[] result = response.readEntity(Series[].class);
        assertEquals(result.length, 1);
        final Series series = result[0];
        assertEquals("sql", series.getEntity());
        assertEquals("price", series.getMetric());
        List<Sample> samples = series.getData();
        assertEquals(samples.size(), 1);
        Sample expected = Sample.ofTimeDecimal(Util.getUnixTime("2020-05-19T10:21:49.123Z"), new BigDecimal("1.12"));
        assertTrue(samples.get(0).theSame(expected));
    }

    @Test
    public void testTwoSeries() throws Exception {
        String sql = "select datetime, price, quantity from atsd_trade where " + instrumentCondition();
        Response response = querySeriesResponse(sql);
        Series[] result = response.readEntity(Series[].class);
        assertEquals(result.length, 2);
        Series priceSeries = Arrays.stream(result).filter(series -> series.getMetric().equals("price")).findFirst().get();
        Series quantitySeries = Arrays.stream(result).filter(series -> series.getMetric().equals("quantity")).findFirst().get();

        assertEquals(1, priceSeries.getData().size());
        assertEquals(1, quantitySeries.getData().size());
        Sample expectedPrice = Sample.ofTimeDecimal(Util.getUnixTime("2020-05-19T10:21:49.123Z"), new BigDecimal("1.12"));
        assertTrue(priceSeries.getData().get(0).theSame(expectedPrice));
        Sample expectedQuantity = Sample.ofTimeInteger(Util.getUnixTime("2020-05-19T10:21:49.123Z"), 15);
        assertTrue(quantitySeries.getData().get(0).theSame(expectedQuantity));
    }
}