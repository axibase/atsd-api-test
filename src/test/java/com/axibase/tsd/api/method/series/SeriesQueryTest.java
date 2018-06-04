package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.PeriodAlignment;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.query.Interval;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolate;
import com.axibase.tsd.api.model.series.query.transformation.AggregationInterpolateType;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.Interpolate;
import com.axibase.tsd.api.model.series.query.transformation.interpolate.InterpolateFunction;
import com.axibase.tsd.api.util.CommonAssertions;
import com.axibase.tsd.api.util.Filter;
import com.axibase.tsd.api.util.Mocks;
import com.google.common.collect.Sets;
import io.qameta.allure.Issue;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.*;

import static com.axibase.tsd.api.util.ErrorTemplate.AGGREGATE_NON_DETAIL_REQUIRE_PERIOD;
import static com.axibase.tsd.api.util.ErrorTemplate.INTERPOLATE_TYPE_REQUIRED;
import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;
import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.*;

public class SeriesQueryTest extends SeriesMethod {
    private final Series TEST_SERIES1 = Mocks.series();
    private final Series TEST_SERIES2 = Mocks.series();
    private final Series TEST_SERIES3 = Mocks.series();

    private Random random = new Random();
    private Calendar calendar = Calendar.getInstance();

    @BeforeClass
    public void prepare() throws Exception {
        TEST_SERIES2.setSamples(Collections.singletonList(Sample.ofDateInteger("2016-07-01T14:23:20.000Z", 1)));
        SeriesMethod.insertSeriesCheck(TEST_SERIES1, TEST_SERIES2);

        TEST_SERIES3.setSamples(Arrays.asList(
                Sample.ofDateInteger("2017-01-01T00:01:00Z", 1),
                Sample.ofDateInteger("2017-01-01T00:02:00Z", 2),
                Sample.ofDateInteger("2017-01-01T00:03:00Z", 3))
        );
        SeriesMethod.insertSeriesCheck(TEST_SERIES3);
    }

    @DataProvider(name = "datesWithTimezonesProvider")
    Object[][] provideDatesWithTimezones() {
        return new Object[][] {
                {"2016-07-01T14:23:20Z"},
                {"2016-07-01T15:46:20+01:23"},
                {"2016-07-01T15:46:20+01:23"}
        };
    }

    @Issue("2850")
    @Test(dataProvider = "datesWithTimezonesProvider")
    public void testISOTimezoneZ(String date) throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate(date);

        List<Series> storedSeries = querySeriesAsList(seriesQuery);

        assertEquals("Incorrect series entity", TEST_SERIES2.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", TEST_SERIES2.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date",
                "2016-07-01T14:23:20.000Z",
                storedSeries.get(0).getData().get(0).getRawDate());
    }

    @DataProvider(name = "incorrectDatesProvider")
    Object[][] provideIncorrectDates() {
        return new Object[][] {
                {"2016-07-01 14:23:20"},
                {"2016-07-01T15:46:20+3123"},
                {"1467383000000"}
        };
    }

    @Issue("2850")
    @Test(dataProvider = "incorrectDatesProvider")
    public void testLocalTimeUnsupported(String date) throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate(date);

        Response response = querySeries(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals(String.format("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: %s\"}", date), response.readEntity(String.class), true);

    }

    @Issue("3013")
    @Test
    public void testDateFilterRangeIsBeforeStorableRange() throws Exception {
        String entityName = "e-query-range-14";
        String metricName = "m-query-range-14";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MIN_STORABLE_DATE);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Not empty data for disjoint query and stored interval", 0, data.size());
    }

    @Issue("3013")
    @Test
    public void testDateFilterRangeIsAfterStorableRange() throws Exception {
        String entityName = "e-query-range-15";
        String metricName = "m-query-range-15";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), addOneMS(MAX_STORABLE_DATE), MAX_QUERYABLE_DATE);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Not empty data for disjoint query and stored interval", 0, data.size());
    }

    @Issue("3013")
    @Test
    public void testDateFilterRangeIncludesStorableRange() throws Exception {
        String entityName = "e-query-range-16";
        String metricName = "m-query-range-16";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that contains stored interval", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getRawDate());
        CommonAssertions.assertDecimals("Incorrect stored value", v, data.get(0).getValue());
    }

    @Issue("3013")
    @Test
    public void testDateFilterRangeIntersectsStorableRangeBeginning() throws Exception {
        String entityName = "e-query-range-17";
        String metricName = "m-query-range-17";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, addOneMS(MIN_STORABLE_DATE));
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that intersects stored interval from left", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getRawDate());
        CommonAssertions.assertDecimals("Incorrect stored value", v, data.get(0).getValue());
    }

    @Issue("3013")
    @Test
    public void testDateFilterRangeIntersectsStorableRangeEnding() throws Exception {
        String entityName = "e-query-range-18";
        String metricName = "m-query-range-18";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addSamples(Sample.ofDateDecimal(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_STORABLE_DATE, MAX_QUERYABLE_DATE);
        List<Sample> data = querySeriesAsList(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that intersects stored interval from right", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getRawDate());
        assertTrue("Incorrect stored value", v.compareTo(data.get(0).getValue()) == 0);
    }

    @Issue("3043")
    @Test
    public void testEveryDayFrom1969ToMinStorableDateFailToInsert() throws Exception {
        Series series = new Series("e-query-range-19", "m-query-range-19");
        BigDecimal v = new BigDecimal("7");

        calendar.setTime(parseDate("1969-01-01T00:00:00.000Z"));
        Date endDate = parseDate(MIN_STORABLE_DATE);

        while (calendar.getTime().before(endDate)) {
            series.addSamples(Sample.ofDateDecimal(ISOFormat(calendar.getTime()), v));
            Response response = insertSeries(Collections.singletonList(series));

            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    BAD_REQUEST.getStatusCode(), response.getStatusInfo().getStatusCode());
            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    "{\"error\":\"IllegalArgumentException: Negative timestamp\"}", response.readEntity(String.class));

            setRandomTimeDuringNextDay(calendar);
        }
    }

    @Issue("3043")
    @Test
    public void testEveryDayFromMinToMaxStorableDateCorrectlySaved() throws Exception {
        Series series = new Series("e-query-range-20", "m-query-range-20");
        BigDecimal v = new BigDecimal("8");

        calendar.setTime(parseDate(MIN_STORABLE_DATE));
        Date maxStorableDay = parseDate(MAX_STORABLE_DATE);

        while (calendar.getTime().before(maxStorableDay)) {
            series.addSamples(Sample.ofDateDecimal(ISOFormat(calendar.getTime()), v));
            setRandomTimeDuringNextDay(calendar);
        }
        series.addSamples(Sample.ofDateDecimal(MAX_STORABLE_DATE, v));
        insertSeriesCheck(Collections.singletonList(series));
    }

    @Issue("3043")
    @Test
    public void testEveryDayFromMaxStorableDateTo2110FailToInsert() throws Exception {
        Series series = new Series("e-query-range-21", "m-query-range-21");
        BigDecimal v = new BigDecimal("9");

        calendar.setTime(parseDate(addOneMS(MAX_STORABLE_DATE)));
        Date endDate = parseDate("2110-01-01T00:00:00.000Z");

        while (calendar.getTime().before(endDate)) {
            series.addSamples(Sample.ofDateDecimal(ISOFormat(calendar.getTime()), v));
            Response response = insertSeries(Collections.singletonList(series));

            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    BAD_REQUEST.getStatusCode(), response.getStatusInfo().getStatusCode());
            assertTrue("Attempt to insert date before min storable date doesn't return error",
                    response.readEntity(String.class).startsWith("{\"error\":\"IllegalArgumentException: Too large timestamp"));

            setRandomTimeDuringNextDay(calendar);
        }
    }

    @Issue("2979")
    @Test
    public void testEntitesExpressionStarChar() throws Exception {
        Series series = new Series("e-query-wildcard-22-1", "m-query-wildcard-22");
        series.addSamples(Sample.ofDateInteger("2010-01-01T00:00:00.000Z", 0));
        insertSeriesCheck(Collections.singletonList(series));

        Map<String, Object> query = new HashMap<>();
        query.put("metric", series.getMetric());
        query.put("entities", "e-query-wildcard-22*");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        final String given = querySeries(query).readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        assertTrue(compareJsonString(expected, given));
    }

    @Issue("2979")
    @Test
    public void testEntitesExpressionQuestionChar() throws Exception {
        Series series = new Series("e-query-wildcard-23-1", "m-query-wildcard-23");
        series.addSamples(Sample.ofDateInteger("2010-01-01T00:00:00.000Z", 0));
        insertSeriesCheck(Collections.singletonList(series));

        Map<String, Object> query = new HashMap<>();
        query.put("metric", series.getMetric());
        query.put("entities", "e-query-wildcard-23-?");
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);

        final String given = querySeries(query).readEntity(String.class);
        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        assertTrue(compareJsonString(expected, given));
    }

    @Issue("2970")
    @Test
    public void testVersionedLimitSupport() throws Exception {
        Series series = new Series("e-query-v-l-24", "m-query-v-l-24");
        final int limitValue = 2;
        Metric versionedMetric = new Metric();
        versionedMetric.setName(series.getMetric());
        versionedMetric.setVersioned(true);

        MetricMethod.createOrReplaceMetric(versionedMetric);

        series.addSamples(
                Sample.ofDateInteger(MIN_STORABLE_DATE, 0),
                Sample.ofDateInteger(addOneMS(MIN_STORABLE_DATE), 1),
                Sample.ofDateInteger(addOneMS(addOneMS(MIN_STORABLE_DATE)), 2)
        );
        insertSeriesCheck(Collections.singletonList(series));

        Map<String, Object> query = new HashMap<>();
        query.put("entity", series.getEntity());
        query.put("metric", series.getMetric());
        query.put("startDate", MIN_QUERYABLE_DATE);
        query.put("endDate", MAX_QUERYABLE_DATE);
        query.put("versioned", true);
        query.put("limit", limitValue);

        final Response response = querySeries(query);
        JSONArray jsonArray = new JSONArray(response.readEntity(String.class));
        final String assertMessage = String.format("Response should contain only %d samples", limitValue);
        assertEquals(assertMessage, limitValue, calculateJsonArraySize(((JSONObject) jsonArray.get(0)).getString("data")));
    }

    @Issue("3030")
    @Test
    public void testDateIntervalFieldEnoughToDetail() throws Exception {
        Series series = new Series("entity-query-24", "metric-query-24");
        series.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 1));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        query.setInterval(new Interval(99999, TimeUnit.QUARTER));

        List<Series> storedSeries = querySeriesAsList(query);

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = jacksonMapper.writeValueAsString(storedSeries);
        assertTrue("Stored series does not match to inserted", compareJsonString(expected, given));
    }

    @Issue("3030")
    @Test
    public void testDateIntervalFieldEnoughToGroup() throws Exception {
        Series series = new Series("entity-query-25", "metric-query-25");
        series.addSamples(Sample.ofDateInteger(MIN_STORABLE_DATE, 1));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        query.setInterval(new Interval(99999, TimeUnit.QUARTER));

        query.setGroup(new Group(GroupType.SUM));

        List<Series> storedSeries = querySeriesAsList(query);

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = jacksonMapper.writeValueAsString(storedSeries);
        assertTrue("Stored series does not match to inserted", compareJsonString(expected, given));
    }

    @Issue("3030")
    @Test
    public void testDateIntervalFieldEnoughToAggregate() throws Exception {
        final BigDecimal VALUE = new BigDecimal("1.0");
        Series series = new Series("entity-query-26", "metric-query-26");
        series.addSamples(Sample.ofDateDecimal("2014-01-01T00:00:00.000Z", VALUE));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        query.setInterval(new Interval(99999, TimeUnit.QUARTER));

        Period period = new Period(99999, TimeUnit.QUARTER, PeriodAlignment.START_TIME);
        query.setAggregate(new Aggregate(AggregationType.SUM, period));


        List<Series> storedSeries = querySeriesAsList(query);
        assertEquals("Response should contain only one series", 1, storedSeries.size());
        List<Sample> data = storedSeries.get(0).getData();
        assertEquals("Response should contain only one sample", 1, data.size());
        assertEquals("Returned value does not match to expected SUM", VALUE, data.get(0).getValue());
    }

    @Issue("3324")
    @Test
    public void testAggregateInterpolateNoTypeRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mock-entity", "mock-metric", MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        Aggregate aggregate = new Aggregate(AggregationType.SUM, new Period(99999, TimeUnit.QUARTER));
        aggregate.setInterpolate(new AggregationInterpolate());

        query.setAggregate(aggregate);

        Response response = querySeries(query);

        assertEquals("Query with interpolation but without type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", INTERPOLATE_TYPE_REQUIRED, extractErrorMessage(response));
    }


    @Issue("3324")
    @Test
    public void testGroupInterpolateNoTypeRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mock-entity", "mock-metric", MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        Group group = new Group(GroupType.SUM, new Period(99999, TimeUnit.QUARTER));
        group.setInterpolate(new AggregationInterpolate());

        query.setGroup(group);

        Response response = querySeries(query);

        assertEquals("Query with interpolation but without type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", INTERPOLATE_TYPE_REQUIRED, extractErrorMessage(response));
    }

    @Issue("3324")
    @Test
    public void testAggregateNoPeriodRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mock-entity", "mock-metric", MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        query.setAggregate(new Aggregate(AggregationType.SUM));

        Response response = querySeries(query);

        assertEquals("Aggregate query without period should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", String.format(AGGREGATE_NON_DETAIL_REQUIRE_PERIOD, query.getAggregate().getType()), extractErrorMessage(response));
    }

    @DataProvider(name = "dataTextProvider")
    Object[][] provideDataText() {
        return new Object[][] {
                {"hello"},
                {"HelLo"},
                {"Hello World"},
                {"spaces      \t\t\t afeqf everywhere"},
                {"Кириллица"},
                {"猫"},
                {"Multi\nline"},
                {null},
                {"null"},
                {"\"null\""},
                {"true"},
                {"\"true\""},
                {"11"},
                {"0"},
                {"0.1"},
                {"\"0.1\""},
                {"\"+0.1\""},
                {""}
        };
    }

    @Issue("3480")
    @Test(dataProvider = "dataTextProvider")
    public void testXTextField(String text) throws Exception {
        String entityName = entity();
        String metricName = metric();

        String largeNumber = "10.1";
        Series series = new Series(entityName, metricName);
        Sample sample = Sample.ofDateDecimalText(MIN_STORABLE_DATE, new BigDecimal(largeNumber), text);
        series.addSamples(sample);
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series);
        List<Series> seriesList = querySeriesAsList(seriesQuery);

        assertEquals("Stored series are incorrect", Collections.singletonList(series), seriesList);
    }

    @Issue("3480")
    @Test
    public void testXTextFieldLastVersion() throws Exception {
        String entityName = "e-text-overwritten-versioning-1";
        String metricName = "m-text-overwritten-versioning-1";

        Series series = new Series(entityName, metricName);

        Metric metric = new Metric(metricName);
        metric.setVersioned(true);
        MetricMethod.createOrReplaceMetricCheck(metric);

        String[] data = new String[] {"1", "2"};
        for (String x : data) {
            Sample sample = Sample.ofDateIntegerText("2016-10-11T13:00:00.000Z", 1, x);
            series.setSamples(Collections.singleton(sample));
            insertSeriesCheck(Collections.singletonList(series));
        }

        SeriesQuery seriesQuery = new SeriesQuery(series);
        List<Series> seriesList = querySeriesAsList(seriesQuery);

        assertFalse("No series", seriesList.isEmpty());
        assertFalse("No series data", seriesList.get(0).getData().isEmpty());
        String received = seriesList.get(0).getData().get(0).getText();
        assertEquals("Last version of text field incorrect", data[data.length - 1], received);
    }

    @Issue("3770")
    @Test
    public void testExactMatchIgnoresReservedVersioningTags() throws Exception {
        String metricName = metric();
        Metric metric = new Metric(metricName);
        metric.setVersioned(true);

        final int insertedVersionsCount = 3;
        Series series = new Series(entity(), metricName);

        MetricMethod.createOrReplaceMetricCheck(metric);

        for (int i = 0; i < insertedVersionsCount; i++) {
            series.setSamples(Collections.singleton(Sample.ofDateInteger(Mocks.ISO_TIME, i)));
            SeriesMethod.insertSeriesCheck(Collections.singletonList(series));
        }

        SeriesQuery query = new SeriesQuery(series);
        query.setVersioned(true);
        query.setExactMatch(true);
        List<Series> receivedSeries = querySeriesAsList(query);
        int receivedVersionsCount = receivedSeries.get(0).getData().size();

        assertEquals("Number of received versions mismatched", insertedVersionsCount, receivedVersionsCount);
    }

    @Test
    public void testSeriesQueryWithTextSample() throws Exception {
        Series series = Mocks.series();
        series.setSamples(Collections.singleton(Mocks.TEXT_SAMPLE));
        SeriesMethod.insertSeriesCheck(series);

        List<Series> resultSeriesList = SeriesMethod.querySeriesAsList(new SeriesQuery(series));

        String assertMessage = "SeriesList serialized as not expected!";
        assertEquals(assertMessage, Collections.singletonList(series), resultSeriesList);
    }

    @Issue("3860")
    @Test
    public void testLastSeriesWithText() throws Exception {

        Series series = Mocks.series();
        series.setSamples(Collections.singleton(Mocks.TEXT_SAMPLE));
        SeriesMethod.insertSeriesCheck(series);

        SeriesQuery query = new SeriesQuery(series);
        query.setLimit(1);

        List<Series> resultSeriesList = SeriesMethod.querySeriesAsList(query);
        assertEquals("Response doesn't match the expected", Collections.singletonList(series), resultSeriesList);
    }

    @DataProvider
    public Object[][] provideTagFilters() {
        return new Object[][] {
                {new Filter<Series>("", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": null", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": {}", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": {\"a\": null}", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": {\"tag\": null}", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": {\"a\": \"b\"}")},
                {new Filter<Series>("\"tags\": {\"tag\": \"b\"}")},
                {new Filter<Series>("\"tags\": {\"tag\": \"value\"}", TEST_SERIES1)},
                {new Filter<Series>("\"tags\": {\"tag\": \"value\", \"a\": \"b\"}")},
                {new Filter<Series>("\"exactMatch\": true")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": null")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {}")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"a\": null}")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"tag\": null}")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"a\": \"b\"}")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"tag\": \"b\"}")},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"tag\": \"value\"}", TEST_SERIES1)},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"tag\": \"value\", \"a\": null}", TEST_SERIES1)},
                {new Filter<Series>("\"exactMatch\": true, \"tags\": {\"tag\": \"value\", \"a\": \"b\"}")}
        };
    }

    @Issue("4670")
    @Test(
            dataProvider = "provideTagFilters",
            description = "test series query with tag filter")
    public void testTagFilter(Filter<Series> filter) {
        String filterExpression = StringUtils.isEmpty(filter.getExpression())
                ? ""
                : "," + filter.getExpression();

        String payload = String.format("{ " +
                        "\"startDate\": \"2015-10-31T07:00:00Z\"," +
                        "\"endDate\": \"2017-10-31T08:00:00Z\"," +
                        "\"entity\": \"%s\"," +
                        "\"metric\": \"%s\"" +
                        "%s" +
                        "}",
                TEST_SERIES1.getEntity(),
                TEST_SERIES1.getMetric(),
                filterExpression);

        Response response = SeriesMethod.querySeries(payload);
        Series[] result = response.readEntity(Series[].class);
        List<Sample> samples = result[0].getData();
        assertEquals("Incorrect series count",
                filter.getExpectedResultSet().size(),
                samples.size());
        if (filter.getExpectedResultSet().size() > 0) {
            assertEquals("Incorrect samples", TEST_SERIES1.getData(), samples);
        }
    }

    @Issue("4670")
    @Test(description = "test series query without tag filter with tag expression")
    public void testSeriesQueryWithoutTagsWithTagExpression() {
        SeriesQuery query = new SeriesQuery(
                TEST_SERIES1.getEntity(), TEST_SERIES1.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        query.setTags(null);
        query.setTagExpression("tags.tag LIKE '*'");

        List<Series> result = SeriesMethod.querySeriesAsList(query);

        assertEquals("Incorrect result in query without tags", 1, result.size());
        assertEquals("Incorrect series result in query without tags", TEST_SERIES1, result.get(0));
    }

    @Issue("4670")
    @Test(description = "test series query with incorrect tag filter")
    public void testTagFilterIncorrectSyntax() {
        String payload = String.format("{ " +
                        "\"startDate\": \"2015-10-31T07:00:00Z\"," +
                        "\"endDate\": \"2017-10-31T08:00:00Z\"," +
                        "\"entity\": \"%s\"," +
                        "\"metric\": \"%s\"" +
                        "\"tags\": []," +
                        "}",
                TEST_SERIES1.getEntity(),
                TEST_SERIES1.getMetric());

        Response response = SeriesMethod.querySeries(payload);
        assertEquals("Incorrect status code", response.getStatus(), BAD_REQUEST.getStatusCode());
    }

    @Issue("4714")
    @Test(description = "test same double series query")
    public void testSameDoubleSeriesQuery() throws JSONException {
        SeriesQuery query = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query.setStartDate("2017-01-01T00:01:00Z");
        query.setEndDate("2017-01-01T00:04:00Z");

        CommonAssertions.jsonAssert(
                Arrays.asList(TEST_SERIES3, TEST_SERIES3),
                SeriesMethod.querySeries(query, query)
        );
    }

    @Issue("4714")
    @Test(description = "test same double series query")
    public void testSameDoubleSeriesQueryWithAggregation() {
        SeriesQuery query = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query.setStartDate("2017-01-01T00:01:00Z");
        query.setEndDate("2017-01-01T00:04:00Z");
        query.setAggregate(new Aggregate(
                AggregationType.SUM,
                new Period(3, TimeUnit.MINUTE, PeriodAlignment.START_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query, query);

        Series expectedSeries = new Series();
        expectedSeries.setEntity(TEST_SERIES3.getEntity());
        expectedSeries.setMetric(TEST_SERIES3.getMetric());
        expectedSeries.setTags(Mocks.TAGS);
        expectedSeries.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("6.0"))
        );

        assertEquals(
                "Incorrect query result with two same series requests",
                Arrays.asList(expectedSeries, expectedSeries),
                result);
    }

    @Issue("4714")
    @Test(description = "test double series query with different transformation")
    public void testDoubleSeriesQueryDifferentAggregation() {
        SeriesQuery query1 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query1.setStartDate("2017-01-01T00:01:00Z");
        query1.setEndDate("2017-01-01T00:04:00Z");
        query1.setAggregate(new Aggregate(
                AggregationType.MAX,
                new Period(120, TimeUnit.SECOND, PeriodAlignment.START_TIME)));

        SeriesQuery query2 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query2.setStartDate("2017-01-01T00:01:00Z");
        query2.setEndDate("2017-01-01T00:04:00Z");
        query2.setAggregate(new Aggregate(
                AggregationType.AVG,
                new Period(3, TimeUnit.MINUTE, PeriodAlignment.START_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query1, query2);

        Series expectedSeries1 = new Series();
        expectedSeries1.setEntity(TEST_SERIES3.getEntity());
        expectedSeries1.setMetric(TEST_SERIES3.getMetric());
        expectedSeries1.setTags(Mocks.TAGS);
        expectedSeries1.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        Series expectedSeries2 = new Series();
        expectedSeries2.setEntity(TEST_SERIES3.getEntity());
        expectedSeries2.setMetric(TEST_SERIES3.getMetric());
        expectedSeries2.setTags(Mocks.TAGS);
        expectedSeries2.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("2.0"))
        );

        assertEquals(
                "Incorrect query result with two series requests with different transformation",
                Sets.newHashSet(expectedSeries1, expectedSeries2),
                Sets.newHashSet(result));
    }

    @Issue("4714")
    @Test(description = "test double series query with different transformation period")
    public void testDoubleSeriesQueryDifferentAggregationPeriod() {
        SeriesQuery query1 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query1.setStartDate("2017-01-01T00:01:00Z");
        query1.setEndDate("2017-01-01T00:04:00Z");
        query1.setAggregate(new Aggregate(
                AggregationType.MAX,
                new Period(2, TimeUnit.MINUTE, PeriodAlignment.START_TIME)));

        SeriesQuery query2 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query2.setStartDate("2017-01-01T00:01:00Z");
        query2.setEndDate("2017-01-01T00:04:00Z");
        query2.setAggregate(new Aggregate(
                AggregationType.MAX,
                new Period(60, TimeUnit.SECOND, PeriodAlignment.START_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query1, query2);

        Series expectedSeries1 = new Series();
        expectedSeries1.setEntity(TEST_SERIES3.getEntity());
        expectedSeries1.setMetric(TEST_SERIES3.getMetric());
        expectedSeries1.setTags(Mocks.TAGS);
        expectedSeries1.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("1.0")),
                Sample.ofDateDecimal("2017-01-01T00:02:00Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        Series expectedSeries2 = new Series();
        expectedSeries2.setEntity(TEST_SERIES3.getEntity());
        expectedSeries2.setMetric(TEST_SERIES3.getMetric());
        expectedSeries2.setTags(Mocks.TAGS);
        expectedSeries2.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        assertEquals(
                "Incorrect query result with two series requests with different transformation period",
                Sets.newHashSet(expectedSeries1, expectedSeries2),
                Sets.newHashSet(result));
    }

    @Issue("4714")
    @Test(description = "test double series query with different transformation")
    public void testDoubleSeriesQueryDifferentAggregationInSingleQuery() {
        SeriesQuery query1 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query1.setStartDate("2017-01-01T00:01:00Z");
        query1.setEndDate("2017-01-01T00:04:00Z");

        Aggregate aggregate = new Aggregate();
        aggregate.addType(AggregationType.MIN);
        aggregate.addType(AggregationType.COUNTER);
        aggregate.setPeriod(new Period(2, TimeUnit.MINUTE, PeriodAlignment.START_TIME));
        query1.setAggregate(aggregate);

        List<Series> result = SeriesMethod.querySeriesAsList(query1);

        Series expectedSeries1 = new Series();
        expectedSeries1.setEntity(TEST_SERIES3.getEntity());
        expectedSeries1.setMetric(TEST_SERIES3.getMetric());
        expectedSeries1.setTags(Mocks.TAGS);
        expectedSeries1.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("1.0")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        Series expectedSeries2 = new Series();
        expectedSeries2.setEntity(TEST_SERIES3.getEntity());
        expectedSeries2.setMetric(TEST_SERIES3.getMetric());
        expectedSeries2.setTags(Mocks.TAGS);
        expectedSeries2.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("1.0")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("1.0"))
        );

        assertEquals(
                "Incorrect query result with two series requests with different transformation",
                Sets.newHashSet(expectedSeries1, expectedSeries2),
                Sets.newHashSet(result));
    }

    @Issue("4714")
    @Test(description = "test double series query with different transformation period align")
    public void testDoubleSeriesQueryDifferentAggregationPeriodAlign() {
        SeriesQuery query1 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query1.setStartDate("2016-12-31T00:01:00Z");
        query1.setEndDate("2017-01-01T00:04:00Z");
        query1.setAggregate(new Aggregate(
                AggregationType.AVG,
                new Period(2, TimeUnit.MINUTE, PeriodAlignment.START_TIME)));

        SeriesQuery query2 = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query2.setStartDate("2016-12-31T00:00:00Z");
        query2.setEndDate("2017-01-01T00:04:00Z");
        query2.setAggregate(new Aggregate(
                AggregationType.AVG,
                new Period(2, TimeUnit.MINUTE, PeriodAlignment.FIRST_VALUE_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query1, query2);

        Series expectedSeries1 = new Series();
        expectedSeries1.setEntity(TEST_SERIES3.getEntity());
        expectedSeries1.setMetric(TEST_SERIES3.getMetric());
        expectedSeries1.setTags(Mocks.TAGS);
        expectedSeries1.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("1.5")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        Series expectedSeries2 = new Series();
        expectedSeries2.setEntity(TEST_SERIES3.getEntity());
        expectedSeries2.setMetric(TEST_SERIES3.getMetric());
        expectedSeries2.setTags(Mocks.TAGS);
        expectedSeries2.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:00Z", new BigDecimal("1.5")),
                Sample.ofDateDecimal("2017-01-01T00:03:00Z", new BigDecimal("3.0"))
        );

        assertEquals(
                "Incorrect query result with two series requests with different transformation period align",
                2,
                result.size());
        assertEquals(
                "Incorrect query result with two series requests with different transformation period align",
                Sets.newHashSet(expectedSeries1, expectedSeries2),
                Sets.newHashSet(result));
    }

    @Issue("4867")
    @Test(description = "test END_TIME period align aggregation")
    public void testEndTimeAggregation() {
        SeriesQuery query = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query.setStartDate("2017-01-01T00:00:50Z");
        query.setEndDate("2017-01-01T00:03:30Z");
        query.setAggregate(new Aggregate(
                AggregationType.MAX,
                new Period(1, TimeUnit.MINUTE, PeriodAlignment.END_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query);

        Series expectedSeries = new Series();
        expectedSeries.setEntity(TEST_SERIES3.getEntity());
        expectedSeries.setMetric(TEST_SERIES3.getMetric());
        expectedSeries.setTags(Mocks.TAGS);
        expectedSeries.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:30Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:02:30Z", new BigDecimal("3.0"))
        );

        assertEquals(
                "Incorrect query result with END_TIME period align aggregation",
                1,
                result.size());
        assertEquals(
                "Incorrect query result with END_TIME period align aggregation",
                expectedSeries,
                result.get(0));
    }

    @Issue("4867")
    @Test(description = "test END_TIME period align aggregation with interpolation")
    public void testEndTimeAggregationWithInterpolation() {
        SeriesQuery query = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query.setStartDate("2017-01-01T00:00:50Z");
        query.setEndDate("2017-01-01T00:03:30Z");
        Aggregate aggregate = new Aggregate(AggregationType.MAX,
                new Period(1, TimeUnit.MINUTE, PeriodAlignment.END_TIME));
        aggregate.setInterpolate(new AggregationInterpolate(AggregationInterpolateType.PREVIOUS));
        query.setAggregate(aggregate);

        List<Series> result = SeriesMethod.querySeriesAsList(query);

        Series expectedSeries = new Series();
        expectedSeries.setEntity(TEST_SERIES3.getEntity());
        expectedSeries.setMetric(TEST_SERIES3.getMetric());
        expectedSeries.setTags(Mocks.TAGS);
        expectedSeries.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:30Z", new BigDecimal("2.0")),
                Sample.ofDateDecimal("2017-01-01T00:02:30Z", new BigDecimal("3.0"))
        );

        assertEquals(
                "Incorrect query result with END_TIME period align aggregation with interpolation",
                1,
                result.size());
        assertEquals(
                "Incorrect query result with END_TIME period align aggregation with interpolation",
                expectedSeries,
                result.get(0));
    }

    @Issue("4867")
    @Test(description = "test END_TIME period align interpolation")
    public void testEndTimeInterpolation() {
        SeriesQuery query = new SeriesQuery(TEST_SERIES3.getEntity(), TEST_SERIES3.getMetric());
        query.setStartDate("2017-01-01T00:00:50Z");
        query.setEndDate("2017-01-01T00:03:30Z");
        query.setInterpolate(new Interpolate(
                InterpolateFunction.LINEAR,
                new Period(1, TimeUnit.MINUTE, PeriodAlignment.END_TIME)));

        List<Series> result = SeriesMethod.querySeriesAsList(query);

        Series expectedSeries = new Series();
        expectedSeries.setEntity(TEST_SERIES3.getEntity());
        expectedSeries.setMetric(TEST_SERIES3.getMetric());
        expectedSeries.setTags(Mocks.TAGS);
        expectedSeries.addSamples(
                Sample.ofDateDecimal("2017-01-01T00:01:30Z", new BigDecimal("1.5")),
                Sample.ofDateDecimal("2017-01-01T00:02:30Z", new BigDecimal("2.5"))
        );

        assertEquals(
                "Incorrect query result with END_TIME period align interpolation",
                1,
                result.size());
        assertEquals(
                "Incorrect query result with END_TIME period align interpolation",
                expectedSeries,
                result.get(0));
    }

    private void setRandomTimeDuringNextDay(Calendar calendar) {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
        calendar.set(Calendar.MINUTE, random.nextInt(60));
    }

    private SeriesQuery buildQuery() {
        SeriesQuery seriesQuery = new SeriesQuery();
        seriesQuery.setEntity(TEST_SERIES2.getEntity());
        seriesQuery.setMetric(TEST_SERIES2.getMetric());

        seriesQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        return seriesQuery;
    }
}