package com.axibase.tsd.api.method.sql.function.aggregation;

import com.axibase.tsd.api.method.metric.MetricMethod;
import com.axibase.tsd.api.method.series.SeriesMethod;
import com.axibase.tsd.api.method.sql.SqlTest;
import com.axibase.tsd.api.model.entity.Entity;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.model.series.DataType;
import com.axibase.tsd.api.model.series.Series;
import org.testng.annotations.Test;

import static com.axibase.tsd.api.util.Mocks.*;

public class AggregationChangedDatatypeTest extends SqlTest {

    /**
     * #3881
     */
    @Test
    public void testChangedDataTypeValues() throws Exception {
        Entity entity = entity();
        Metric metric = metric();

        Series series = new Series();
        series.setEntity(entity.getName());
        series.setMetric(metric.getName());
        series.addData(SAMPLE);
        series.addTag("tag1", "1");

        SeriesMethod.insertSeriesCheck(series);

        metric.setDataType(DataType.DECIMAL);
        MetricMethod.createOrReplaceMetric(metric);

        String sqlQuery = String.format(
                "SELECT SUM(ROUND(value * cast(tags.tag1))) FROM '%s'",
                metric.getName());

        String[][] expectedRows = {{"123.0"}};

        assertSqlQueryRows(
                "Error when querying metric with changed data type",
                expectedRows,
                sqlQuery);
    }
}
