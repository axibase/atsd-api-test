package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.CustomParameters;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.util.Mocks;
import io.qameta.allure.Issue;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class MetricSeriesTagsTest extends MetricMethod {
    private static final String ENTITY_NAME1 = Mocks.entity();
    private static final String ENTITY_NAME2 = Mocks.entity();
    private static final String METRIC_NAME = Mocks.metric();

    @BeforeClass
    public static void prepareData() throws Exception {
        Series series1 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t1", "v2").addTag("t2", "v1");
        series1.addSamples(Sample.ofDateInteger("2017-11-01T00:00:00.000Z", 1));

        Series series2 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t1", "v1").addTag("t2", "v2");
        series2.addSamples(Sample.ofDateInteger("2017-11-02T00:00:00.000Z", 1));

        Series series3 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t1", "p1").addTag("t2", "v1");
        series3.addSamples(Sample.ofDateInteger("2017-11-05T00:00:00.000Z", 1));

        Series series4 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t1", "p2").addTag("t2", "v2");
        series4.addSamples(Sample.ofDateInteger("2017-11-06T00:00:00.000Z", 1));

        Series series5 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t2", "v1");
        series5.addSamples(Sample.ofDateInteger("2017-11-07T00:00:00.000Z", 1));

        Series series6 = new Series(ENTITY_NAME1, METRIC_NAME).addTag("t2", "v2");
        series6.addSamples(Sample.ofDateInteger("2017-11-08T00:00:00.000Z", 1));

        Series series7 = new Series(ENTITY_NAME2, METRIC_NAME).addTag("t1", "x1");
        series7.addSamples(Sample.ofDateInteger("2017-11-09T00:00:00.000Z", 1));

        SeriesMethod.insertSeriesCheck(series1, series2, series3, series4,
                series5, series6, series7);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags without parameters")
    public void testMetricSeriesTagsNoParams() throws Exception {
        String expectedJson = "{" +
                "  \"t1\" : [ \"p1\", \"p2\", \"v1\", \"v2\", \"x1\" ]," +
                "  \"t2\" : [ \"v1\", \"v2\" ]" +
                "}";

        String r = queryMetricSeriesTags(METRIC_NAME, null).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with single tags parameter")
    public void testMetricSeriesTagsTagParam() throws Exception {
        String expectedJson = "{" +
                "  \"t1\" : [ \"p1\", \"p2\", \"v1\", \"v2\", \"x1\" ]" +
                "}";

        String r = queryMetricSeriesTags(METRIC_NAME,
                new MetricListParameters().addTag("t1")).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with wildcard tags parameters")
    public void testMetricSeriesTagsTagPatternParam() throws Exception {
        String expectedJson = "{" +
                "  \"t1\" : [ \"v1\", \"v2\" ]," +
                "  \"t2\" : [ \"v1\", \"v2\" ]" +
                "}";

        String r = queryMetricSeriesTags(METRIC_NAME, new CustomParameters().addParameter("tags.t1", "v*"))
                .readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags.name parameter")
    public void testMetricSeriesTagsTagNameParams() throws Exception {
        String expectedJson = "{" +
                "  \"t1\" : [ \"v1\" ]," +
                "  \"t2\" : [ \"v2\" ]" +
                "}";

        String r = queryMetricSeriesTags(METRIC_NAME,
                new CustomParameters().addParameter("tags.t1", "v1")
                        .addParameter("tags.t2", "v2")).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags and tags.name wildcard parameters")
    public void testMetricSeriesTagsTagNameAndTagsParams() throws Exception {
        String expectedJson = "{\"t1\":[\"p2\"]}";

        String r = queryMetricSeriesTags(METRIC_NAME,
                new CustomParameters().addParameter("tags.t1", "p*")
                        .addParameter("tags.t2", "v2")
                        .addParameter("tags", "t1")).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags.name as simple name and wildcard")
    public void testMetricSeriesTagsNoSecondTagPattern() throws Exception {
        String expectedJson = "{}";

        String r = queryMetricSeriesTags(METRIC_NAME,
                new CustomParameters().addParameter("tags.t1", "x1")
                        .addParameter("tags.t2", "*")).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with existing but irrelevant tags.name")
    public void testMetricSeriesTagsNoSecondTag() throws Exception {
        String expectedJson = "{}";

        String r = queryMetricSeriesTags(METRIC_NAME,
                new CustomParameters().addParameter("tags.t1", "x1")
                        .addParameter("tags.t2", "v2")).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags entity parameter")
    public void testMetricSeriesTagsEntity() throws Exception {
        String expectedJson = "{\"t1\" : [ \"x1\" ]}";

        String r = queryMetricSeriesTags(METRIC_NAME, new CustomParameters()
                .addParameter("entity", ENTITY_NAME2)).readEntity(String.class);
        assertJsonEquals(expectedJson, r);
    }

    private void assertJsonEquals(String expected, String actual) throws Exception {
        JSONObject actualObject = new JSONObject(actual);
        if (actualObject.has("error")) {
            fail(String.format("Expected JSON %s, got error: %s", expected, actualObject.getString("error")));
        }

        String assertMessage = String.format("The response JSON was %s, expected %s", actual, expected);
        assertTrue(assertMessage, compareJsonString(expected, actual, true));
    }
}
