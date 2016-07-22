package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

import static com.axibase.tsd.api.Util.*;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

public class CSVUploadTest extends CSVUploadMethod {
    private static final String RESOURCE_DIR = "csv_upload";
    private static final String ENTITY_PREFIX = "e-csv-simple-parser";
    private static final String METRIC_PREFIX = "m-csv-simple-parser";
    public static final String SIMPLE_PARSER = "simple-parser";
    public static final String SIMPLE_PARSER_ISO = "simple-parser-iso";
    public static final String SIMPLE_PARSER_MS = "simple-parser-ms";
    public static final String LF_PARSER = "lf-parser";
    public static final String CRLF_PARSER = "crlf-parser";
    public static Integer offsetMinutes  = 0;

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException, JSONException {
        String[] parsers = {SIMPLE_PARSER, SIMPLE_PARSER_ISO, SIMPLE_PARSER_MS, LF_PARSER, CRLF_PARSER};
        for (String parserName : parsers) {
            File configPath = resolvePath(RESOURCE_DIR + File.separator + parserName + ".xml");
            boolean success = importParser(configPath);
            if (!success)
                Assert.fail("Failed to import parser");
        }
        JSONObject jsonObject = new JSONObject(BaseMethod.queryATSDVersion().readEntity(String.class));
        offsetMinutes = Integer.parseInt(((JSONObject) ((JSONObject) jsonObject.get("date")).get("timeZone")).get("offsetMinutes").toString());
    }

    /* #2916 */
    @Test
    public void testPlainCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-1";
        String metricName = METRIC_PREFIX + "-1";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        checkMultipartFileUpload(entityName, metricName, csvPath);

    }

    /* #2916 */
    @Test
    public void testPlainCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-2";
        String metricName = METRIC_PREFIX + "-2";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-3";
        String metricName = METRIC_PREFIX + "-3";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".tar.gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-4";
        String metricName = METRIC_PREFIX + "-4";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-5";
        String metricName = METRIC_PREFIX + "-5";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".zip");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-6";
        String metricName = METRIC_PREFIX + "-6";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-7";
        String metricName = METRIC_PREFIX + "-7";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-8";
        String metricName = METRIC_PREFIX + "-8";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInTarGz(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-9";
        String metricName = METRIC_PREFIX + "-9";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInTarGz(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-10";
        String metricName = METRIC_PREFIX + "-10";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInZip(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-11";
        String metricName = METRIC_PREFIX + "-11";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInZip(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-12";
        String metricName = METRIC_PREFIX + "-12";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2957 */
    @Test
    public void testTimeRangeInISO(Method method) throws Exception {
        Entity entity = new Entity("e-csv-simple-parser-iso-0");
        Metric metric = new Metric("m-csv-simple-parser-iso-0");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER_ISO);
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

        assertEquals("Managed to insert dataset with date out of range", 2, series.getData().size());

        assertEquals("Min storable date failed to save", MIN_STORABLE_DATE, series.getData().get(0).getD());
        assertEquals("Incorrect stored value", "12.45", series.getData().get(0).getV().toString());
        assertEquals("Max storable date failed to save", MAX_STORABLE_DATE, series.getData().get(1).getD());
        assertEquals("Incorrect stored value", "10.8", series.getData().get(1).getV().toString());
    }

    /* #2957 */
    @Test
    public void testTimeRangeInMS(Method method) throws Exception {
        Entity entity = new Entity("e-csv-simple-parser-ms-1");
        Metric metric = new Metric("m-csv-simple-parser-ms-1");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER_MS);
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

        assertEquals("Managed to insert dataset with date out of range", 2, series.getData().size());

        assertEquals("Min storable date failed to save", MIN_STORABLE_DATE, series.getData().get(0).getD());
        assertEquals("Incorrect stored value", "12.45", series.getData().get(0).getV().toString());
        assertEquals("Max storable date failed to save", MAX_STORABLE_DATE, series.getData().get(1).getD());
        assertEquals("Incorrect stored value", "10.8", series.getData().get(1).getV().toString());
    }

    /* #3011 */
    @Test
    public void testFileWithCRLineBreak(Method method) throws Exception {
        Entity entity = new Entity("e-cr-parser-ms-2");
        Metric metric = new Metric("m-cr-parser-ms-2");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        Response response = binaryCsvUpload(csvPath, CRLF_PARSER, entity.getName());
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        Sample sample = SeriesMethod.executeQueryReturnSeries(seriesQuery).get(0).getData().get(0);
        String expectedDate = "2015-03-24T06:17:00.000Z";

        assertEquals("Incorrect stored value", "533.9", sample.getV().toString());
        assertEquals("Date failed to save", transformDateToServerTimeZone(expectedDate, offsetMinutes), sample.getD());
    }

    /* #3011 */
    @Test
    public void testFileWithLFLineBreak(Method method) throws Exception {
        Entity entity = new Entity("e-lf-parser-ms-3");
        Metric metric = new Metric("m-lf-parser-ms-3");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        Response response = binaryCsvUpload(csvPath, LF_PARSER, entity.getName());
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        Sample sample = SeriesMethod.executeQueryReturnSeries(seriesQuery).get(0).getData().get(0);
        String expectedDate = "2015-03-24T06:17:00.000Z";

        assertEquals("Incorrect stored value", "533.9", sample.getV().toString());
        assertEquals("Date failed to save", transformDateToServerTimeZone(expectedDate, offsetMinutes), sample.getD());
    }

    /* #3011 */
    @Test
    public void testFileWithCRLFLineBreak(Method method) throws Exception {
        Entity entity = new Entity("e-crlf-parser-ms-4");
        Metric metric = new Metric("m-crlf-parser-ms-4");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + method.getName() + ".csv");

        Response response = binaryCsvUpload(csvPath, CRLF_PARSER, entity.getName());
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        Sample sample = SeriesMethod.executeQueryReturnSeries(seriesQuery).get(0).getData().get(0);
        String expectedDate = "2015-03-24T06:17:00.000Z";

        assertEquals("Incorrect stored value", "533.9", sample.getV().toString());
        assertEquals("Date failed to save", transformDateToServerTimeZone(expectedDate,offsetMinutes), sample.getD());
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

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER);

        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }

    private void checkMultipartFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);

        Response response = multipartCsvUpload(csvPath, SIMPLE_PARSER);

        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }
}
