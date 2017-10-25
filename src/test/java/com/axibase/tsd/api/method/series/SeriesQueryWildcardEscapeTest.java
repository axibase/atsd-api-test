package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
    @Test(
            description = "Test series query for entity name that contains wildcard",
            dataProvider = "provideSpecialCharacters"
    )
    public static void testEntitySpecialCharEscape(char c) throws Exception {
        String entityName = ENTITY_PREFIX + c + "e1";

        Series series = new Series(entityName, Mocks.metric());
        series.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series);

        SeriesQuery seriesQuery = new SeriesQuery(series);
        String actualEntityName = SeriesMethod.querySeriesAsList(seriesQuery).get(0).getEntity();

        assertEquals("Wrong result when performing series query with entity that contains wildcard character " + c,
                entityName, actualEntityName);
    }

    @Issue("4662")
    @Test(
            description = "Test series query for entity name that contains wildcard and nothing were found",
            dataProvider = "provideSpecialCharacters"
    )
    public static void testEntitySpecialCharEscapeNotFound(char c) throws Exception {
        String entityName = ENTITY_PREFIX + c + "e2";
        String metricName =  Mocks.metric();

        Series series = new Series(entityName, metricName);

        EntityMethod.createOrReplaceEntityCheck(new Entity(entityName));
        MetricMethod.createOrReplaceMetricCheck(new Metric(metricName));

        SeriesQuery seriesQuery = new SeriesQuery(series);
        String actualEntityName = SeriesMethod.querySeriesAsList(seriesQuery).get(0).getEntity();

        String expectedEntityName = ENTITY_PREFIX + '\\' + c + "e2";

        assertEquals("Wrong result when performing series query with entity that contains wildcard character " + c +
                " and no series were found", expectedEntityName, actualEntityName);
    }

    @Issue("4662")
    @Test(
            description = "Test series query for tag value that contains wildcard",
            dataProvider = "provideSpecialCharacters"
    )
    public static void testTagSpecialCharEscape(char c) throws Exception {
        String entityName = Mocks.entity();
        String metircName = Mocks.metric();
        String tagValue = "tag" + c;

        Series series = new Series(entityName, metircName);
        series.addTag("tag", tagValue);
        series.addSamples(Mocks.SAMPLE);

        SeriesMethod.insertSeriesCheck(series);

        SeriesQuery seriesQuery = new SeriesQuery(series);
        String actualTagValue = SeriesMethod.querySeriesAsList(seriesQuery).get(0).getTags().get("tag");

        assertEquals("Wrong result when performing series query with tag value that contains wildcard character " + c,
                tagValue, actualTagValue);
    }

    @Issue("4662")
    @Test(
            description = "Test series query for tag value that contains wildcard and nothing were found",
            dataProvider = "provideSpecialCharacters"
    )
    public static void testTagSpecialCharEscapeNotFound(char c) throws Exception {
        String entityName = Mocks.entity();
        String metricName = Mocks.metric();

        String expectedTagValue = "tag" + '\\' + c;

        EntityMethod.createOrReplaceEntityCheck(new Entity(entityName));
        MetricMethod.createOrReplaceMetricCheck(new Metric(metricName));

        SeriesQuery seriesQuery = new SeriesQuery(metricName, entityName,
                MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        seriesQuery.addTag("tag", expectedTagValue);
        String actualTagValue = SeriesMethod.querySeriesAsList(seriesQuery).get(0).getTags().get("tag");

        assertEquals("Wrong result when performing series query with tag value that contains wildcard character " + c +
                        " and no series were found", expectedTagValue, actualTagValue);
    }
}
