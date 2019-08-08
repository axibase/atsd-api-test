package com.axibase.tsd.api.model.alert;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Korchagin.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert {
    private Integer id;
    private Boolean acknowledged;
    private String date;
    private String entity;
    private String metric;
    private String rule;
    private String type;
    private String ruleExpression;
    private String window;
    private Integer alertDuration;
    private String alertOpenDate;
    private String receivedDate;
    private String warning;
    private String alert;
    //TODO replace to ENUM
    private String severity;
    private Map<String, String> tags;
    private Integer repeatCount;
    private String textValue;
    private Double value;
    private Double openValue;
    private String openDate;
    private String lastEventDate;
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    //TODO consider adding method fromMap
}
