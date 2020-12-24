package com.axibase.tsd.api.method.series.trade.ohlcv;

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
public class OhlcvTestCasesBuilder {
    private final SyntheticDataProvider dp;
    Period period10 = new Period(10, MINUTE);
    Aggregate aggregate10 = new Aggregate(AggregationType.OHLCV, period10);
    Group group10 = new Group(GroupType.OHLCV, period10);

    public Object[][] testCases() {
        List<TestCase> testCases = new ArrayList<>();

        {   // 10-minutes aggregate, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregate10);
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy))
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell))
                    .series(new OhlcvSeries(dp.entityB, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell));
            testCases.add(testCase);
        }

        {   // 10-minute aggregate, with `side = *`, for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregate10);
            query.addTag("side", "*");
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy))
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell))
                    .series(new OhlcvSeries(dp.entityB, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell));
            testCases.add(testCase);
        }

        {   // 10-minute aggregate, with 'side = B', for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregate10);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy))
                    .series(new OhlcvSeries(dp.entityB, "B").sample(dp.time00, dp.ohlcvBuy).sample(dp.time20, dp.ohlcvBuy));
            testCases.add(testCase);
        }

        {   // 10-minute aggregate, with 'side = S', for whole selection interval
            SeriesQuery query = dp.aggregateQuery(aggregate10);
            query.addTag("side", "S");
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time00, dp.ohlcvSell).sample(dp.time30, dp.ohlcvSell));
            testCases.add(testCase);
        }

        {   // 10-minutes aggregate, without 'side' tag, for shortened selection interval
            SeriesQuery query = dp.aggregateQuery(aggregate10);
            query.setStartDate(dp.time30).setEndDate(dp.time50);
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time30, dp.ohlcvSell))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time30, dp.ohlcvSell));
            testCases.add(testCase);
        }

        {   // aggregation without period, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.aggregateQuery(new Aggregate(AggregationType.OHLCV));
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "B").sample(dp.time00, dp.ohlcvBuyTotal))
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time00, dp.ohlcvSellTotal))
                    .series(new OhlcvSeries(dp.entityB, "B").sample(dp.time00, dp.ohlcvBuyTotal))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time00, dp.ohlcvSellTotal));
            testCases.add(testCase);
        }

        {   // aggregation without period, with 'side = S', for shortened selection interval
            SeriesQuery query = dp.aggregateQuery(new Aggregate(AggregationType.OHLCV));
            query.addTag("side", "S");
            query.setStartDate(dp.time20);
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "S").sample(dp.time20, dp.ohlcvSell))
                    .series(new OhlcvSeries(dp.entityB, "S").sample(dp.time20, dp.ohlcvSell));
            testCases.add(testCase);
        }

        {   // 10-minutes group, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(group10, null);
            TestCase testCase = grouping10(query, null, "OHLCV");
            testCases.add(testCase);
        }

        {   // grouping without period, without 'side' tag, for whole selection interval
            SeriesQuery query = dp.entityAQuery(new Group(GroupType.OHLCV), null);
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, null, null, "OHLCV")
                            .sample(dp.time00, dp.ohlcvBothSidesTotal));
            testCases.add(testCase);
        }

        {   // 10-minutes group, with 'side = B' tag, for whole selection interval - the same result as for aggregation
            SeriesQuery query = dp.entityAQuery(group10, null);
            query.addTag("side", "B");
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, "B", null, "OHLCV")
                            .sample(dp.time00, dp.ohlcvBuy)
                            .sample(dp.time20, dp.ohlcvBuy));
            testCases.add(testCase);
        }

        {   // 10-minutes "DETAIL" group, 10-minute aggregate, without side tag, for whole selection interval - the same as 10-minutes grouping
            Group group = new Group(GroupType.DETAIL, period10);
            SeriesQuery query = dp.entityAQuery(group, aggregate10);
            TestCase testCase = grouping10(query, "OHLCV", "DETAIL");
            testCases.add(testCase);
        }

        {   // "DETAIL" grouping without period, 10-minute aggregate, without side tag, for whole selection interval - the same as 10-minutes grouping
            Group group = new Group(GroupType.DETAIL);
            SeriesQuery query = dp.entityAQuery(group, aggregate10);
            TestCase testCase = grouping10(query, "OHLCV", "DETAIL");
            testCases.add(testCase);
        }

        {   // "DETAIL" grouping without period, aggregate without period, without side tag, for whole selection interval - the same as grouping without period
            Group group = new Group(GroupType.DETAIL);
            Aggregate aggregate = new Aggregate(AggregationType.OHLCV);
            SeriesQuery query = dp.entityAQuery(group, aggregate);
            TestCase testCase = new TestCase(query)
                    .series(new OhlcvSeries(dp.entityA, null, "OHLCV", "DETAIL")
                            .sample(dp.time00, dp.ohlcvBothSidesTotal));
            testCases.add(testCase);
        }

        return TestUtil.convertTo2DimArray(testCases);
    }

    private TestCase grouping10(SeriesQuery query, String aggregation, String group) {
        return new TestCase(query)
                .series(new OhlcvSeries(dp.entityA, null, aggregation, group)
                        .sample(dp.time00, dp.ohlcvBothSides)
                        .sample(dp.time20, dp.ohlcvBuy)
                        .sample(dp.time30, dp.ohlcvSell));
    }
}
