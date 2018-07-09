package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.common.InterpolationMode;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.model.series.search.SeriesSearchQuery;
import com.axibase.tsd.api.model.series.search.SeriesSearchResult;
import com.axibase.tsd.api.model.series.search.SeriesSearchResultRecord;
import com.axibase.tsd.api.util.Filter;
import com.axibase.tsd.api.util.Filters;
import com.axibase.tsd.api.util.Mocks;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.testng.Assert.*;

public class SeriesSearchTest extends SeriesMethod {
    private SeriesSearchResultRecord resultRecord1;
    private SeriesSearchResultRecord resultRecord2;
    private SeriesSearchResultRecord resultRecord3;
    private SeriesSearchResultRecord resultRecord4;
    private SeriesSearchResultRecord resultRecord5;

    private List<Filter<SeriesSearchResultRecord>> filters;

    private final String[] entityFields = {"name", "label", "interpolate", "timezone", "enabled"};
    private final String[] metricFields = {"name", "label", "interpolate", "timezone", "enabled", "description",
            "datatype", "persistent", "filter", "retentiondays", "seriesretentiondays", "versioned",
            "minvalue", "maxvalue", "invalidaction", "units"};

    @DataProvider
    public Object[][] provideSingleQueries() {
        return Filters.formatForDataProvider(filters);
    }

    @DataProvider
    public Object[][] provideDoubleQueriesAnd() {
        Collection<Filter<SeriesSearchResultRecord>> result = Filters.crossProductAnd(filters, filters);
        return Filters.formatForDataProvider(result);
    }

    @DataProvider
    public Object[][] provideDoubleQueriesOr() {
        Collection<Filter<SeriesSearchResultRecord>> result = Filters.crossProductOr(filters, filters);
        return Filters.formatForDataProvider(result);
    }

    @BeforeTest
    public void prepareData() throws Exception {
        resultRecord1 = createSeries("sst_1_");
        resultRecord2 = createSeries("sst_2_");
        resultRecord3 = createSeries("sst_11_");
        resultRecord4 = createSeries("sst_22_");
        resultRecord5 = createSeries("sst_other_");

        filters = Arrays.asList(
                new Filter<>("entity:sst_1*", resultRecord1, resultRecord3),
                new Filter<>("entity.label:sst_2*", resultRecord2, resultRecord4),
                new Filter<>("metric:sst_22*", resultRecord4),
                new Filter<>("metric.label:sst_1*", resultRecord1, resultRecord3),
                new Filter<>("sst_11_entity_tag:sst_11_entity_value", resultRecord3),
                new Filter<>("sst_other_metric_tag:sst_other_metric_value", resultRecord5)
        );

        updateSearchIndex();
    }

    private static SeriesSearchResultRecord createSeries(String prefix) throws Exception {
        Entity entity = new Entity(
                prefix + Mocks.entity(),
                Collections.singletonMap(prefix + "entity_tag", prefix + "entity_value"))
                .setLabel(prefix + Mocks.LABEL)
                .setInterpolationMode(InterpolationMode.PREVIOUS)
                .setTimeZoneID("Europe/Moscow")
                .setEnabled(true);

        Metric metric = new Metric(
                prefix + Mocks.metric(),
                Collections.singletonMap(prefix + "metric_tag", prefix + "metric_value"))
                .setLabel(prefix + Mocks.LABEL)
                .setTimeZoneID("Europe/London")
                .setEnabled(true)
                .setInterpolate(InterpolationMode.PREVIOUS)
                .setDescription(prefix + Mocks.DESCRIPTION )
                .setDataType(DataType.DECIMAL)
                .setEnabled(true)
                .setPersistent(true)
                .setFilter("name = '*'")
                .setRetentionDays(0)
                .setSeriesRetentionDays(0)
                .setVersioned(false)
                .setMinValue(new BigDecimal("1"))
                .setMaxValue(new BigDecimal("200.1"))
                .setInvalidAction("TRANSFORM")
                .setUnits("kg");

        Map<String, String> tags = Collections.singletonMap(prefix + "tag", prefix + "value");

        Series series = new Series(entity.getName(), metric.getName(), tags);
        series.addSamples(Mocks.SAMPLE);

        EntityMethod.createOrReplaceEntityCheck(entity);
        MetricMethod.createOrReplaceMetricCheck(metric);
        SeriesMethod.insertSeriesCheck(series);

        return new SeriesSearchResultRecord(entity, metric, tags, 1.0);
    }

    @Issue("4404")
    @Test(description = "Test all records returned")
    public void testSearchAll() {
        Filter<SeriesSearchResultRecord> filter = new Filter<>(
                "sst_*", resultRecord1, resultRecord2, resultRecord3, resultRecord4, resultRecord5);

        testQuery(filter);
    }

    @Issue("4404")
    @Test(description = "Test all fields returned")
    public void testAllFields() {
        Entity expectedEntity = EntityMethod.getEntity(resultRecord4.getEntity().getName());
        Metric expectedMetric = MetricMethod.getMetric(resultRecord4.getMetric().getName());

        SeriesSearchQuery query = new SeriesSearchQuery("sst_22*");
        query.addEntityFields("*");
        query.addEntityTags("*");

        query.addMetricFields("*");
        query.addMetricTags("*");

        SeriesSearchResult result = SeriesMethod.searchSeries(query);
        SeriesSearchResultRecord[] resultRecords = result.getData();
        assertTrue(
                resultRecords != null && resultRecords.length == 1,
                "Incorrect series count");
        SeriesSearchResultRecord resultRecord = resultRecords[0];
        Entity resultEntity = resultRecord.getEntity();
        Metric resultMetric = resultRecord.getMetric();

        assertEquals(resultEntity, expectedEntity);
        assertEquals(resultMetric, expectedMetric);
    }

    @Issue("4404")
    @Test(dataProvider = "provideSingleQueries",
            description = "Test filter by every field is working")
    public void testSingleQueries(Filter<SeriesSearchResultRecord> filter) {
        testQuery(filter);
    }

    @Issue("4404")
    @Test(dataProvider = "provideDoubleQueriesAnd",
            description = "Test filter with complex condition (AND) is working")
    public void testDoubleQueriesAnd(Filter<SeriesSearchResultRecord> filter) {
        testQuery(
                new Filter<>(
                        String.format("(%1$s) OR (%1$s)", filter.getExpression()),
                        filter.getExpectedResultSet()));
    }

    @Issue("4404")
    @Test(dataProvider = "provideDoubleQueriesOr",
            description = "Test filter with complex condition (OR) is working")
    public void testDoubleQueriesOr(Filter<SeriesSearchResultRecord> filter) {
        testQuery(
                new Filter<>(
                        String.format("(%1$s) AND (%1$s)", filter.getExpression()),
                        filter.getExpectedResultSet()));
    }

    @Issue("4404")
    @Test(description = "Test limit and offset options are working")
    public void testLimitOffset() {
        SeriesSearchQuery query = new SeriesSearchQuery("sst_*");
        query.addEntityFields(entityFields);
        query.addEntityTags("*");

        query.addMetricFields(metricFields);
        query.addMetricTags("*");

        query.setLimit(2);
        query.setOffset(1);

        Set<SeriesSearchResultRecord> result = Sets.newHashSet(Arrays.asList(resultRecord2, resultRecord3));

        checkQueryWithoutRelevance(query, result);
    }

    @Issue("4404")
    @Test(description = "Test explicitly requested fields and tags are returned")
    public void testFieldsTags() {
        SeriesSearchQuery query = new SeriesSearchQuery("sst_1*");
        query.addEntityFields("interpolate", "timezone");
        query.addEntityTags("sst_11_entity_tag");

        query.addMetricFields("datatype");
        query.addMetricTags("sst_11_metric_tag");

        Entity entity1 = new Entity()
                .setName(resultRecord1.getEntity().getName())
                .setLabel(resultRecord1.getEntity().getLabel())
                .setInterpolationMode(resultRecord1.getEntity().getInterpolationMode())
                .setTimeZoneID(resultRecord1.getEntity().getTimeZoneID());

        Metric metirc1 = new Metric()
                .setName(resultRecord1.getMetric().getName())
                .setLabel(resultRecord1.getMetric().getLabel())
                .setDataType(resultRecord1.getMetric().getDataType());

        SeriesSearchResultRecord expectedResult1 = new SeriesSearchResultRecord(
                entity1,
                metirc1,
                resultRecord1.getSeriesTags(),
                1.0);

        Entity entity2 = new Entity()
                .setName(resultRecord3.getEntity().getName())
                .setLabel(resultRecord3.getEntity().getLabel())
                .setInterpolationMode(resultRecord3.getEntity().getInterpolationMode())
                .setTimeZoneID(resultRecord3.getEntity().getTimeZoneID())
                .setTags(resultRecord3.getEntity().getTags());

        Metric metirc2 = new Metric()
                .setName(resultRecord3.getMetric().getName())
                .setLabel(resultRecord3.getMetric().getLabel())
                .setDataType(resultRecord3.getMetric().getDataType())
                .setTags(resultRecord3.getMetric().getTags());

        SeriesSearchResultRecord expectedResult3 = new SeriesSearchResultRecord(
                entity2,
                metirc2,
                resultRecord3.getSeriesTags(),
                1.0);

        SeriesSearchResultRecord[] expectedResult = {
                expectedResult1,
                expectedResult3
        };

        checkQueryWithoutRelevance(query, Sets.newHashSet(Arrays.asList(expectedResult)));
    }

    @Issue("4666")
    @Test(description = "Test createdDate and lastInsertDate fields format")
    public void testDatePrecision() throws IOException {
        SeriesSearchQuery query = new SeriesSearchQuery("sst_22*");
        query.addEntityFields("createdDate,lastInsertDate");
        query.addMetricFields("createdDate,lastInsertDate");

        Response response = SeriesMethod.searchRawSeries(query);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode resultNode = mapper.readTree(response.readEntity(String.class));
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
        List<JsonNode> dateNodes = new ArrayList<>();
        dateNodes.addAll(resultNode.findValues("createdDate"));
        dateNodes.addAll(resultNode.findValues("lastInsertDate"));

        assertEquals(dateNodes.size(), 4, "Incorrect fields count");
        for (JsonNode node : dateNodes) {
            try {
                dateFormat.parse(node.asText());
            } catch (ParseException e) {
                fail("Incorrect date format", e);
            }
        }
    }

    private void testQuery(Filter<SeriesSearchResultRecord> filter) {
        SeriesSearchQuery query = new SeriesSearchQuery(filter.getExpression());
        query.addEntityFields(entityFields);
        query.addEntityTags("*");

        query.addMetricFields(metricFields);
        query.addMetricTags("*");

        checkQueryWithoutRelevance(query, filter.getExpectedResultSet());
    }

    private static void checkQueryWithoutRelevance(SeriesSearchQuery query, Set<SeriesSearchResultRecord> expectedResult) {
        SeriesSearchResult result = searchSeries(query);
        HashMap<String, SeriesSearchResultRecord> actualRecords = new HashMap<>();
        for (SeriesSearchResultRecord resultRecord : result.getData()) {
            actualRecords.put(resultRecord.getEntity().getName(), resultRecord);
        }

        assertEquals(
                expectedResult.size(),
                actualRecords.size(),
                "Expected and actual result sets has different sizes");

        for (SeriesSearchResultRecord expectedRecord : expectedResult) {
            SeriesSearchResultRecord actualRecord = actualRecords.get(expectedRecord.getEntity().getName());
            assertNotNull(actualRecord, "Requested series not found");
            assertEquals(actualRecord.getEntity(), expectedRecord.getEntity(), "Entities are different");
            assertEquals(actualRecord.getMetric(), expectedRecord.getMetric(), "Metrics are different");
            assertEquals(actualRecord.getSeriesTags(), expectedRecord.getSeriesTags(), "Tags are different");
        }
    }
}
