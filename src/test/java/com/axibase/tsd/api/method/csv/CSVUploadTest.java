package com.axibase.tsd.api.method.csv;

import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.version.VersionMethod;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.model.version.Version;
import com.axibase.tsd.api.util.Registry;
import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import static com.axibase.tsd.api.method.series.SeriesTest.assertSeriesQueryDataSize;
import static com.axibase.tsd.api.util.Mocks.*;
import static com.axibase.tsd.api.util.Util.DEFAULT_TIMEZONE_NAME;
import static com.axibase.tsd.api.util.Util.parseDate;
import static javax.ws.rs.core.Response.Status.OK;
import static org.testng.AssertJUnit.assertEquals;

public class CSVUploadTest extends CSVUploadMethod {
    public static final String LINE_BREAKS_TEST_VALUE = "533.9";
    private static final String SIMPLE_PARSER = "simple-parser";
    private static final String SIMPLE_PARSER_ISO = "simple-parser-iso";
    private static final String SIMPLE_PARSER_MS = "simple-parser-ms";
    private static final String LF_PARSER = "lf-parser";
    private static final String CRLF_PARSER = "crlf-parser";
    private static final String RESOURCE_DIR = "csv_upload";
    private static final String ENTITY_PREFIX = "e-csv-simple-parser";
    private static final String METRIC_PREFIX = "m-csv-simple-parser";
    private static String timezone = DEFAULT_TIMEZONE_NAME;

    @BeforeClass
    public static void installParser() throws URISyntaxException, FileNotFoundException, JSONException {
        String[] parsers = {SIMPLE_PARSER, SIMPLE_PARSER_ISO, SIMPLE_PARSER_MS, LF_PARSER, CRLF_PARSER};
        for (String parserName : parsers) {
            File configPath = resolvePath(RESOURCE_DIR + File.separator + parserName + ".xml");
            boolean success = importParser(configPath);
            if (!success)
                Assert.fail("Failed to import parser");
        }

        Version versionInfo = VersionMethod.queryVersion().readEntity(Version.class);
        timezone = versionInfo.getDate().getTimeZone().getName();
    }

    @DataProvider(name = "parserProvider")
    private static Object[][] provideParsers() {
        return new Object[][]{
                {2, "cr", CRLF_PARSER, 3},
                {3, "lf", LF_PARSER, 3},
                {4, "crlf", CRLF_PARSER, 3}
        };
    }

    /* #2916 */
    @Test
    public void testPlainCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-1";
        String metricName = METRIC_PREFIX + "-1";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-multipart.csv").toString());

        checkMultipartFileUpload(entityName, metricName, csvPath);

    }

    /* #2916 */
    @Test
    public void testPlainCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-2";
        String metricName = METRIC_PREFIX + "-2";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-binary.csv").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-3";
        String metricName = METRIC_PREFIX + "-3";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-multipart.tar.gz").toString());

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testTarGzCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-4";
        String metricName = METRIC_PREFIX + "-4";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-binary.tar.gz").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-5";
        String metricName = METRIC_PREFIX + "-5";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-multipart.zip").toString());

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testZipCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-6";
        String metricName = METRIC_PREFIX + "-6";


        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-binary.zip").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvMultipartUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-7";
        String metricName = METRIC_PREFIX + "-7";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-multipart.gz").toString());

        checkMultipartFileUpload(entityName, metricName, csvPath);
    }

    /* #2919 */
    @Test
    public void testGzCsvBinaryUpload(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-8";
        String metricName = METRIC_PREFIX + "-8";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-csv-binary.gz").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInTarGz(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-9";
        String metricName = METRIC_PREFIX + "-9";
        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-dsstore.tar.gz").toString());
        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInTarGz(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-10";
        String metricName = METRIC_PREFIX + "-10";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-meta.tar.gz").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testDSStoreFileInZip(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-11";
        String metricName = METRIC_PREFIX + "-11";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-dsstore.zip").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2966 */
    @Test
    public void testMetaFileInZip(Method method) throws Exception {
        String entityName = ENTITY_PREFIX + "-12";
        String metricName = METRIC_PREFIX + "-12";

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-meta.zip").toString());

        checkBinaryFileUpload(entityName, metricName, csvPath);
    }

    /* #2957 */
    @Test
    public void testTimeRangeInISO(Method method) throws Exception {
        Entity entity = new Entity("e-csv-simple-parser-iso-0");
        Metric metric = new Metric("m-csv-simple-parser-iso-0");

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-time-range-iso.csv").toString());

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER_ISO);
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);

        assertSeriesQueryDataSize(seriesQuery, 2);

        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

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

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-time-range-ms.csv").toString());

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER_MS);
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        assertSeriesQueryDataSize(seriesQuery, 2);
        List<Series> seriesList = SeriesMethod.executeQueryReturnSeries(seriesQuery);
        Series series = seriesList.get(0);

        assertEquals("Managed to insert dataset with date out of range", 2, series.getData().size());

        assertEquals("Min storable date failed to save", MIN_STORABLE_DATE, series.getData().get(0).getD());
        assertEquals("Incorrect stored value", "12.45", series.getData().get(0).getV().toString());
        assertEquals("Max storable date failed to save", MAX_STORABLE_DATE, series.getData().get(1).getD());
        assertEquals("Incorrect stored value", "10.8", series.getData().get(1).getV().toString());
    }

    /* #3011 */
    @Test(dataProvider = "parserProvider")
    public void testFileWithLineBreak(Method method, int numTest, String lineBreakType, String parser, Integer dataSize) throws Exception {
        String nameSuffix = String.format("-%s-parser-ms-%d", lineBreakType, numTest);
        Entity entity = new Entity("e" + nameSuffix);
        Metric metric = new Metric("m" + nameSuffix);

        String confFileName = String.format("test-%s-parser.csv", lineBreakType);
        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, confFileName).toString());

        Response response = binaryCsvUpload(csvPath, parser, entity.getName());
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        assertSeriesQueryDataSize(seriesQuery, dataSize);

        Sample sample = SeriesMethod.executeQueryReturnSeries(seriesQuery).get(0).getData().get(0);
        Calendar serverCalendar = new GregorianCalendar(TimeZone.getTimeZone(timezone));
        serverCalendar.clear();
        serverCalendar.set(2015, Calendar.MARCH, 24, 6, 17);

        assertEquals("Incorrect stored value", LINE_BREAKS_TEST_VALUE, sample.getV().toString());
        assertEquals("Date failed to save", serverCalendar.getTime(), parseDate(sample.getD()));
    }

    /* #3591 */
    @Test
    public void testFileWithCRLineBreakAndDST(Method method) throws Exception {
        Entity entity = new Entity("e-cr-dst-parser-ms-2");
        Metric metric = new Metric("m-cr-dst-parser-ms-2");

        File csvPath = resolvePath(Paths.get(RESOURCE_DIR, "test-cr-dst-parser.csv").toString());

        Response response = binaryCsvUpload(csvPath, CRLF_PARSER, entity.getName());
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(entity.getName(), metric.getName(), MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        assertSeriesQueryDataSize(seriesQuery, 3);
        Sample sample = SeriesMethod.executeQueryReturnSeries(seriesQuery).get(0).getData().get(0);

        Calendar serverCalendar = new GregorianCalendar(TimeZone.getTimeZone(timezone));
        serverCalendar.clear();
        serverCalendar.set(2015, Calendar.NOVEMBER, 24, 6, 17);

        assertEquals("Date failed to save", serverCalendar.getTime(), parseDate(sample.getD()));
    }


    private void assertSeriesValue(String entity, String metric, String date, String value, List<Series> actualSeriesList) throws JSONException {
        Series expectedSeries = new Series();
        expectedSeries.setEntity(entity);
        expectedSeries.setMetric(metric);
        expectedSeries.addData(new Sample("2016-06-19T00:00:00.000Z", "123.45"));
        List<Series> expectedSeriesList = Collections.singletonList(expectedSeries);
        assertEquals(expectedSeriesList, actualSeriesList);
    }

    private void checkBinaryFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);

        Response response = binaryCsvUpload(csvPath, SIMPLE_PARSER);

        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());

        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        assertSeriesQueryDataSize(seriesQuery, 1);
        assertSeriesValue(
                entityName,
                metricName,
                "2016-06-19T00:00:00.000Z",
                "123.45",
                SeriesMethod.executeQueryReturnSeries(seriesQuery)
        );
    }

    private void checkMultipartFileUpload(String entityName, String metricName, File csvPath) throws Exception {
        Registry.Entity.registerPrefix(entityName);
        Registry.Metric.registerPrefix(metricName);
        Response response = multipartCsvUpload(csvPath, SIMPLE_PARSER);
        assertEquals("Failed to upload file", OK.getStatusCode(), response.getStatus());
        SeriesQuery seriesQuery = new SeriesQuery(entityName, metricName, MIN_QUERYABLE_DATE, MAX_QUERYABLE_DATE);
        assertSeriesQueryDataSize(seriesQuery, 1);
        assertSeriesValue(
                entityName,
                metricName,
                "2016-06-19T00:00:00.000Z",
                "123.45",
                SeriesMethod.executeQueryReturnSeries(seriesQuery)
        );
    }

    private static class SeriesQueryDataSizeCheck extends AbstractCheck {
        private SeriesQuery query;
        private Integer size;

        private SeriesQueryDataSizeCheck(SeriesQuery query, Integer size) {
            this.query = query;
            this.size = size;
        }

        @Override
        public boolean isChecked() {
            Response response = SeriesMethod.querySeries(query);
            if (response.getStatus() != OK.getStatusCode()) {
                return false;
            }
            List<Series> seriesList = response.readEntity(new GenericType<List<Series>>() {
            });
            return (seriesList.size() == 1) && (seriesList.get(0).getData().size() == size);
        }
    }
}
