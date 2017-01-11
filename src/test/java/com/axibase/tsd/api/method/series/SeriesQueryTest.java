package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.model.Interval;
import com.axibase.tsd.api.model.TimeUnit;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.*;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import org.json.JSONArray;
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
import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.*;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.*;

public class SeriesQueryTest extends SeriesMethod {
    private static final String sampleDate = "2016-07-01T14:23:20.000Z";
    private static final Series series;

    static {
        series = new Series("series-query-e-1", "series-query-m-1");
        series.addData(new Sample(sampleDate, "1"));
    }

    private final Random random = new Random();
    private Calendar calendar = Calendar.getInstance();

    @BeforeClass
    public static void prepare() throws Exception {
        try {
            insertSeriesCheck(Collections.singletonList(series));
        } catch (Exception e) {
            fail("Can not store common dataset");
        }
    }


    /**
     * #2850
     */
    @Test
    public void testISOTimezoneZ() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T14:23:20Z");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezonePlusHoursMinutes() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T15:46:20+01:23");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }

    /**
     * #2850
     */
    @Test
    public void testISOTimezoneMinusHoursMinutes() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T13:00:20-01:23");

        List<Series> storedSeries = executeQueryReturnSeries(seriesQuery);

        assertEquals("Incorrect series entity", series.getEntity(), storedSeries.get(0).getEntity());
        assertEquals("Incorrect series metric", series.getMetric(), storedSeries.get(0).getMetric());
        assertEquals("Incorrect series sample date", sampleDate, storedSeries.get(0).getData().get(0).getD());
    }


    /**
     * #2850
     */
    @Test
    public void testLocalTimeUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01 14:23:20");

        Response response = querySeries(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01 14:23:20\"}", response.readEntity(String.class), true);

    }

    /**
     * #2850
     */
    @Test
    public void testXXTimezoneUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("2016-07-01T15:46:20+0123");

        Response response = querySeries(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 2016-07-01T15:46:20+0123\"}", response.readEntity(String.class), true);

    }

    /**
     * #2850
     */
    @Test
    public void testMillisecondsUnsupported() throws Exception {
        SeriesQuery seriesQuery = buildQuery();

        seriesQuery.setStartDate("1467383000000");

        Response response = querySeries(seriesQuery);

        assertEquals("Incorrect response status code", BAD_REQUEST.getStatusCode(), response.getStatus());
        JSONAssert.assertEquals("{\"error\":\"IllegalArgumentException: Wrong startDate syntax: 1467383000000\"}", response.readEntity(String.class), true);
    }

    /**
     * #3013
     */
    @Test
    public void testDateFilterRangeIsBeforeStorableRange() throws Exception {
        String entityName = "e-query-range-14";
        String metricName = "m-query-range-14";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MIN_STORABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Not empty data for disjoint query and stored interval", 0, data.size());
    }

    /**
     * #3013
     */
    @Test
    public void testDateFilterRangeIsAfterStorableRange() throws Exception {
        String entityName = "e-query-range-15";
        String metricName = "m-query-range-15";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), addOneMS(MAX_STORABLE_DATE), MAX_QUERYABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Not empty data for disjoint query and stored interval", 0, data.size());
    }

    /**
     * #3013
     */
    @Test
    public void testDateFilterRangeIncludesStorableRange() throws Exception {
        String entityName = "e-query-range-16";
        String metricName = "m-query-range-16";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that contains stored interval", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getD());
        assertEquals("Incorrect stored value", v, data.get(0).getV());
    }

    /**
     * #3013
     */
    @Test
    public void testDateFilterRangeIntersectsStorableRangeBeginning() throws Exception {
        String entityName = "e-query-range-17";
        String metricName = "m-query-range-17";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_QUERYABLE_DATE, addOneMS(MIN_STORABLE_DATE));
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that intersects stored interval from left", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getD());
        assertEquals("Incorrect stored value", v, data.get(0).getV());
    }

    /**
     * #3013
     */
    @Test
    public void testDateFilterRangeIntersectsStorableRangeEnding() throws Exception {
        String entityName = "e-query-range-18";
        String metricName = "m-query-range-18";
        BigDecimal v = new BigDecimal("7");

        Series series = new Series(entityName, metricName);
        series.addData(new Sample(MIN_STORABLE_DATE, v));

        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series.getEntity(), series.getMetric(), MIN_STORABLE_DATE, MAX_QUERYABLE_DATE);
        List<Sample> data = executeQueryReturnSeries(seriesQuery).get(0).getData();

        assertEquals("Empty data for query interval that intersects stored interval from right", 1, data.size());
        assertEquals("Incorrect stored date", MIN_STORABLE_DATE, data.get(0).getD());
        assertEquals("Incorrect stored value", v, data.get(0).getV());
    }

    /**
     * #3043
     */
    @Test
    public void testEveryDayFrom1969ToMinStorableDateFailToInsert() throws Exception {
        Series series = new Series("e-query-range-19", "m-query-range-19");
        BigDecimal v = new BigDecimal("7");

        calendar.setTime(parseDate("1969-01-01T00:00:00.000Z"));
        Date endDate = parseDate(MIN_STORABLE_DATE);

        while (calendar.getTime().before(endDate)) {
            series.setData(Collections.singletonList(new Sample(ISOFormat(calendar.getTime()), v)));
            Response response = insertSeries(Collections.singletonList(series), false);

            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    BAD_REQUEST.getStatusCode(), response.getStatusInfo().getStatusCode());
            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    "{\"error\":\"IllegalArgumentException: Negative timestamp\"}", response.readEntity(String.class));

            setRandomTimeDuringNextDay(calendar);
        }
    }

    /**
     * #3043
     */
    @Test
    public void testEveryDayFromMinToMaxStorableDateCorrectlySaved() throws Exception {
        Series series = new Series("e-query-range-20", "m-query-range-20");
        BigDecimal v = new BigDecimal("8");

        calendar.setTime(parseDate(MIN_STORABLE_DATE));
        Date maxStorableDay = parseDate(MAX_STORABLE_DATE);

        while (calendar.getTime().before(maxStorableDay)) {
            series.addData(new Sample(ISOFormat(calendar.getTime()), v));
            setRandomTimeDuringNextDay(calendar);
        }
        series.addData(new Sample(MAX_STORABLE_DATE, v));
        insertSeriesCheck(Collections.singletonList(series));
    }

    /**
     * #3043
     */
    @Test
    public void testEveryDayFromMaxStorableDateTo2110FailToInsert() throws Exception {
        Series series = new Series("e-query-range-21", "m-query-range-21");
        BigDecimal v = new BigDecimal("9");

        calendar.setTime(parseDate(addOneMS(MAX_STORABLE_DATE)));
        Date endDate = parseDate("2110-01-01T00:00:00.000Z");

        while (calendar.getTime().before(endDate)) {
            series.setData(Collections.singletonList(new Sample(ISOFormat(calendar.getTime()), v)));
            Response response = insertSeries(Collections.singletonList(series), false);

            assertEquals("Attempt to insert date before min storable date doesn't return error",
                    BAD_REQUEST.getStatusCode(), response.getStatusInfo().getStatusCode());
            assertTrue("Attempt to insert date before min storable date doesn't return error",
                    response.readEntity(String.class).startsWith("{\"error\":\"IllegalArgumentException: Too large timestamp"));

            setRandomTimeDuringNextDay(calendar);
        }
    }

    /**
     * #2979
     */
    @Test
    public void testEntitesExpressionStarChar() throws Exception {
        Series series = new Series("e-query-wildcard-22-1", "m-query-wildcard-22");
        series.addData(new Sample("2010-01-01T00:00:00.000Z", "0"));
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

    /**
     * #2979
     */
    @Test
    public void testEntitesExpressionQuestionChar() throws Exception {
        Series series = new Series("e-query-wildcard-23-1", "m-query-wildcard-23");
        series.addData(new Sample("2010-01-01T00:00:00.000Z", "0"));
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

    /**
     * #2970
     */
    @Test
    public void testVersionedLimitSupport() throws Exception {
        Series series = new Series("e-query-v-l-24", "m-query-v-l-24");
        final int limitValue = 2;
        Metric versionedMetric = new Metric();
        versionedMetric.setName(series.getMetric());
        versionedMetric.setVersioned(true);

        MetricMethod.createOrReplaceMetric(versionedMetric);

        series.addData(new Sample(MIN_STORABLE_DATE, "0"));
        series.addData(new Sample(addOneMS(MIN_STORABLE_DATE), "1"));
        series.addData(new Sample(addOneMS(addOneMS(MIN_STORABLE_DATE)), "2"));
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

    /**
     * #3030
     */
    @Test
    public void testDateIntervalFieldEnoughToDetail() throws Exception {
        Series series = new Series("entity-query-24", "metric-query-24");
        series.addData(new Sample(MIN_STORABLE_DATE, 1));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        query.setInterval(new Interval(99999, TimeUnit.QUARTER));

        List<Series> storedSeries = executeQueryReturnSeries(query);

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = jacksonMapper.writeValueAsString(storedSeries);
        assertTrue("Stored series does not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #3030
     */
    @Test
    public void testDateIntervalFieldEnoughToGroup() throws Exception {
        Series series = new Series("entity-query-25", "metric-query-25");
        series.addData(new Sample(MIN_STORABLE_DATE, 1));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        query.setInterval(new Interval(99999, TimeUnit.QUARTER));

        query.setGroup(new Group(GroupType.SUM));

        List<Series> storedSeries = executeQueryReturnSeries(query);

        final String expected = jacksonMapper.writeValueAsString(Collections.singletonList(series));
        final String given = jacksonMapper.writeValueAsString(storedSeries);
        assertTrue("Stored series does not match to inserted", compareJsonString(expected, given));
    }

    /**
     * #3030
     */
    @Test
    public void testDateIntervalFieldEnoughToAggregate() throws Exception {
        final BigDecimal VALUE = new BigDecimal("1.0");
        Series series = new Series("entity-query-26", "metric-query-26");
        series.addData(new Sample("2014-01-01T00:00:00.000Z", VALUE));
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery query = new SeriesQuery();
        query.setEntity(series.getEntity());
        query.setMetric(series.getMetric());
        Interval interval = new Interval(99999, TimeUnit.QUARTER);
        query.setInterval(interval);

        query.setAggregate(new Aggregate(AggregationType.SUM, interval));


        List<Series> storedSeries = executeQueryReturnSeries(query);
        assertEquals("Response should contain only one series", 1, storedSeries.size());
        List<Sample> data = storedSeries.get(0).getData();
        assertEquals("Response should contain only one sample", 1, data.size());
        assertEquals("Returned value does not match to expected SUM", VALUE, data.get(0).getV());
    }

    /**
     * #3324
     */
    @Test
    public void testAggregateInterpolateNoTypeRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mock-entity", "mock-metric", MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        Aggregate aggregate = new Aggregate(AggregationType.SUM, new Interval(99999, TimeUnit.QUARTER));
        aggregate.setInterpolate(new Interpolate());

        query.setAggregate(aggregate);

        Response response = querySeries(query);

        assertEquals("Query with interpolation but without type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", INTERPOLATE_TYPE_REQUIRED, extractErrorMessage(response));
    }


    /**
     * #3324
     */
    @Test
    public void testGroupInterpolateNoTypeRaiseError() throws Exception {
        SeriesQuery query = new SeriesQuery("mock-entity", "mock-metric", MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        Group group = new Group(GroupType.SUM, new Interval(99999, TimeUnit.QUARTER));
        group.setInterpolate(new Interpolate());

        query.setGroup(group);

        Response response = querySeries(query);

        assertEquals("Query with interpolation but without type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Error message mismatch", INTERPOLATE_TYPE_REQUIRED, extractErrorMessage(response));
    }

    /**
     * #3324
     */
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
        return new Object[][]{
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

    /**
     * #3480
     **/
    @Test(dataProvider = "dataTextProvider")
    public void testXTextField(String text) throws Exception {
        String entityName = Util.TestNames.entity();
        String metricName = Util.TestNames.metric();

        String largeNumber = "10.1";
        Series series = new Series(entityName, metricName);
        Sample sample = new Sample(MIN_STORABLE_DATE, new BigDecimal(largeNumber), text);
        series.addData(sample);
        insertSeriesCheck(Collections.singletonList(series));

        SeriesQuery seriesQuery = new SeriesQuery(series);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertEquals("Stored series are incorrect", Collections.singletonList(series), seriesList);
    }

    /**
     * #3480
     **/
    @Test
    public void testXTextFieldLastVersion() throws Exception {
        String metricName = "m-text-overwritten-versioning-1";
        Metric metric = new Metric(metricName);
        metric.setVersioned(true);
        MetricMethod.createOrReplaceMetricCheck(metric);

        Series series = new Series();
        series.setMetric(metricName);
        String entityName = "e-text-overwritten-versioning-1";
        Registry.Entity.register(entityName);
        series.setEntity(entityName);

        String[] data = new String[]{"1", "2"};
        for (String x : data) {
            Sample sample = new Sample("2016-10-11T13:00:00.000Z", new BigDecimal(1.0), x);
            series.setData(Collections.singleton(sample));
            insertSeriesCheck(Collections.singletonList(series));
        }

        SeriesQuery seriesQuery = new SeriesQuery(series);
        List<Series> seriesList = executeQueryReturnSeries(seriesQuery);

        assertFalse("No series", seriesList.isEmpty());
        assertFalse("No series data", seriesList.get(0).getData().isEmpty());
        String received = seriesList.get(0).getData().get(0).getText();
        assertEquals("Last version of text field incorrect", data[data.length-1], received);
    }

    private void setRandomTimeDuringNextDay(Calendar calendar) {
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, random.nextInt(24));
        calendar.set(Calendar.MINUTE, random.nextInt(60));
    }


    private SeriesQuery buildQuery() {
        SeriesQuery seriesQuery = new SeriesQuery();
        seriesQuery.setEntity("series-query-e-1");
        seriesQuery.setMetric("series-query-m-1");

        seriesQuery.setInterval(new Interval(1, TimeUnit.MILLISECOND));
        return seriesQuery;
    }
}
