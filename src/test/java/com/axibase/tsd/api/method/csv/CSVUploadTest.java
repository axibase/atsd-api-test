package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.Registry;
import com.axibase.tsd.api.Util;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Series;
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
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CSVUploadTest extends CSVUploadMethod {
    private static final String RESOURCE_DIR = "csv_upload";
    private static final String ENTITY_PREFIX = "e-csv-simple-parser";
    private static final String METRIC_PREFIX = "m-csv-simple-parser";
    public static final String[] PARSER_NAMES = {"simple-parser", "simple-parser-iso", "simple-parser-ms"};

    @Rule
    public TestName name = new TestName();

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException {
        for (String parserName : PARSER_NAMES) {
            File configPath = resolvePath(RESOURCE_DIR + File.separator + parserName + ".xml");
            boolean success = importParser(configPath);
            assertTrue(success);
        }
    }

    /* #2916 */
    @Test
    public void testPlainCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-1";
        String metricName = METRIC_PREFIX + "-1";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".csv");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2916 */
    @Test
    public void testPlainCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-2";
        String metricName = METRIC_PREFIX + "-2";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".csv");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-3";
        String metricName = METRIC_PREFIX + "-3";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-4";
        String metricName = METRIC_PREFIX + "-4";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-5";
        String metricName = METRIC_PREFIX + "-5";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-6";
        String metricName = METRIC_PREFIX + "-6";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvMultipartUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-7";
        String metricName = METRIC_PREFIX + "-7";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".gz");

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvBinaryUpload() throws Exception {
        String entityName = ENTITY_PREFIX + "-8";
        String metricName = METRIC_PREFIX + "-8";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInTarGz() throws Exception {
        String entityName = ENTITY_PREFIX + "-9";
        String metricName = METRIC_PREFIX + "-9";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInTarGz() throws Exception {
        String entityName = ENTITY_PREFIX + "-10";
        String metricName = METRIC_PREFIX + "-10";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".tar.gz");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInZip() throws Exception {
        String entityName = ENTITY_PREFIX + "-11";
        String metricName = METRIC_PREFIX + "-11";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInZip() throws Exception {
        String entityName = ENTITY_PREFIX + "-12";
        String metricName = METRIC_PREFIX + "-12";

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".zip");

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    @Test
    public void testTimeRangeInISO() throws Exception {
        Entity entity = new Entity("e-csv-simple-parser-iso-0");
        Metric metric = new Metric("m-csv-simple-parser-iso-0");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".csv");

        Response response = binaryCsvUpload(csvPath, PARSER_NAMES[1]);
        assertEquals(response.getStatus(), OK.getStatusCode());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), Util.getMinDate(), Util.getMaxDate());
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

        assertEquals("Managed to insert dataset with date out of range", 3, series.getData().size());

        assertEquals("Incorrect stored date", "1970-01-01T00:00:00.000Z", series.getData().get(0).getD());
        assertEquals("Incorrect stored value", "12.45", series.getData().get(0).getV().toString());
        assertEquals("Incorrect stored date", "1970-01-01T00:00:00.001Z", series.getData().get(1).getD());
        assertEquals("Incorrect stored value", "12", series.getData().get(1).getV().toString());
        assertEquals("Incorrect stored date", "2106-02-07T07:28:14.999Z", series.getData().get(2).getD());
        assertEquals("Incorrect stored value", "10.8", series.getData().get(2).getV().toString());
    }

    @Test
    public void testTimeRangeInMS() throws Exception {
        Entity entity = new Entity("e-csv-simple-parser-ms-1");
        Metric metric = new Metric("m-csv-simple-parser-ms-1");

        File csvPath = resolvePath(RESOURCE_DIR + File.separator + name.getMethodName() + ".csv");

        Response response = binaryCsvUpload(csvPath, PARSER_NAMES[2]);
        assertEquals(response.getStatus(), OK.getStatusCode());
        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), Util.getMinDate(), Util.getMaxDate());
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

        assertEquals("Managed to insert dataset with date out of range", 3, series.getData().size());

        assertEquals("Incorrect stored date", "1970-01-01T00:00:00.000Z", series.getData().get(0).getD());
        assertEquals("Incorrect stored value", "12.45", series.getData().get(0).getV().toString());
        assertEquals("Incorrect stored date", "1970-01-01T00:00:00.001Z", series.getData().get(1).getD());
        assertEquals("Incorrect stored value", "12", series.getData().get(1).getV().toString());
        assertEquals("Incorrect stored date", "2106-02-07T07:28:14.999Z", series.getData().get(2).getD());
        assertEquals("Incorrect stored value", "10.8", series.getData().get(2).getV().toString());
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

        Response response = binaryCsvUpload(csvPath, PARSER_NAMES[0]);

        assertEquals(response.getStatus(), OK.getStatusCode());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, Util.getMinDate(), Util.getMaxDate());
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }

    private void checkMultipartFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);

        Response response = multipartCsvUpload(csvPath, PARSER_NAMES[0]);

        assertEquals(response.getStatus(), OK.getStatusCode());

        Thread.sleep(1000L);

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, Util.getMinDate(), Util.getMaxDate());
        JSONArray storedSeriesList = SeriesMethod.executeQuery(seriesQuery);
        assertSeriesValue(entityName, metricName, "2016-06-19T00:00:00.000Z", "123.45", storedSeriesList);
    }
}
