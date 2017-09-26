package com.axibase.tsd.api.model.series;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleVersion {
    private String d;
}
