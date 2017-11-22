package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.model.series.SeriesQueryType;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.TestUtil.MILLIS_IN_HOUR;
import static com.axibase.tsd.api.util.TestUtil.MILLIS_IN_YEAR;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class SeriesQueryAddMetaTest extends SeriesMethod {
    private static final String ENTITY_NAME = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series s = new Series(ENTITY_NAME, METRIC_NAME);
        long currentTime = System.currentTimeMillis();
        for (int i = 1; i <= 500; i++) {
            s.addSamples(Sample.ofTimeInteger(currentTime - i * MILLIS_IN_HOUR, 123));
        }

        insertSeriesCheck(s);
    }

    @DataProvider
    Object[][] seriesQueryTypeProvider() {
        Object[][] result = new Object[SeriesQueryType.values().length][];
        for (int i = 0; i < SeriesQueryType.values().length; i++) {
            result[i] = new Object[]{SeriesQueryType.values()[i]};
        }
        return result;
    }

    @Issue("4713")
    @Test(
            description = "Check that meta is included for all types of data",
            dataProvider = "seriesQueryTypeProvider"
    )
    public void testSeriesResponseMetaIncluded(SeriesQueryType type) throws JSONException {
        long currentTime = System.currentTimeMillis();
        SeriesQuery seriesQuery =
                new SeriesQuery(ENTITY_NAME, METRIC_NAME,
                                Util.ISOFormat(currentTime - MILLIS_IN_YEAR),
                                Util.ISOFormat(currentTime + MILLIS_IN_YEAR))
                        .setAddMeta(true)
                        .setType(type);

        JSONArray responses = new JSONArray(querySeries(seriesQuery).readEntity(String.class));
        assertEquals(String.format("Response for series query of type %s has inappropriate length", type),
                1, responses.length());
        JSONObject response = responses.getJSONObject(0);
        assertTrue(String.format("Response for series query of type %s doesn't contain meta", type),
                response.has("meta"));
        JSONObject meta = response.getJSONObject("meta");
        assertTrue(String.format("Response for series query of type %s doesn't contain metric meta", type),
                meta.has("entity"));
        assertTrue(String.format("Response for series query of type %s doesn't contain entity meta", type),
                meta.has("metric"));
    }
}
