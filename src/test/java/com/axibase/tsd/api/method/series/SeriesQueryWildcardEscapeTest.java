package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.json.JSONArray;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static org.testng.AssertJUnit.assertEquals;

public class SeriesQueryWildcardEscapeTest {
    private static final String ENTITY_PREFIX = Mocks.entity();

    @DataProvider
    public static Object[][] provideSpecialCharacters() {
        return new Object[][] {
                {'*'}, {'?'}, {'\\'}
        };
    }

    @Issue("4662")
    @Test(dataProvider = "provideSpecialCharacters")
    public static void testEnitySpecialCharEscape(char c) throws Exception {
        String entityName = ENTITY_PREFIX + c + "e1";

        Series series = new Series(entityName, Mocks.metric());
        series.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series);

        SeriesQuery seriesQuery = new SeriesQuery(series);
        Response queryResponse = SeriesMethod.querySeries(seriesQuery);
        String actualEntityName = new JSONArray(queryResponse.readEntity(String.class))
                .getJSONObject(0).getString("entity");


        assertEquals("", entityName, actualEntityName);
    }

    @Issue("4662")
    @Test(dataProvider = "provideSpecialCharacters")
    public static void testEntitySpecialCharEscapeNotFound(char c) throws Exception {
        String entityName = ENTITY_PREFIX + c + "e2";
        String metricName =  Mocks.metric();

        EntityMethod.createOrReplaceEntityCheck(new Entity(entityName));
        MetricMethod.createOrReplaceMetricCheck(new Metric(metricName));

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName,
                MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        Response queryResponse = SeriesMethod.querySeries(seriesQuery);
        String actualEntityName = new JSONArray(queryResponse.readEntity(String.class))
                .getJSONObject(0).getString("entity");

        String expectedEntityName = ENTITY_PREFIX + '\\' + c + "e2";

        assertEquals("", expectedEntityName, actualEntityName);
    }

    @Issue("4662")
    @Test(dataProvider = "provideSpecialCharacters")
    public static void testTagSpecialCharEscape(char c) throws Exception {
        String entityName = Mocks.entity();
        String metircName = Mocks.metric();
        String tagValue = "tag" + c;

        Series series = new Series(entityName, metircName);
        series.addTag("tag", tagValue);
        series.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series);

        SeriesQuery seriesQuery = new SeriesQuery(series);
        Response queryResponse = SeriesMethod.querySeries(seriesQuery);
        String actualTagValue = new JSONArray(queryResponse.readEntity(String.class))
                .getJSONObject(0).getJSONObject("tags").getString("tag");

        assertEquals("", tagValue, actualTagValue);
    }

    @Issue("4662")
    @Test(dataProvider = "provideSpecialCharacters")
    public static void testTagSpecialCharEscapeNotFound(char c) throws Exception {
        String entityName = Mocks.entity();
        String metricName = Mocks.metric();
        String tagValue = "tag" + c;

        EntityMethod.createOrReplaceEntityCheck(new Entity(entityName));
        MetricMethod.createOrReplaceMetricCheck(new Metric(metricName));

        SeriesQuery seriesQuery = new SeriesQuery(metricName, entityName,
                MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        seriesQuery.addTag("tag", tagValue);
        Response queryResponse = SeriesMethod.querySeries(seriesQuery);
        String actualTagValue = new JSONArray(queryResponse.readEntity(String.class))
                .getJSONObject(0).getJSONObject("tags").getString("tag");

        String expectedTagValue = "tag" + '\\' + c;

        assertEquals("", expectedTagValue, actualTagValue);
    }
}
