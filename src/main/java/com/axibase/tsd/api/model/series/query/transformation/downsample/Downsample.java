package com.axibase.tsd.api.model.series.query.transformation.downsample;

import com.axibase.tsd.api.model.Period;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
@NoArgsConstructor
public class Downsample {
    private BigDecimal difference;
    private BigDecimal ratio;
    private Period gap;
    private int order;
    private Algorithm algorithm = Algorithm.DETAIL;
    private DownsamplingType type;


    enum Algorithm {
        DETAIL, INTERPOLATE;
    }
}
