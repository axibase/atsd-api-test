package com.axibase.tsd.api.method.series;

import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import com.axibase.tsd.api.model.series.SeriesQuery;
import com.axibase.tsd.api.util.Registry;
import com.axibase.tsd.api.util.Util;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static com.axibase.tsd.api.util.Mocks.entity;
import static com.axibase.tsd.api.util.Mocks.metric;

public class SeriesQueryTagExpressionFilterTest extends SeriesMethod {

    /**
     * #3915(#20) test bugfix
     */

    @Test
    public void testTagExpressionFindsNotOnlyLastWrittenSeriesForEntity() throws Exception {
        // Arrange
        String metric = metric();
        String entity = entity();

        Series series1 = new Series(entity, metric, "key", "val1");
        series1.addSamples(new Sample("2017-03-27T00:00:00.000Z", 1));

        Series series2 = new Series(entity, metric, "key", "val2");
        series2.addSamples(new Sample("2017-03-27T00:00:01.000Z", 1));

        Series series3 = new Series(entity, metric, "key", "val3");
        series3.addSamples(new Sample("2017-03-27T00:00:02.000Z", 1));

        insertSeriesCheck(series1, series2, series3);

        // Action
        SeriesQuery query = new SeriesQuery();
        query.setMetric(metric);
        query.setEntity(entity);
        query.setStartDate(Util.MIN_QUERYABLE_DATE);
        query.setEndDate(Util.MAX_QUERYABLE_DATE);
        query.setTagExpression("tags.key = 'val2'");

        List<Series> list = executeQueryReturnSeries(query);

        // Assert
        Assert.assertEquals(list, Collections.singletonList(series2), "Series are not matched to tag expression '"+ query.getTagExpression()+"'");
    }
}
