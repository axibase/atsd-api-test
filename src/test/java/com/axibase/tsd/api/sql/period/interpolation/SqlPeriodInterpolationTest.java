package com.axibase.tsd.api.sql.period.interpolation;

import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.entity.EntityMethod;
import com.axibase.tsd.api.method.metrics.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.sql.SqlExecuteMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private static final int[] valuesDistribution = {18, 8, 0, 6, 19, 19};
    private static final Long PERIOD_LENGTH = 300000L;
    private static final Logger logger = LoggerFactory.getLogger(SqlPeriodInterpolationTest.class);
    private static final Date startDate = Util.parseDate("2016-06-03T09:20:00.000Z");
    private final Date missingPeriodDate = new Date(startDate.getTime() + PERIOD_LENGTH*2);


    private Map<Date,Integer> loadQueryResult(String queryName) throws IOException, JSONException {
        String workDir = System.getProperty("user.dir");
        String testDataDirectory = workDir + "/src/test/java/com/axibase/tsd/api/sql/data/period/interpolation/";
        String path  = testDataDirectory + queryName;
        return resultAsMap(new SqlExecuteMethod().queryAsJson(Util.readFile(path)));
    }

    @BeforeClass
    public static void initialize() throws Exception {
        Random random = new Random();
        Entity testEntity = new Entity(TEST_PREFIX + "-entity");
        testEntity.addTag("type", "test");
        EntityMethod testEntityMethod = new EntityMethod();
        testEntityMethod.createOrUpdate(testEntity);
        Metric testMetric = new Metric(TEST_PREFIX + "-metric");
        MetricMethod testMetricMethod = new MetricMethod();
        testMetricMethod.createOrReplaceMetric(testMetric);
        Series series = new Series();
        series.setEntity(testEntity.getName());
        series.setMetric(testMetric.getName());
        for (int i = 0; i < valuesDistribution.length; i++) {
            for (int j = 0; j < valuesDistribution[i]; j++) {
                long t = startDate.getTime() + PERIOD_LENGTH * i + random.nextInt(PERIOD_LENGTH.intValue());
                int v = random.nextInt(10);
                series.addData(new Sample(t, v));
            }
        }
        SeriesMethod seriesMethod = new SeriesMethod();
        seriesMethod.insertSeries(series);
    }

    @AfterClass
    public static void clearTestData() throws IOException {
        new EntityMethod().deleteEntity(TEST_PREFIX + "-entity");
        new MetricMethod().deleteMetric(TEST_PREFIX + "-metric");
    }

    private Map<Date, Integer> resultAsMap(JSONObject resultJSON) throws JSONException {
        Map<Date, Integer> results = new HashMap<>();
        logger.debug(resultJSON.toString());
        JSONArray data = resultJSON.getJSONArray("data");
        for (int i = 0; i<data.length(); i++) {
            JSONObject row = data.getJSONObject(i);
            Date periodDate = Util.parseDate((String) row.get("date_format(period(5 MINUTE))"));
            Integer periodValue = ((int) Float.parseFloat((String) row.get("COUNT(value)")));
            results.put(periodDate, periodValue);
        }
        return results;
    }


    /**
     * Test that PERIOD(TIME) does not contain missing period
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testContainsOfMissingValue() throws IOException, JSONException {
        Map<Date, Integer> result = loadQueryResult("no-interpolation.sql");
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
        Map<Date, Integer> result = loadQueryResult("fill-the-gaps.sql");
        Integer missingPeriodValue = result.get(missingPeriodDate);
        Assert.assertEquals((long)missingPeriodValue, 0L);
    }


    /**
     * Test that PERIOD(TIME, VALUE -1) fills the gaps with -1
     *
     * @throws IOException
     * @throws JSONException
     */
    @Test
    public void testNegativeFillingTheGaps() throws IOException, JSONException {
        Map<Date, Integer> result = loadQueryResult("negative-fill-the-gaps.sql");
        Integer missingPeriodValue = result.get(missingPeriodDate);
        Assert.assertEquals((long)missingPeriodValue, -1L);
    }

}
