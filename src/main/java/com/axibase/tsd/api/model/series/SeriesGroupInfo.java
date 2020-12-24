package com.axibase.tsd.api.model.series;

import com.axibase.tsd.api.model.series.query.transformation.group.GroupType;
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
    private GroupType type;
    private BigDecimal groupScore;
    private BigDecimal totalScore;
}
