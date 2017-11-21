package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.method.MethodParameters;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Setter @Getter
public class MetricListParameters extends MethodParameters {
    private String expression;
    private String minInsertDate;
    private String maxInsertDate;
    private Integer limit;
    private List<String> tags = new ArrayList<>();

    public MetricListParameters addTag(String tag) {
        tags.add(tag);
        return this;
    }

    public MetricListParameters setTags(List<String> tags) {
        this.tags = new ArrayList<>(tags);
        return this;
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

}
