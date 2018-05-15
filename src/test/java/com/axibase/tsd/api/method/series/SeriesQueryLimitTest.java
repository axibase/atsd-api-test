package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.util.CommonAssertions;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.*;

public class SeriesQueryLimitTest extends SeriesMethod {

    private final static String DEFAULT_QUERY_LIMIT_METRIC = "series-query-limit-metric-1";
    private final static String DEFAULT_QUERY_LIMIT_ENTITY_PREFIX = "series-query-limit-entity";
    private final static int ENTITY_COUNT = 10;
    private final static int SERIES_WITH_TAGS_COUNT = 9;
    private final static int SAMPLES_COUNT = 10;
    private final static String START_SAMPLE_DATE = "2014-06-06T00:00:00.000Z";
    private static List<Series> seriesList = new ArrayList<>();
    private static List<String> entities = new ArrayList<>();

    static {
        Registry.Metric.checkExists(DEFAULT_QUERY_LIMIT_METRIC);
    }

    @DataProvider(name = "seriesQueryProvider")
    public static Object[][] buildSeriesQuery() {
        return new Object[][]{{prepareSeriesQueryWithEntities()}, {prepareSeriesQueryWithEntityPattern()}};
    }

    private static SeriesQuery prepareSeriesQueryWithEntities() {
        SeriesQuery query = new SeriesQuery();
        query.setMetric(DEFAULT_QUERY_LIMIT_METRIC);
        query.setEntities(entities);
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }

    private static SeriesQuery prepareSeriesQueryWithEntityPattern() {
        SeriesQuery query = new SeriesQuery();
        query.setMetric(DEFAULT_QUERY_LIMIT_METRIC);
        query.setEntity(DEFAULT_QUERY_LIMIT_ENTITY_PREFIX + "*");
        query.setStartDate(MIN_QUERYABLE_DATE);
        query.setEndDate(MAX_QUERYABLE_DATE);
        return query;
    }

    @BeforeClass
    public void prepareDataSet() throws Exception {
        String entityName;
        for (int i = 0; i < ENTITY_COUNT - 1; i++) {
            entityName = DEFAULT_QUERY_LIMIT_ENTITY_PREFIX.concat(String.valueOf(i));
            Registry.Entity.checkExists(entityName);
            entities.add(entityName);

            seriesList.addAll(makeSeriesWithTagList(entityName, DEFAULT_QUERY_LIMIT_METRIC, SERIES_WITH_TAGS_COUNT));
            seriesList.add(makeSeriesWithoutTag(entityName, DEFAULT_QUERY_LIMIT_METRIC));
        }

        insertSeriesCheck(seriesList);

        //require to insert in two step to define latest inserted entity
        entityName = DEFAULT_QUERY_LIMIT_ENTITY_PREFIX.concat(String.valueOf(ENTITY_COUNT - 1));
        Registry.Entity.checkExists(entityName);
        entities.add(entityName);

        seriesList.addAll(makeSeriesWithTagList(entityName, DEFAULT_QUERY_LIMIT_METRIC, SERIES_WITH_TAGS_COUNT));
        seriesList.add(makeSeriesWithoutTag(entityName, DEFAULT_QUERY_LIMIT_METRIC));
        insertSeriesCheck(seriesList);
    }

    private List<Series> makeSeriesWithTagList(String entityName, String metricName, int count) {
        List<Series> serieses = new ArrayList<>();
        Series series;
        for (int j = 0; j < count; j++) {
            String suffix = String.valueOf(j);
            series = new Series();
            series.setEntity(entityName);
            series.setMetric(metricName);
            series.addTag("entity-tag-".concat(suffix), "entity-tag-value-".concat(suffix));
            fillSeriesData(series);
            serieses.add(series);
        }
        return serieses;
    }

    private Series makeSeriesWithoutTag(String entityName, String metricName) {
        Series series = new Series();
        series.setEntity(entityName);
        series.setMetric(metricName);
        fillSeriesData(series);
        return series;
    }

    private void fillSeriesData(Series series) {
        String date = START_SAMPLE_DATE;
        for (int k = 0; k < SAMPLES_COUNT; k++) {
            series.addSamples(Sample.ofDateInteger(date, k));
            date = Util.addOneMS(date);
        }
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testSeriesLimit0(SeriesQuery query) throws Exception {
        query.setSeriesLimit(0);

        String expected = jacksonMapper.writeValueAsString(seriesList);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        String given = response.readEntity(String.class);

        assertTrue("All inserted series should be returned", compareJsonString(expected, given));
    }


    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testSeriesLimitNegative(SeriesQuery query) throws Exception {
        query.setSeriesLimit(-1);

        String expected = jacksonMapper.writeValueAsString(seriesList);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        String given = response.readEntity(String.class);

        assertTrue("All inserted series should be returned", compareJsonString(expected, given));
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testSeriesLimit1(SeriesQuery query) throws Exception {
        final int seriesLimit = 1;
        query.setSeriesLimit(seriesLimit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        assertEquals("Only one series should be returned", seriesLimit, seriesList.size());
        assertEquals("Sample count mismatch", SAMPLES_COUNT, seriesList.get(0).getData().size());
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testSeriesLimit5(SeriesQuery query) throws Exception {
        final int seriesLimit = 5;
        query.setSeriesLimit(seriesLimit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());

        assertSampleCountMatch(seriesList, SAMPLES_COUNT);
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testLimit20SeriesLimit0(SeriesQuery query) throws Exception {
        final int limit = 20;
        final int seriesLimit = 0;
        query.setLimit(limit);
        query.setSeriesLimit(seriesLimit);

        String expected = jacksonMapper.writeValueAsString(seriesList);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        String given = response.readEntity(String.class);

        CommonAssertions.jsonAssert("All inserted series should be returned", seriesList, response);
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testLimit1SeriesLimit11(SeriesQuery query) throws Exception {
        final int limit = 1;
        final int seriesLimit = 11;
        query.setLimit(limit);
        query.setSeriesLimit(seriesLimit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());


        assertTrue("Returned series should contain different entity", diffEntityCountGraterThan(seriesList, 1));

        assertSampleCountMatch(seriesList, limit);
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testLimit10000SeriesLimit11(SeriesQuery query) throws Exception {
        final int limit = 10000;
        final int seriesLimit = 2;
        query.setLimit(limit);
        query.setSeriesLimit(seriesLimit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());

        assertSampleCountMatch(seriesList, SAMPLES_COUNT);
    }

    @Issue("3211")
    @Test
    public void testLastInsertedEntitySeriesLimit2() throws Exception {
        final int seriesLimit = 2;
        SeriesQuery query = prepareSeriesQueryWithEntityPattern();
        query.setEntity(DEFAULT_QUERY_LIMIT_ENTITY_PREFIX + String.valueOf(ENTITY_COUNT - 1));
        query.setSeriesLimit(seriesLimit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());

        assertSampleCountMatch(seriesList, SAMPLES_COUNT);

        assertFalse("All series should be for one entity", diffEntityCountGraterThan(seriesList, 1));
    }

    @Issue("3211")
    @Test
    public void testLastInsertedEntitySeriesLimit2Limit5() throws Exception {
        final int seriesLimit = 2;
        final int limit = 5;
        SeriesQuery query = prepareSeriesQueryWithEntityPattern();
        query.setEntity(DEFAULT_QUERY_LIMIT_ENTITY_PREFIX.concat(String.valueOf(ENTITY_COUNT - 1)));
        query.setSeriesLimit(seriesLimit);
        query.setLimit(limit);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());

        assertSampleCountMatch(seriesList, limit);

        assertFalse("All series should be for one entity", diffEntityCountGraterThan(seriesList, 1));
    }

    @Issue("3211")
    @Test(dataProvider = "seriesQueryProvider")
    public void testLimit1SeriesLimit5ExactMatch(SeriesQuery query) throws Exception {
        final int seriesLimit = 5;
        final int limit = 1;
        query.setSeriesLimit(seriesLimit);
        query.setLimit(limit);
        query.setExactMatch(true);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", seriesLimit);
        assertEquals(message, seriesLimit, seriesList.size());

        assertSampleCountMatch(seriesList, limit);

        assertTrue("All series should be for different entity", diffEntityCountGraterThan(seriesList, seriesLimit - 1));
    }

    @Issue("3211")
    @Test
    public void testLastEntityLimit1SeriesLimit5ExactMatch() throws Exception {
        final int seriesLimit = 3;
        final int limit = 5;
        final int expectedSeries = 1;
        SeriesQuery query = prepareSeriesQueryWithEntityPattern();
        query.setEntity(DEFAULT_QUERY_LIMIT_ENTITY_PREFIX.concat(String.valueOf(ENTITY_COUNT - 1)));
        query.setSeriesLimit(seriesLimit);
        query.setLimit(limit);
        query.setExactMatch(true);

        Response response = querySeries(query);
        assertEquals("Fail to execute series query", OK.getStatusCode(), response.getStatus());

        List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
        });

        String message = String.format("%s series should be returned", expectedSeries);
        assertEquals(message, expectedSeries, seriesList.size());

        assertSampleCountMatch(seriesList, limit);

        assertFalse("All series should be for one entity", diffEntityCountGraterThan(seriesList, 1));
    }


    private boolean diffEntityCountGraterThan(List<Series> seriesList, int count) {
        Set<String> entitySet = new HashSet();
        for (Series s : seriesList) {
            entitySet.add(s.getEntity());
        }
        return entitySet.size() > count;
    }

    private void assertSampleCountMatch(List<Series> seriesList, int expectedCount) {
        for (int i = 0; i < seriesList.size(); i++) {
            assertEquals("Sample count mismatch", expectedCount, seriesList.get(i).getData().size());
        }
    }
}
