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
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * @author Igor Shmagrinsky
 */
public class SqlPeriodInterpolationTest {
    private static final String TEST_PREFIX = "sql-period-interpolation";
    private static final Double[] valuesDistribution = {18.0, 8.0, 0.0, 6.0, 19.0, 19.0};
    private static final Long PERIOD_LENGTH = 300000L;
    private static final Double EPS = 10e-3;
    private static final Logger logger = LoggerFactory.getLogger(SqlPeriodInterpolationTest.class);
    private static final Date startDate = Util.parseISODate("2016-06-03T09:20:00.000Z");
    private final Date missingPeriodDate = new Date(startDate.getTime() + PERIOD_LENGTH*2);


    private Map<Date,Double> loadQueryResult(String queryFileName) throws IOException, JSONException {
        String workDir = System.getProperty("user.dir");
        String testDataDirectory = workDir + "/src/test/java/com/axibase/tsd/api/sql/data/period/interpolation/";
        String path  = testDataDirectory + queryFileName;
        return resultAsMap(new SqlExecuteMethod().queryAsJson(Util.readFile(path)));
    }

    @BeforeClass
    public static void initialize() throws Exception {
        createTestData(valuesDistribution, PERIOD_LENGTH, startDate);
    }

    private static void createTestData(Double[] valuesDistribution, Long periodLength, Date startDate) throws Exception {
        Random random = new Random();
        Entity testEntity = new Entity(TEST_PREFIX + "-entity");
        testEntity.addTag("type", "test");
        EntityMethod testEntityMethod = new EntityMethod();
        EntityMethod.prepare();
        testEntityMethod.createOrUpdateCheck(testEntity);
        Metric testMetric = new Metric(TEST_PREFIX + "-metric");
        MetricMethod testMetricMethod = new MetricMethod();
        testMetricMethod.createOrReplaceMetric(testMetric);
        Series series = new Series();
        series.setEntity(testEntity.getName());
        series.setMetric(testMetric.getName());
        Integer samplesCount = 0;
        for (int i = 0; i < valuesDistribution.length; i++) {
            for (int j = 0; j < valuesDistribution[i]; j++) {
                long t = startDate.getTime() + periodLength * i + random.nextInt(periodLength.intValue());
                String v = Integer.toString(random.nextInt(10));
                series.addData(new Sample(t, v));
                samplesCount++;
            }
        }
        Date endDate = new Date(startDate.getTime() + PERIOD_LENGTH*valuesDistribution.length);
        SeriesMethod seriesMethod = new SeriesMethod();
        seriesMethod.insertSeries(series);
        waitForData(startDate, endDate, samplesCount);
    }

    @AfterClass
    public static void clearTestData() throws IOException {
        new EntityMethod().deleteEntity(TEST_PREFIX + "-entity");
        new MetricMethod().deleteMetric(TEST_PREFIX + "-metric");
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
        for (int i = 0; i<data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            Date periodDate = Util.parseISODate((String) row.get(keyColumnName));
            Double periodValue = Double.parseDouble((String) row.get(valueColumnName));
            results.put(periodDate, periodValue);
        }
        return results;
    }

    private static void waitForData(Date startDate,Date endDate,Integer samplesCount) throws ParseException, JSONException, IOException {
        SeriesQuery seriesQuery = new SeriesQuery(TEST_PREFIX + "-entity", TEST_PREFIX + "-metric", startDate.getTime(), endDate.getTime());
        ArrayList<SeriesQuery> queryList = new ArrayList<SeriesQuery>();
        SeriesMethod seriesMethod = new SeriesMethod();
        queryList.add(seriesQuery);
        Integer resultLength;
        do {
            JSONArray result = seriesMethod.queryAsJson(queryList);
            resultLength = result.getJSONObject(0).getJSONArray("data").length();
            System.out.println(resultLength + " " +  samplesCount);
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
        Map<Date, Double>result = loadQueryResult("no-interpolation.sql");
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
        Map<Date, Double>result = loadQueryResult("fill-the-gaps.sql");
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
        Map<Date, Double>result = loadQueryResult("negative-fill-the-gaps.sql");
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
        Map<Date, Double>result = loadQueryResult("set-previous.sql");
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
        Map<Date, Double>result = loadQueryResult("linear-interpolation.sql");
        Double missingPeriodValue = result.get(missingPeriodDate);
        Double interpolatedValue =valuesDistribution[3] + (valuesDistribution[1] - valuesDistribution[3])/2;
        System.out.println(interpolatedValue);
        Assert.assertEquals(missingPeriodValue, interpolatedValue, EPS);
    }
}
