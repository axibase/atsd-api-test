package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeriesGroupInfo {
    private List<SeriesMetaInfo> series;
    private double groupScore;
    private double totalScore;
}
