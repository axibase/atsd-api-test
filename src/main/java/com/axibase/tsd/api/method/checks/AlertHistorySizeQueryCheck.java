package com.axibase.tsd.api.method.checks;


import com.axibase.tsd.api.method.alert.AlertMethod;
import com.axibase.tsd.api.model.alert.AlertHistoryQuery;

import java.util.Collections;
import java.util.List;

public class AlertHistorySizeQueryCheck extends AbstractCheck {
    private List<AlertHistoryQuery> queryList;
    private Integer size;

    public AlertHistorySizeQueryCheck(AlertHistoryQuery query, Integer size) {
        this.queryList = Collections.singletonList(query);
        this.size = size;
    }

    public AlertHistorySizeQueryCheck(List<AlertHistoryQuery> queryList, Integer size) {
        this.queryList = queryList;
        this.size = size;
    }

    @Override
    public String getErrorMessage() {
        return String.format(
                "Query response( []) for alert history is empty!.%n%s",
                queryList
        );
    }

    @Override
    public boolean isChecked() {
        return AlertMethod.queryHistory(queryList).size() == size;
    }
}
