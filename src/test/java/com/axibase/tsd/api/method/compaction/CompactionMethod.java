package com.axibase.tsd.api.method.compaction;

import com.axibase.tsd.api.method.BaseMethod;

public class CompactionMethod extends BaseMethod {
    public static void performCompaction(String day, boolean historical) {
        getHttpRootResource().path("/compaction")
                .queryParam("day", day)
                .queryParam("historical", historical).request().get().close();
    }
}