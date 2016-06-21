package com.axibase.tsd.api.sql.period.interpolation;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metrics.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.sql.SqlExecuteMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;


/**
 * Test cases for testing SQL PERIOD function
 *
 * @author Igor Shmagrinsky
 * @see <a href="redmine/issues/1475">#1475</a>
 */
public class SqlPeriodInterpolationTest {

    private static final String TEST_PREFIX = "sql-period-interpolation";
    private static Entity testEntity = new Entity(TEST_PREFIX + "-entity");
    private static Metric testMetric = new Metric(TEST_PREFIX + "-metric");
    private static final Double[] valuesDistribution = {18.0, 8.0, 0.0, 6.0, 19.0, 19.0};
    private static final Long PERIOD_LENGTH = 300000L;
    private static final Double EPS = 10e-3;
    private static Series series = new Series(testEntity.getName(),testMetric.getName());
    private static final Long QUERY_EXECUTION_TIMEOUT = 2000L;
    private static final Date startDate = parseISODate("2016-06-03T09:20:00.000Z");
    private final Date missingPeriodDate = new Date(startDate.getTime() + PERIOD_LENGTH * 2);


    private Map<Date, Double> loadQueryResult(String queryFileName) throws IOException, JSONException {
        String workDir = System.getProperty("user.dir");
        String testDataDirectory = workDir + "/src/test/resources/sql/period/interpolation/";
        String path = testDataDirectory + queryFileName;
        return resultAsMap(new SqlExecuteMethod().queryAsJson(Util.readFile(path)));
    }

    @BeforeClass
    public static void initialize() throws Exception {
        createTestData(valuesDistribution, PERIOD_LENGTH, startDate);
    }


    private static Date parseISODate(String date) {
        try {
            return Util.getDate(date);
        }catch (java.text.ParseException pe) {
            return null;
        }
    }

    private static void createTestData(Double[] valuesDistribution, Long periodLength, Date startDate) throws Exception {
        Random random = new Random();
        testEntity.addTag("type", "test");
        EntityMethod testEntityMethod = new EntityMethod();
        EntityMethod.prepare();
        testEntityMethod.createOrUpdateCheck(testEntity);
        MetricMethod testMetricMethod = new MetricMethod();
        testMetricMethod.createOrReplaceMetric(testMetric);
        boolean created;
        do {
            created = entityExists(testEntity) && metricExists(testMetric);
        } while (!created);
        Integer samplesCount = 0;
        for (int i = 0; i < valuesDistribution.length; i++) {
            for (int j = 0; j < valuesDistribution[i]; j++) {
                long t = startDate.getTime() + periodLength * i + random.nextInt(periodLength.intValue());
                String v = Integer.toString(random.nextInt(10));
                series.addData(new Sample(t, v));
                samplesCount++;
            }
        }
        Date endDate = new Date(startDate.getTime() + periodLength * valuesDistribution.length);
        SeriesMethod seriesMethod = new SeriesMethod();
        seriesMethod.insertSeries(series);
        waitForSeriesData(startDate, endDate, samplesCount);
    }

    @AfterClass
    public static void clearTestData() throws Exception {
        EntityMethod testEntityMethod = new EntityMethod();
        MetricMethod testMetricMethod = new MetricMethod();
        testEntityMethod.deleteEntity(testEntity.getName());
        testMetricMethod.deleteMetric(testMetric.getName());
        Long deleteStartTime = System.currentTimeMillis();
        boolean deleted;
        do {
            deleted = !entityExists(testEntity) && !metricExists(testMetric);
            if ((System.currentTimeMillis() - deleteStartTime) > QUERY_EXECUTION_TIMEOUT) {
                throw new TimeoutException("Entity and metric are deleted too long!");
            }
        } while (!deleted);
    }

    private Map<Date, Double> resultAsMap(JSONObject resultJSON) throws JSONException {
        Map<Date, Double> results = new HashMap<>();
        JSONArray data = resultJSON.getJSONArray("data");
        JSONArray columns = resultJSON
                .getJSONObject("metadata")
                .getJSONObject("tableSchema")
                .getJSONArray("columns");
        String keyColumnName = columns
                .getJSONObject(1)
                .getString("name");
        String valueColumnName = columns
                .getJSONObject(2)
                .getString("name");
        for (int i = 0; i < data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            Date periodDate = parseISODate((String) row.get(keyColumnName));
            Double periodValue = Double.parseDouble((String) row.get(valueColumnName));
            results.put(periodDate, periodValue);
        }
        return results;
    }

    private static Boolean entityExists(Entity entity) throws IOException {
        EntityMethod entityMethod = new EntityMethod();
        try {
            return entityMethod.entityExist(entity);
        } catch (AssertionError e) {
            return false;
        }
    }

    private static Boolean metricExists(Metric metric) throws IOException {
        try {
            return new MetricMethod().metricExists(metric);
        } catch (Exception e) {
            return false;
        }
    }

    private double[] getValuesSortByKeys(Map<Date, Double> result) {
        List<Date> keys = new ArrayList<>();
        keys.addAll(result.keySet());
        Collections.sort(keys);
        double[] resultValues = new double[keys.size()];
        int count = 0;
        for (Date key : keys) {
            if (result.containsKey(key)) {
                resultValues[count] = result.get(key);
                count++;
            }
        }
        return resultValues;
    }

    private static void waitForSeriesData(Date startDate, Date endDate, Integer samplesCount) throws ParseException, JSONException, IOException, TimeoutException {
        SeriesQuery seriesQuery = new SeriesQuery(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric", startDate.getTime(), endDate.getTime());
        ArrayList<SeriesQuery> queryList = new ArrayList<>();
        SeriesMethod seriesMethod = new SeriesMethod();
        queryList.add(seriesQuery);
        Long createStartTime = System.currentTimeMillis();
        Integer resultLength;
        do {
            JSONArray result = seriesMethod.queryAsJson(queryList);
            resultLength = result.getJSONObject(0).getJSONArray("data").length();
            System.out.println(resultLength + " " + samplesCount);
            if ((System.currentTimeMillis() - createStartTime) > QUERY_EXECUTION_TIMEOUT) {
                throw new TimeoutException("Series data is created too long!");
            }
        }
        while (!samplesCount.equals(resultLength));
    }


    /**
     * Test that PERIOD(TIME) does not contain missing period
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testContainsOfMissingValue() throws IOException, JSONException {
        Map<Date, Double> result = loadQueryResult("no-interpolation.sql");
        Boolean containsMissingValue = result.containsKey(missingPeriodDate);
        Assert.assertEquals(containsMissingValue, false);
    }


    /**
     * Test that PERIOD(TIME, VALUE 0) fills the gaps with 0
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testFillingTheGaps() throws IOException, JSONException {
        Map<Date, Double> result = loadQueryResult("fill-the-gaps.sql");
        Double missingPeriodValue = result.get(missingPeriodDate);
        Assert.assertEquals(missingPeriodValue, 0L, EPS);
    }


    /**
     * Test that PERIOD(TIME, VALUE -1) fills the gaps with -1
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testNegativeFillingTheGaps() throws IOException, JSONException {
        Map<Date, Double> result = loadQueryResult("negative-fill-the-gaps.sql");
        Double missingPeriodValue = result.get(missingPeriodDate);
        Assert.assertEquals(missingPeriodValue, -1L, EPS);
    }

    /**
     * Test that PERIOD(TIME, PREVIOUS) filling missing period with
     * value from previous period
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testPreviousFillingTheGaps() throws IOException, JSONException {
        Map<Date, Double> result = loadQueryResult("set-previous.sql");
        Double missingPeriodValue = result.get(missingPeriodDate);
        Assert.assertEquals(missingPeriodValue, valuesDistribution[1], EPS);
    }


    /**
     * Test that PERIOD(TIME, LINEAR) filling missing period with
     * linear interpolated value
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testLinearInterpolation() throws IOException, JSONException {
        Map<Date, Double> result = loadQueryResult("linear-interpolation.sql");
        Double missingPeriodValue = result.get(missingPeriodDate);
        Double interpolatedValue = valuesDistribution[3] + (valuesDistribution[1] - valuesDistribution[3]) / 2;
        System.out.println(interpolatedValue);
        Assert.assertEquals(missingPeriodValue, interpolatedValue, EPS);
    }


    /**
     * Test that PERIOD(TIME, LINEAR) filling missing multiple period with
     * linear interpolated value
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testMultiplePeriodLinearInterpolation() throws Exception {
        Double[] valueDistributions = {4.0, 0.0, 0.0, 0.0, 0.0, 3.0};
        double[] expectedValues = {4.0, 3.8, 3.6, 3.4, 3.2, 3.0};
        createTestData(valueDistributions, 60000L, parseISODate("2016-06-03T09:00:00.000Z"));
        Map<Date, Double> result = loadQueryResult("multiple-period-interpolation.sql");
        double[] resultValues = getValuesSortByKeys(result);
        Assert.assertArrayEquals(resultValues, expectedValues, EPS);
    }

    /**
     * Test that PERIOD (TIME, VALUE) HAVING CONDITION filters values by
     * condition
     *
     * @throws Exception
     */
    @Test
    public void testHavingClause() throws Exception {
        Double[] valueDistributions = {4.0, 0.0, 1.0, 1.0, 1.0, 3.0};
        double[] expectedValues = {4.0, 0.0, 0.0, 0.0, 0.0, 3.0};
        createTestData(valueDistributions, 60000L, parseISODate("2016-06-03T09:10:00.000Z"));
        Map<Date, Double> result = loadQueryResult("having-clause.sql");
        double[] resultValues = getValuesSortByKeys(result);
        Assert.assertArrayEquals(resultValues, expectedValues, EPS);
    }
}
