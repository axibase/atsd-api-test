package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageQuery {
    private String entity;
    private String entityExpression;
    private String type;
    private String metric;
    private String startDate;
    private String endDate;
    private String severity;

    private String minSeverity;
    private List<String> severities;
    private String source;
    private Map<String, String> tags;
    private Period interval;
    private Integer limit;
}
