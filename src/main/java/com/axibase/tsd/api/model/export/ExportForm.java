package com.axibase.tsd.api.model.export;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExportForm {
    @JsonProperty("m")
    private String metric;
    @JsonProperty("e")
    private String entity;
    @JsonProperty("g")
    private String entityGroup;
    @JsonProperty("ex")
    private String entityExpression;
    @JsonProperty("si") // 1-HOUR
    private String selectionInterval;
    @JsonProperty("st")
    private String startTime;
    @JsonProperty("et")
    private String endTime;
    @JsonProperty("l")
    private Integer limit;
    @JsonProperty("t") // HISTORY, FORECAST
    private String exportType;
    @JsonProperty("f") // CSV, HTML
    private String exportFormat;
    @JsonIgnore
    private boolean aggregate;
    @JsonProperty("ai") // 1-MINUTE
    private String aggregateInterval;
    @JsonProperty("a")
    private String[] aggregations;
    @JsonProperty("fn")
    private String forecastName;
    @JsonProperty("np")
    private int numericPrecision = -1;
    @JsonProperty("v")
    private boolean versioning;
    @JsonProperty("df") // ISO8601, ISO8601_SECONDS, LOCALTIME, LOCALTIME_SECONDS, DATE_YMD, DATE_MD, MD_DOW, MD_DOW_FULL, DEFAULT
    private String dateFormat;
    @JsonProperty("tz") // UTC, LOCAL
    private String timeZone;
    @JsonProperty("vf")
    private String valueFilter;
    @JsonProperty("ro")
    private boolean revisionsOnly;
    @JsonProperty("rf")
    private String versionFilter;
    @JsonProperty("te")
    private List<String> entityTags;
    @JsonProperty("am")
    private boolean addMetaData;
    @JsonProperty("eft") // ALL, NAME, GROUP
    private String entityFilterType;
    @JsonProperty("tglfmt")
    private boolean toggleFormat;
    @JsonProperty("tglftr")
    private boolean useFilter;
    @JsonProperty("tc")
    private boolean includeTextColumn;
    @JsonProperty("ost")
    private List<String> outputSeriesTags = Collections.emptyList();
}
