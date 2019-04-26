package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesGroupInfo {
    private List<SeriesMetaInfo> series;
    private BigDecimal groupScore;
    private BigDecimal totalScore;
}
