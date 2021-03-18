package com.axibase.tsd.api.method.series.trade.wvap;

import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.series.trade.SyntheticDataProvider;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.CommonAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.MathContext;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test VWAP aggregation and grouping.
 */
public class VwapTest {
    private final SyntheticDataProvider dataProvider = new SyntheticDataProvider();
    /* Use it to compare actual and expected values, because double arithmetic is not reliable. */
    private final MathContext roundingContext = new MathContext(12);

    @BeforeClass
    public void insertTrades() throws Exception {
        dataProvider.insertTrades();
    }

    @DataProvider
    public Object[][] testCases() {
        VwapTestCasesBuilder testBuilder = new VwapTestCasesBuilder(dataProvider);
        return testBuilder.testCases();
    }

    @Test(dataProvider = "testCases")
    public void test(TestCase testCase) {
        List<Series> response = SeriesMethod.querySeriesAsList(testCase.query);
        List<ExpectedSeries> expectedSeriesList = testCase.seriesList;
        assertEquals(response.size(), expectedSeriesList.size(), "Unexpected series count in response.");
        for (Series series : response) {
            checkSeries(series, expectedSeriesList);
        }
    }

    private void checkSeries(Series actualSeries, List<ExpectedSeries> expectedSeriesList) {
        SeriesSign sign = SeriesSign.of(actualSeries);
        ExpectedSeries expectedSeries = findInList(sign, expectedSeriesList);
        assertNotNull(expectedSeries, "Series with unexpected meta data in response: " + sign.toString() );
        CommonAssertions.assertEqualSamples(actualSeries.getData(), expectedSeries.getSamples(), "", roundingContext);
    }

    private ExpectedSeries findInList(SeriesSign sign, List<ExpectedSeries> expectedSeriesList) {
        ExpectedSeries found = null;
        for (ExpectedSeries expectedSeries : expectedSeriesList) {
            if (sign.equals(expectedSeries.getSign())) {
                found = expectedSeries;
                break;
            }
        }
        if (found != null) {
            expectedSeriesList.remove(found);
        }
        return found;
    }

}
