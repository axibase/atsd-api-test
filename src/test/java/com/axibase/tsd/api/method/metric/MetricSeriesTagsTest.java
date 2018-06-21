package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.CustomParameters;
import com.axibase.tsd.api.method.MethodParameters;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.metric.MetricSeriesTags;
import com.axibase.tsd.api.util.Mocks;
import com.axibase.tsd.api.util.TestUtil;
import io.qameta.allure.Issue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.testng.AssertJUnit.assertEquals;

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
    public void testMetricSeriesTagsNoParams() {
        MetricSeriesTags expectedTags = new MetricSeriesTags()
                .addTags("t1", "p1", "p2", "v1", "v2", "x1")
                .addTags("t2", "v1", "v2");

        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, null);
        assertEquals("Wrong result for {metric}/series/tags without parameters",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with single tags parameter")
    public void testMetricSeriesTagsTagParam() {
        MetricSeriesTags expectedTags = new MetricSeriesTags()
                .addTags("t1", "p1", "p2", "v1", "v2", "x1");

        MethodParameters parameters = new MetricListParameters().addTag("t1");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with single tags parameter",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with wildcard tags parameters")
    public void testMetricSeriesTagsTagPatternParam() {
        MetricSeriesTags expectedTags = new MetricSeriesTags()
                .addTags("t1", "v1", "v2")
                .addTags("t2", "v1", "v2");

        MethodParameters parameters = new CustomParameters().addParameter("tags.t1", "v*");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with wildcard tags parameters",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags.name parameter")
    public void testMetricSeriesTagsTagNameParams() {
        MetricSeriesTags expectedTags = new MetricSeriesTags()
                .addTags("t1", "v1")
                .addTags("t2", "v2");

        MethodParameters parameters = new CustomParameters().addParameter("tags.t1", "v1")
                .addParameter("tags.t2", "v2");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with tags.name parameter",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags and tags.name wildcard parameters")
    public void testMetricSeriesTagsTagNameAndTagsParams() {
        MetricSeriesTags expectedTags = new MetricSeriesTags().addTags("t1", "p2");

        MethodParameters parameters = new CustomParameters().addParameter("tags.t1", "p*")
                .addParameter("tags.t2", "v2")
                .addParameter("tags", "t1");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with tags and tags.name wildcard parameters",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with tags.name as simple name and wildcard")
    public void testMetricSeriesTagsNoSecondTagPattern() {
        MetricSeriesTags expectedTags = new MetricSeriesTags();

        MethodParameters parameters = new CustomParameters()
                .addParameter("tags.t1", "x1")
                .addParameter("tags.t2", "*");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with tags.name as simple name and wildcard",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags with existing but irrelevant tags.name")
    public void testMetricSeriesTagsNoSecondTag() {
        MetricSeriesTags expectedTags = new MetricSeriesTags();

        MethodParameters parameters = new CustomParameters()
                .addParameter("tags.t1", "x1")
                .addParameter("tags.t2", "v2");
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags with existing but irrelevant tags.name",
                expectedTags, responseTags);
    }

    @Issue("4715")
    @Test(description = "Test {metric}/series/tags entity parameter")
    public void testMetricSeriesTagsEntity() {
        MetricSeriesTags expectedTags = new MetricSeriesTags().addTags("t1", "x1");

        MethodParameters parameters = new CustomParameters().addParameter("entity", ENTITY_NAME2);
        MetricSeriesTags responseTags = queryMetricSeriesTags(METRIC_NAME, parameters);

        assertEquals("Wrong result for {metric}/series/tags entity parameter",
                expectedTags, responseTags);
    }

    @Issue("5208")
    @Test(description = "Query series filtered only by tag's name with wildcard",
            dataProvider = "provideWildcardTagsWithoutEntityData")
    public void testWildCardFilterWithoutEntity(final TestData testData) throws Exception {
        //Arrange
        String metricName = Mocks.metric();
        String entityName = Mocks.entity();
        List<Series> seriesList = testData.getTagsSet()
                .stream().map(tags -> new Series(entityName, metricName, tags))
                .peek(s -> s.addSamples(Mocks.SAMPLE))
                .collect(Collectors.toList());
        SeriesMethod.insertSeriesCheck(seriesList);

        //Action
        final Map<String, String> filter = testData.getFilter();
        MethodParameters parameters = CustomParameters.of(filter);
        MetricSeriesTags actualTags = MetricMethod.queryMetricSeriesTags(metricName, parameters);

        //Assert
        final String assertMessage = String.format(
                "Incorrect tags after applying filter: %s.%nSource data: %s.%n" +
                        "Expected: %s%nActual: %s%n",
                testData.filter, seriesList, testData.filteredTags, actualTags
        );
        assertEquals(assertMessage, testData.filteredTags, actualTags);
    }

    @DataProvider
    private Object[][] provideWildcardTagsWithoutEntityData() throws URISyntaxException, IOException {
        final URL resource = TestData.class.getResource("metric-series-tags-wildcard-without-entity-data.json");
        final File inputFile = Paths.get(resource.toURI()).toFile();
        return TestUtil.jsonProvider(inputFile, TestData[].class);
    }

    @AllArgsConstructor(staticName = "of")
    @Getter
    @ToString
    private static class TestData {
        private Collection<Map<String, String>> tagsSet;
        private Map<String, String> filter;
        private MetricSeriesTags filteredTags;
    }
}
