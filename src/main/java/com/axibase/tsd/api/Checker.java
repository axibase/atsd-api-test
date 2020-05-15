package com.axibase.tsd.api;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.util.NotCheckedException;

import java.util.concurrent.TimeUnit;

public class Checker {
    public static void check(AbstractCheck check) {
        check(check, BaseMethod.UPPER_BOUND_FOR_CHECK, TimeUnit.MILLISECONDS);
    }

    public static void check(AbstractCheck check, long maxWaitTime, TimeUnit unit) {
        final long retryUntil = System.currentTimeMillis() + unit.toMillis(maxWaitTime);
        while (!check.isChecked()) {
            if (System.currentTimeMillis() > retryUntil) {
                throw new NotCheckedException(check.getErrorMessage());
            } else {
                try {
                    Thread.sleep(BaseMethod.REQUEST_INTERVAL);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }
    }
}
