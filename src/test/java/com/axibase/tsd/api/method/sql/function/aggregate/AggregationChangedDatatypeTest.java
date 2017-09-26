package com.axibase.tsd.api.method.sql.function.aggregate;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.*;

public class AggregationChangedDatatypeTest extends SqlTest {

    @Issue("3881")
    @Test
    public void testChangedDataTypeValues() throws Exception {
        String entityName = entity();
        String metricName = metric();

        Series series = new Series(entityName, metricName, "tag1", "1");
        series.addSamples(SAMPLE);

        SeriesMethod.insertSeriesCheck(series);

        Metric metric = new Metric();
        metric.setName(metricName);
        metric.setDataType(DataType.DECIMAL);
        MetricMethod.createOrReplaceMetric(metric);

        String sqlQuery = String.format(
                "SELECT SUM(ROUND(value * cast(tags.tag1))) FROM \"%s\"",
                metric.getName());

        String[][] expectedRows = {{"123.0"}};

        assertSqlQueryRows(
                "Error when querying metric with changed data type",
                expectedRows,
                sqlQuery);
    }
}
