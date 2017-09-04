package com.axibase.tsd.api;


import com.axibase.tsd.api.method.BaseMethod;
import com.axibase.tsd.api.method.checks.AbstractCheck;
import com.axibase.tsd.api.util.NotCheckedException;
import lombok.SneakyThrows;

public class Checker {
    @SneakyThrows(InterruptedException.class)
    public static void check(AbstractCheck check) {
        final long startTime = System.currentTimeMillis();
        boolean checked = false;
        while (!checked) {
            checked = check.isChecked();
            if (!checked) {
                if (System.currentTimeMillis() - BaseMethod.UPPER_BOUND_FOR_CHECK > startTime) {
                    throw new NotCheckedException(check.getErrorMessage());
                }
                Thread.sleep(BaseMethod.REQUEST_INTERVAL);
            }
        }
    }
}
