package com.axibase.tsd.api.util;

import javax.ws.rs.core.GenericType;
import java.util.List;

public class ResponseAsList<T> extends GenericType<List<T>> {
    public ResponseAsList() {
        super();
    }
}
