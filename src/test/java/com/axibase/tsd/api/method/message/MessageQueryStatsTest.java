package com.axibase.tsd.api.method.message;

import com.axibase.tsd.api.model.message.Message;
import com.axibase.tsd.api.model.message.MessageStatsQuery;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.AggregationType;
import com.axibase.tsd.api.model.series.Sample;
import com.axibase.tsd.api.model.series.Series;
import io.qameta.allure.Issue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.axibase.tsd.api.util.Util.MAX_QUERYABLE_DATE;
import static com.axibase.tsd.api.util.Util.MIN_QUERYABLE_DATE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.testng.AssertJUnit.assertEquals;

public class MessageQueryStatsTest extends MessageMethod {
    private final static String MESSAGE_STATS_ENTITY = "entity-message-statistics-1";
    private final static String MESSAGE_STATS_TYPE = "stats-type-1";
    private final static List<String> DATES = Arrays.asList(
            "2017-05-21T00:00:01.000Z",
            "2017-05-21T00:01:01.000Z",
            "2017-05-21T00:02:01.000Z",
            "2017-05-21T00:03:01.000Z",
            "2017-05-21T00:04:01.000Z");

    @BeforeClass
    public void insertMessages() throws Exception{
        Message message = new Message(MESSAGE_STATS_ENTITY, MESSAGE_STATS_TYPE);
        message.setMessage("message-stats-test");
        for(String date:DATES) {
            message.setDate(date);
            insertMessageCheck(message);
        }
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testNoAggregate() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testAggregateCount() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.COUNT));

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test(enabled = false)
    public void testAggregateDetail() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.DETAIL));

        List<Series> messageStatsList = queryMessageStatsReturnSeries(statsQuery);

        assertEquals("Response should contain only 1 series", 1, messageStatsList.size());
        List<Sample> samples = messageStatsList.get(0).getData();
        assertEquals("Response should contain only 1 sample", 1, samples.size());
        assertEquals("Message count mismatch", new BigDecimal(DATES.size()), samples.get(0).getValue());
    }

    @Issue("2945")
    @Test
    public void testAggregateUnknownRaiseError() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate(AggregationType.SUM));

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Issue("2945")
    @Test
    public void testAggregateNoTypeRaiseError() throws Exception {
        MessageStatsQuery statsQuery = prepareSimpleMessageStatsQuery(MESSAGE_STATS_ENTITY);
        statsQuery.setAggregate(new Aggregate());

        Response response = queryMessageStats(statsQuery);

        assertEquals("Query with unknown aggregate type should fail", BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    private MessageStatsQuery prepareSimpleMessageStatsQuery(String entityName) {
        MessageStatsQuery statsQuery = new MessageStatsQuery();
        statsQuery.setEntity(entityName);
        statsQuery.setType(MESSAGE_STATS_TYPE);
        statsQuery.setStartDate(MIN_QUERYABLE_DATE);
        statsQuery.setEndDate(MAX_QUERYABLE_DATE);
        return statsQuery;
    }
}
