package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSVUploadTest extends CSVUploadMethod {
    private static final String RESOURCE_DIR = "csv_upload";
    private static final String ENTITY_PREFIX = "e-csv-simple-parser";
    private static final String METRIC_PREFIX = "m-csv-simple-parser";
    public static final String PARSER_NAME = "simple-parser";

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException {
        File configPath = resolvePath(RESOURCE_DIR + File.separator+PARSER_NAME+".xml");
        boolean success = importParser(configPath);
        assertTrue(success);
    }

    /*
    * #2916
    * */
    @Test
    public void testPlainCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX+"-1";
        String metricName = METRIC_PREFIX+"-1";
        
        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".csv");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2916
    * */
    @Test
    public void testPlainCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX+"-2";
        String metricName = METRIC_PREFIX+"-2";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator+name.getMethodName()+".csv");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testTarGzCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-3";
        String metricName = METRIC_PREFIX + "-3";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testTarGzCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-4";
        String metricName = METRIC_PREFIX + "-4";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testZipCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-5";
        String metricName = METRIC_PREFIX + "-5";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testZipCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-6";
        String metricName = METRIC_PREFIX + "-6";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testGzCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-7";
        String metricName = METRIC_PREFIX + "-7";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /*
    * #2919
    * */
    @Test
    public void testGzCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-8";
        String metricName = METRIC_PREFIX + "-8";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    private void assertSeriesValue(String entity, String metric, String date, String value, JSONArray storedSeriesList) throws JSONException {
        assertEquals(entity, storedSeriesList.getJSONObject(0).getString("entity"));
        assertEquals(metric, storedSeriesList.getJSONObject(0).getString("metric"));
        assertEquals(date, storedSeriesList.getJSONObject(0).getJSONArray("data").getJSONObject(0).getString("d"));
        assertEquals(value, storedSeriesList.getJSONObject(0).getJSONArray("data").getJSONObject(0).getString("v"));
    }

    private void checkBinaryFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);

        Response response = binaryCsvUpload(csvPath, PARSER_NAME);

        assertEquals(response.getStatus(), OK.getStatusCode());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, Util.getMinDate(), Util.getMaxDate());
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }

    private void checkMultipartFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);

        Response response = multipartCsvUpload(csvPath, PARSER_NAME);

        assertEquals(response.getStatus(), OK.getStatusCode());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, Util.getMinDate(), Util.getMaxDate());
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }
}
