package com.axibase.tsd.api.method.metric;

import com.axibase.tsd.api.Checker;
import com.axibase.tsd.api.method.checks.MetricCheck;
import com.axibase.tsd.api.model.metric.Metric;
import com.axibase.tsd.api.util.NotCheckedException;

import static org.testng.Assert.fail;


public class MetricTest extends MetricMethod {
    public static void assertMetricExisting(String message, Metric metric) {
        try {
            Checker.check(new MetricCheck(metric));
        } catch (NotCheckedException e) {
            fail(message);
        }
    }
}
