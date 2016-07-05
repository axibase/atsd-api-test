package com.axibase.tsd.api.model;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @see <a href="https://github.com/axibase/atsd-docs/blob/master/api/data/properties/query.md#result-filter-fields">api docs</a>
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultFilter {
    private Integer limit;
    private Boolean last;
    private Integer offset;

    public Boolean isLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }


    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
