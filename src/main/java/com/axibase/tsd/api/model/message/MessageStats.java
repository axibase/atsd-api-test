package com.axibase.tsd.api.model.message;

import com.axibase.tsd.api.model.series.Series;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MessageStats extends Series {
    private String warning;
}
