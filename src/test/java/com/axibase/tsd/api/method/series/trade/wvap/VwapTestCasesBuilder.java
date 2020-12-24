package com.axibase.tsd.api.method.series.trade.wvap;

import com.axibase.tsd.api.method.series.trade.SyntheticDataProvider;
import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.SeriesQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.query.transformation.group.Group;
import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
import com.axibase.tsd.api.util.TestUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static com.axibase.tsd.api.model.TimeUnit.MINUTE;

@RequiredArgsConstructor
public class VwapTestCasesBuilder {
    private final SyntheticDataProvider dp;
    Period period10 = new Period(10, MINUTE);
    Aggregate aggregateVwap10 = new Aggregate(AggregationType.VWAP, period10);
    Aggregate aggregateSum10 = new Aggregate(AggregationType.SUM, period10);
    Aggregate aggregateSum = new Aggregate(AggregationType.SUM);
    Aggregate aggregateVwap = new Aggregate(AggregationType.VWAP);
    Group groupVwap10 = new Group(GroupType.VWAP, period10);
    Group groupVwap = new Group(GroupType.VWAP);
    Group groupDetail10 = new Group(GroupType.DETAIL, period10);
    Group groupDetail = new Group(GroupType.DETAIL);

    public Object[][] testCases() {
        List<TestCase> testCases = new ArrayList<>();

        {   // 10-minutes aggregate, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregateVwap10);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityA, "S", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapSell).sample(dp.time30, dp.wvapSell))
                    .series(new ExpectedSeries(dp.entityB, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityB, "S", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapSell).sample(dp.time30, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // aggregate for whole selection interval, without 'side' tag
            SeriesQuery query = dp.aggregateQuery(aggregateVwap);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityA, "S", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapSell))
                    .series(new ExpectedSeries(dp.entityB, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityB, "S", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // 10-minutes aggregate, with 'side = B' tag, for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregateVwap10);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityB, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy));
            testCases.add(testCase);
        }

        {   // aggregate for whole selection interval, with 'side = B' tag
            SeriesQuery query = dp.aggregateQuery(aggregateVwap);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy))
                    .series(new ExpectedSeries(dp.entityB, "B", AggregationType.VWAP, null)
                            .sample(dp.time00, dp.wvapBuy));
            testCases.add(testCase);
        }

        {   // 10-minutes group, for entity A, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupVwap10, null);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, null, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBothSides).sample(dp.time20, dp.wvapBuy).sample(dp.time30, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // 10-minutes group, for entity A, 'side = B', for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupVwap10, null);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", null, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy));
            testCases.add(testCase);
        }

        {   // group for whole selection interval, for entity A, without 'side' tag
            SeriesQuery query = dp.entityAQuery(groupVwap, null);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, null, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBothSides));
            testCases.add(testCase);
        }

        {   // group for whole selection interval, for entity A, 'side = B'
            SeriesQuery query = dp.entityAQuery(groupVwap, null);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", null, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBuy));
            testCases.add(testCase);
        }

        {   // group for the first 3 periods, for entity A, without 'side' tag
            SeriesQuery query = dp.entityAQuery(groupVwap, null);
            query.setEndDate(dp.time30);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, null, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBothSides3periods));
            testCases.add(testCase);
        }

        {   // 10-minutes group, 10-minute aggregate, for entity A, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupVwap10, aggregateSum10);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, AggregationType.SUM, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBothSides).sample(dp.time20, dp.wvapBuy).sample(dp.time30, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // 10-minutes group, aggregate for the whole selection interval, for entity A, without 'side' tag
            SeriesQuery query = dp.entityAQuery(groupVwap10, aggregateSum);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, AggregationType.SUM, GroupType.VWAP)
                            .sample(dp.time00, dp.wvapBothSides + dp.wvapBuy + dp.wvapSell));
            testCases.add(testCase);
        }

        {   // 10-minutes DETAIL group, 10-minute aggregate, for entity A, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupDetail10, aggregateVwap10);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, AggregationType.VWAP, GroupType.DETAIL)
                            .sample(dp.time00, dp.wvapBothSides).sample(dp.time20, dp.wvapBuy).sample(dp.time30, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // 10-minutes DETAIL group, 10-minute aggregate, for entity A, 'side = B', for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupDetail10, aggregateVwap10);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, GroupType.DETAIL)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy));
            testCases.add(testCase);
        }

        {   // DETAIL grouping without period, 10-minute aggregate, for entity A, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupDetail, aggregateVwap10);
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, null, AggregationType.VWAP, GroupType.DETAIL)
                            .sample(dp.time00, dp.wvapBothSides).sample(dp.time20, dp.wvapBuy).sample(dp.time30, dp.wvapSell));
            testCases.add(testCase);
        }

        {   // DETAIL grouping without period, 10-minute aggregate, for entity A, 'side = B', for whole selection interval
            SeriesQuery query = dp.entityAQuery(groupDetail, aggregateVwap10);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new ExpectedSeries(dp.entityA, "B", AggregationType.VWAP, GroupType.DETAIL)
                            .sample(dp.time00, dp.wvapBuy).sample(dp.time20, dp.wvapBuy));
            testCases.add(testCase);
        }

        return TestUtil.convertTo2DimArray(testCases);
    }
}
