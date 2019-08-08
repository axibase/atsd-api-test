package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.model.Period;
import com.axibase.tsd.api.model.series.query.transformation.aggregate.Aggregate;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageStatsQuery {
    private static final String MESSAGE_STATS_METRIC = "message-count";
    private final static String metric = MESSAGE_STATS_METRIC;
    private String entity;
    private String type;
    private String startDate;
    private String endDate;
    private String severity;
    private String source;
    private Map<String, String> tags;
    private Period interval;
    private Aggregate aggregate;
}
