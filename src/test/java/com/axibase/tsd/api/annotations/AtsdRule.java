package com.axibase.tsd.api.annotations;


import com.axibase.tsd.api.annotations.states.HbaseVersion;
import com.axibase.tsd.api.method.version.ProductVersion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AtsdRule {
    ProductVersion version() default ProductVersion.COMMUNITY;

    HbaseVersion hbaseVersion() default HbaseVersion.HBASE0;
}
