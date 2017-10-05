package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesSearchResult {
    private String query;
    private Integer recordsTotal;
    private Integer time;
    private SeriesSearchResultRecord[] data;
}
